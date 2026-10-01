package com.bifrost.backend.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.enums.ProfileEnvironment;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainRulesTest {

  @Test
  void viewerCannotTeleop() {
    var permissions = SessionPermissionResolver.resolve(UserRole.VIEWER);
    assertFalse(permissions.get("canTeleop"));
    assertFalse(permissions.get("canManageProfiles"));
  }

  @Test
  void adminCanManageProfiles() {
    var permissions = SessionPermissionResolver.resolve(UserRole.ADMIN);
    assertTrue(permissions.get("canTeleop"));
    assertTrue(permissions.get("canManageProfiles"));
  }

  @Test
  void teleopWithoutCmdVelFails() {
    Map<String, String> topics = new LinkedHashMap<>();
    topics.put("map", "/noblenara/alfa/map");
    RobotProfile profile =
        new RobotProfile(
            UUID.randomUUID(),
            "bad",
            "Bad",
            "noblenara",
            "alfa",
            ProfileEnvironment.SIM,
            "wheelchair_nara",
            List.of("teleop"),
            topics,
            Map.of(),
            "ws://localhost:9090",
            "http://localhost:8080",
            true,
            Instant.now(),
            Instant.now());
    ValidationException ex =
        assertThrows(ValidationException.class, () -> RobotProfileValidator.validate(profile));
    assertEquals("PROFILE_INCONSISTENT", ex.code());
  }
}
