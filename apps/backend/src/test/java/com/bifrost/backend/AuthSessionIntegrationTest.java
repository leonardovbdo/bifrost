package com.bifrost.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthSessionIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void datasourceProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("bifrost.jwt.secret", () -> "test-secret-bifrost-at-least-32-characters!");
    registry.add("bifrost.admin.enabled", () -> "true");
    registry.add("bifrost.admin.username", () -> "admin");
    registry.add("bifrost.admin.password", () -> "admin-pass");
    registry.add("bifrost.operator.enabled", () -> "true");
    registry.add("bifrost.operator.username", () -> "operator");
    registry.add("bifrost.operator.password", () -> "operator-pass");
  }

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void authSessionAndProfileAclFlow() throws Exception {
    String adminAccess = login("admin", "admin-pass");
    String operatorAccess = login("operator", "operator-pass");

    mockMvc
        .perform(get("/api/v1/me").cookie(cookie("BIFROST_ACCESS", adminAccess)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("admin"));

    mockMvc
        .perform(get("/api/v1/me/session-config").cookie(cookie("BIFROST_ACCESS", adminAccess)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.schemaVersion").value(1))
        .andExpect(jsonPath("$.activeProfile.slug").value("nara-sim-alfa"))
        .andExpect(jsonPath("$.permissions.canManageProfiles").value(true));

    mockMvc
        .perform(get("/api/v1/robot-profiles").cookie(cookie("BIFROST_ACCESS", operatorAccess)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].slug").value("nara-sim-alfa"));

    MvcResult create =
        mockMvc
            .perform(
                post("/api/v1/robot-profiles")
                    .cookie(cookie("BIFROST_ACCESS", adminAccess))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "slug": "alpha-bot",
                          "displayName": "Alpha Bot",
                          "project": "demo",
                          "prefix": "alpha",
                          "environment": "sim",
                          "technology": "generic_diff_drive",
                          "capabilities": ["teleop"],
                          "topics": { "cmd_vel": "/demo/alpha/cmd_vel" },
                          "frames": { "base": "base_link" },
                          "rosbridgeUrl": "ws://localhost:9091",
                          "videoBaseUrl": "http://localhost:8081",
                          "active": true,
                          "grantAccessToCreator": true
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value("alpha-bot"))
            .andReturn();

    String alphaId = objectMapper.readTree(create.getResponse().getContentAsString()).path("id").asText();

    mockMvc
        .perform(
            post("/api/v1/robot-profiles")
                .cookie(cookie("BIFROST_ACCESS", operatorAccess))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "slug": "forbidden-create",
                      "displayName": "X",
                      "project": "demo",
                      "prefix": "x",
                      "environment": "sim",
                      "technology": "generic_diff_drive",
                      "capabilities": ["teleop"],
                      "topics": { "cmd_vel": "/x/cmd_vel" },
                      "rosbridgeUrl": "ws://localhost:9091",
                      "videoBaseUrl": "http://localhost:8081"
                    }
                    """))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            get("/api/v1/robot-profiles/" + alphaId).cookie(cookie("BIFROST_ACCESS", operatorAccess)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("PROFILE_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/me/active-profile")
                .cookie(cookie("BIFROST_ACCESS", operatorAccess))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profileId\":\"" + alphaId + "\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("PROFILE_FORBIDDEN"));

    mockMvc
        .perform(
            patch("/api/v1/robot-profiles/" + alphaId)
                .cookie(cookie("BIFROST_ACCESS", adminAccess))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Alpha Bot Renamed\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Alpha Bot Renamed"));

    mockMvc
        .perform(
            post("/api/v1/robot-profiles")
                .cookie(cookie("BIFROST_ACCESS", adminAccess))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "slug": "bad-teleop",
                      "displayName": "Bad",
                      "project": "demo",
                      "prefix": "bad",
                      "environment": "sim",
                      "technology": "generic_diff_drive",
                      "capabilities": ["teleop"],
                      "topics": { "map": "/demo/map" },
                      "rosbridgeUrl": "ws://localhost:9091",
                      "videoBaseUrl": "http://localhost:8081"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("PROFILE_INCONSISTENT"));

    mockMvc
        .perform(get("/api/v1/robot-profiles").cookie(cookie("BIFROST_ACCESS", adminAccess)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(2));
  }

  private String login(String username, String password) throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return extractCookie(login, "BIFROST_ACCESS");
  }

  private static String extractCookie(MvcResult result, String name) {
    for (String header : result.getResponse().getHeaders("Set-Cookie")) {
      if (header.startsWith(name + "=")) {
        return header.substring(name.length() + 1, header.indexOf(';'));
      }
    }
    throw new IllegalStateException("Missing cookie " + name);
  }

  private static jakarta.servlet.http.Cookie cookie(String name, String value) {
    return new jakarta.servlet.http.Cookie(name, value);
  }
}
