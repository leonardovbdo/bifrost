package com.bifrost.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class HardeningIntegrationTest {

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
    registry.add("bifrost.llm.api-key", () -> "");
    registry.add("bifrost.llm.stub-when-missing-key", () -> "true");
    registry.add("bifrost.llm.rate-limit.max-requests", () -> "2");
    registry.add("bifrost.llm.rate-limit.window", () -> "1m");
    registry.add("bifrost.audit.client-events.max-goal-pose-per-window", () -> "2");
    registry.add("bifrost.audit.client-events.window", () -> "1m");
  }

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void llmAskReturns429WhenRateLimited() throws Exception {
    String operator = login("operator", "operator-pass");
    String body = "{\"prompt\":\"ping\",\"context\":{}}";

    mockMvc
        .perform(
            post("/api/v1/llm/ask")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/v1/llm/ask")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/v1/llm/ask")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.error.code").value("LLM_RATE_LIMIT"));
  }

  @Test
  void goalPoseAuditReturns429WhenRateLimited() throws Exception {
    String operator = login("operator", "operator-pass");
    String payload =
        """
        {"type":"goal_pose","payload":{"x":1.0,"y":2.0,"yaw":0.0}}
        """;

    mockMvc
        .perform(
            post("/api/v1/audit/events")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isCreated());
    mockMvc
        .perform(
            post("/api/v1/audit/events")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isCreated());
    mockMvc
        .perform(
            post("/api/v1/audit/events")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.error.code").value("AUDIT_RATE_LIMIT"));
  }

  @Test
  void profilePatchRejectsInvalidUrlsWith400() throws Exception {
    String admin = login("admin", "admin-pass");
    MvcResult list =
        mockMvc
            .perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                        "/api/v1/robot-profiles")
                    .cookie(cookie("BIFROST_ACCESS", admin)))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode first = objectMapper.readTree(list.getResponse().getContentAsString()).path("items").get(0);
    String profileId = first.path("id").asText();

    mockMvc
        .perform(
            patch("/api/v1/robot-profiles/" + profileId)
                .cookie(cookie("BIFROST_ACCESS", admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rosbridgeUrl\":\"ftp://bad.example/ws\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

    mockMvc
        .perform(
            patch("/api/v1/robot-profiles/" + profileId)
                .cookie(cookie("BIFROST_ACCESS", admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"environment\":\"outer-space\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
  }

  @Test
  void loginRejectsOversizedCredentials() throws Exception {
    String longPassword = "p".repeat(129);
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"" + longPassword + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
  }

  @Test
  void loginFailureAuditTruncatesLongUsername() throws Exception {
    String admin = login("admin", "admin-pass");
    String longUsername = "u".repeat(80);
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\""
                        + longUsername
                        + "\",\"password\":\"wrong-pass\"}"))
        .andExpect(status().isUnauthorized());

    MvcResult audit =
        mockMvc
            .perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                        "/api/v1/audit/events?type=login_failure&limit=5")
                    .cookie(cookie("BIFROST_ACCESS", admin)))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode items = objectMapper.readTree(audit.getResponse().getContentAsString()).path("items");
    String stored = items.get(0).path("payload").path("username").asText();
    assertThat(stored).hasSize(64);
    assertThat(stored).isEqualTo("u".repeat(64));
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
