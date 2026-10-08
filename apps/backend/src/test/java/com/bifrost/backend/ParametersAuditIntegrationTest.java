package com.bifrost.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class ParametersAuditIntegrationTest {

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
  }

  @Autowired MockMvc mockMvc;

  @Test
  void parametersMergeAuditAndAcl() throws Exception {
    String admin = login("admin", "admin-pass");
    String operator = login("operator", "operator-pass");

    mockMvc
        .perform(get("/api/v1/me/session-config").cookie(cookie("BIFROST_ACCESS", operator)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.limits.teleop.profile").value("normal"))
        .andExpect(jsonPath("$.limits.teleop.linearMax").value(0.5));

    mockMvc
        .perform(
            put("/api/v1/parameters")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"scope":"user","key":"teleop.activeProfile","value":{"value":"safety"}}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.key").value("teleop.activeProfile"));

    mockMvc
        .perform(get("/api/v1/me/session-config").cookie(cookie("BIFROST_ACCESS", operator)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.limits.teleop.profile").value("safety"))
        .andExpect(jsonPath("$.limits.teleop.linearMax").value(0.2));

    mockMvc
        .perform(
            put("/api/v1/parameters")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"scope":"global","key":"teleop.activeProfile","value":{"value":"fast"}}
                    """))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/api/v1/audit/events").cookie(cookie("BIFROST_ACCESS", operator)))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            get("/api/v1/audit/events?type=login_success")
                .cookie(cookie("BIFROST_ACCESS", admin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

    mockMvc
        .perform(
            get("/api/v1/audit/events?type=parameter_change")
                .cookie(cookie("BIFROST_ACCESS", admin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

    mockMvc
        .perform(
            post("/api/v1/audit/events")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"goal_pose","payload":{"x":1.0,"y":2.0,"yaw":0.1}}
                    """))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/audit/events")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"login_success","payload":{}}
                    """))
        .andExpect(status().isBadRequest());

    String viewer = login("viewer", "viewer-pass");
    mockMvc
        .perform(
            post("/api/v1/audit/events")
                .cookie(cookie("BIFROST_ACCESS", viewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"goal_pose","payload":{"x":1.0,"y":2.0,"yaw":0.1}}
                    """))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong-pass\"}"))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            get("/api/v1/audit/events?type=login_failure")
                .cookie(cookie("BIFROST_ACCESS", admin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

    mockMvc
        .perform(
            put("/api/v1/parameters")
                .cookie(cookie("BIFROST_ACCESS", admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "scope":"global",
                      "key":"teleop.presets",
                      "value":{
                        "safety":{"linearMax":"fast","angularMax":0.6},
                        "normal":{"linearMax":0.5,"angularMax":1.0},
                        "fast":{"linearMax":0.8,"angularMax":1.4}
                      }
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("PARAMETER_VALUE_INVALID"));
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
