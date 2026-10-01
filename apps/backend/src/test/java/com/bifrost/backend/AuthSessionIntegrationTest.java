package com.bifrost.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
    registry.add("bifrost.admin.username", () -> "admin");
    registry.add("bifrost.admin.password", () -> "admin-pass");
    registry.add("bifrost.admin.enabled", () -> "true");
  }

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void loginMeSessionConfigAndForbiddenProfileSwitch() throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"username":"admin","password":"admin-pass"}
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.username").value("admin"))
            .andExpect(jsonPath("$.user.role").value("admin"))
            .andReturn();

    String accessCookie = extractCookie(login, "BIFROST_ACCESS");
    String refreshCookie = extractCookie(login, "BIFROST_REFRESH");

    mockMvc
        .perform(get("/api/v1/me").cookie(cookie("BIFROST_ACCESS", accessCookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("admin"));

    MvcResult session =
        mockMvc
            .perform(get("/api/v1/me/session-config").cookie(cookie("BIFROST_ACCESS", accessCookie)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.schemaVersion").value(1))
            .andExpect(jsonPath("$.activeProfile.slug").value("nara-sim-alfa"))
            .andExpect(jsonPath("$.permissions.canManageProfiles").value(true))
            .andExpect(jsonPath("$.limits.teleop.profile").value("normal"))
            .andReturn();

    JsonNode body = objectMapper.readTree(session.getResponse().getContentAsString());
    String profileId = body.path("activeProfile").path("id").asText();

    mockMvc
        .perform(
            put("/api/v1/me/active-profile")
                .cookie(cookie("BIFROST_ACCESS", accessCookie))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profileId\":\"" + profileId + "\"}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            put("/api/v1/me/active-profile")
                .cookie(cookie("BIFROST_ACCESS", accessCookie))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profileId\":\"00000000-0000-0000-0000-000000000099\"}"))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(post("/api/v1/auth/refresh").cookie(cookie("BIFROST_REFRESH", refreshCookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.username").value("admin"));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"admin","password":"wrong"}
                    """))
        .andExpect(status().isUnauthorized());
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
