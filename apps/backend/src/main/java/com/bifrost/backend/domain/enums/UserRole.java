package com.bifrost.backend.domain.enums;

public enum UserRole {
  ADMIN("admin", "ROLE_ADMIN"),
  OPERATOR("operator", "ROLE_OPERATOR"),
  VIEWER("viewer", "ROLE_VIEWER");

  private final String dbValue;
  private final String springRole;

  UserRole(String dbValue, String springRole) {
    this.dbValue = dbValue;
    this.springRole = springRole;
  }

  public String dbValue() {
    return dbValue;
  }

  public String springRole() {
    return springRole;
  }

  public static UserRole fromDb(String value) {
    for (UserRole role : values()) {
      if (role.dbValue.equalsIgnoreCase(value)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Unknown role: " + value);
  }
}
