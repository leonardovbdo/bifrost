package com.bifrost.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
class TelemetrySamplesIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
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
    registry.add("bifrost.viewer.enabled", () -> "true");
    registry.add("bifrost.viewer.username", () -> "viewer");
    registry.add("bifrost.viewer.password", () -> "viewer-pass");
    registry.add("bifrost.telemetry.min-sample-interval", () -> "1h");
  }

  @Autowired MockMvc mockMvc;

  @Test
  void ingestSampleListAndRejectFloodAndViewer() throws Exception {
    String admin = login("admin", "admin-pass");
    String operator = login("operator", "operator-pass");
    String viewer = login("viewer", "viewer-pass");

    MvcResult session =
        mockMvc
            .perform(get("/api/v1/me/session-config").cookie(cookie("BIFROST_ACCESS", operator)))
            .andExpect(status().isOk())
            .andReturn();
    String profileId =
        JsonPath.read(session.getResponse().getContentAsString(), "$.activeProfile.id");

    mockMvc
        .perform(
            post("/api/v1/telemetry/samples")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "robotProfileId":"%s",
                      "metric":"battery",
                      "value":{"percentage":87.5}
                    }
                    """
                        .formatted(profileId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.metric").value("battery"))
        .andExpect(jsonPath("$.value.percentage").value(87.5));

    mockMvc
        .perform(
            post("/api/v1/telemetry/samples")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "robotProfileId":"%s",
                      "metric":"battery",
                      "value":{"percentage":86.0}
                    }
                    """
                        .formatted(profileId)))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.error.code").value("TELEMETRY_SAMPLE_TOO_FREQUENT"));

    mockMvc
        .perform(
            post("/api/v1/telemetry/samples")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "robotProfileId":"%s",
                      "metric":"cmd_vel",
                      "value":{"linear":0.1}
                    }
                    """
                        .formatted(profileId)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("TELEMETRY_METRIC_FORBIDDEN"));

    mockMvc
        .perform(
            post("/api/v1/telemetry/samples")
                .cookie(cookie("BIFROST_ACCESS", viewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "robotProfileId":"%s",
                      "metric":"pose",
                      "value":{"x":1.0,"y":2.0}
                    }
                    """
                        .formatted(profileId)))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            get("/api/v1/telemetry/samples?metric=battery")
                .cookie(cookie("BIFROST_ACCESS", operator)))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            get("/api/v1/telemetry/samples?metric=battery")
                .cookie(cookie("BIFROST_ACCESS", admin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].metric").value("battery"));
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
    for (String header : login.getResponse().getHeaders("Set-Cookie")) {
      if (header.startsWith("BIFROST_ACCESS=")) {
        String value = header.substring("BIFROST_ACCESS=".length(), header.indexOf(';'));
        if (!value.isEmpty()) {
          return value;
        }
      }
    }
    throw new IllegalStateException("missing access cookie");
  }

  private static jakarta.servlet.http.Cookie cookie(String name, String value) {
    return new jakarta.servlet.http.Cookie(name, value);
  }
}
