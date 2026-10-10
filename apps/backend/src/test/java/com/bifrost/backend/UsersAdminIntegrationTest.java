package com.bifrost.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class UsersAdminIntegrationTest {

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
  void adminUsersCrudAndAcl() throws Exception {
    String admin = login("admin", "admin-pass");
    String operator = login("operator", "operator-pass");

    mockMvc
        .perform(get("/api/v1/users").cookie(cookie("BIFROST_ACCESS", admin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(greaterThanOrEqualTo(3)))
        .andExpect(jsonPath("$.items[0].passwordHash").doesNotExist())
        .andExpect(jsonPath("$.items[0].password_hash").doesNotExist());

    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/users")
                    .cookie(cookie("BIFROST_ACCESS", admin))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "username":"demo-user",
                          "password":"demo-pass-123",
                          "role":"viewer",
                          "email":"demo@example.com",
                          "active":true
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("demo-user"))
            .andExpect(jsonPath("$.role").value("viewer"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andReturn();

    String userId = readJson(created, "$.id");

    mockMvc
        .perform(
            patch("/api/v1/users/" + userId)
                .cookie(cookie("BIFROST_ACCESS", admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"active":false,"role":"operator"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false))
        .andExpect(jsonPath("$.role").value("operator"));

    mockMvc
        .perform(get("/api/v1/users").cookie(cookie("BIFROST_ACCESS", operator)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            post("/api/v1/users")
                .cookie(cookie("BIFROST_ACCESS", operator))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"x","password":"y","role":"viewer"}
                    """))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            patch("/api/v1/users/" + userId)
                .cookie(cookie("BIFROST_ACCESS", admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("USER_PATCH_EMPTY"));
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

  private static String readJson(MvcResult result, String jsonPath) throws Exception {
    return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
  }
}
