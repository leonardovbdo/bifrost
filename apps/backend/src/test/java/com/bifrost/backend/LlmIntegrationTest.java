package com.bifrost.backend;

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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class LlmIntegrationTest {

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
    registry.add("bifrost.llm.api-key", () -> "");
    registry.add("bifrost.llm.stub-when-missing-key", () -> "true");
  }

  @Autowired MockMvc mockMvc;

  @Test
  void operatorCanAskViewerForbiddenAndAuditEmitted() throws Exception {
    String operator = login("operator", "operator-pass");
    String viewer = login("viewer", "viewer-pass");
    String admin = login("admin", "admin-pass");

    mockMvc
        .perform(
            post("/api/v1/llm/ask")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"prompt":"Hello NARA","context":{"profileSlug":"nara-sim-alfa"}}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reply").exists())
        .andExpect(jsonPath("$.model").value(org.hamcrest.Matchers.containsString("stub")));

    mockMvc
        .perform(
            post("/api/v1/llm/ask")
                .cookie(cookie("BIFROST_ACCESS", viewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"should fail\"}"))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                    "/api/v1/audit/events?type=llm_ask")
                .cookie(cookie("BIFROST_ACCESS", admin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.items[0].payload.promptLength").exists())
        .andExpect(jsonPath("$.items[0].payload").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasKey("prompt"))));
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
        return header.substring("BIFROST_ACCESS=".length(), header.indexOf(';'));
      }
    }
    throw new IllegalStateException("missing access cookie");
  }

  private static jakarta.servlet.http.Cookie cookie(String name, String value) {
    return new jakarta.servlet.http.Cookie(name, value);
  }
}
