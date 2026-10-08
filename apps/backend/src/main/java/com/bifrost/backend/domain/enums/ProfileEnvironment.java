package com.bifrost.backend.domain.enums;

public enum ProfileEnvironment {
  SIM("sim"),
  PHYSICAL("physical");

  private final String dbValue;

  ProfileEnvironment(String dbValue) {
    this.dbValue = dbValue;
  }

  public String dbValue() {
    return dbValue;
  }

  public static ProfileEnvironment fromDb(String value) {
    for (ProfileEnvironment env : values()) {
      if (env.dbValue.equalsIgnoreCase(value)) {
        return env;
      }
    }
    throw new IllegalArgumentException("Unknown environment: " + value);
  }
}
