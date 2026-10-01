package com.bifrost.backend.domain.service;

import com.bifrost.backend.domain.enums.UserRole;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SessionPermissionResolver {
  private SessionPermissionResolver() {}

  public static Map<String, Boolean> resolve(UserRole role) {
    Map<String, Boolean> permissions = new LinkedHashMap<>();
    boolean isAdmin = role == UserRole.ADMIN;
    boolean isOperator = role == UserRole.OPERATOR || isAdmin;
    permissions.put("canTeleop", isOperator);
    permissions.put("canSendGoal", isOperator);
    permissions.put("canInspectRosapi", isOperator);
    permissions.put("canManageProfiles", isAdmin);
    return permissions;
  }
}
