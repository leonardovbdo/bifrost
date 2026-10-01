package com.bifrost.backend.domain.enums;

public enum ParameterScope {
  GLOBAL("global"),
  USER("user");

  private final String dbValue;

  ParameterScope(String dbValue) {
    this.dbValue = dbValue;
  }

  public String dbValue() {
    return dbValue;
  }

  public static ParameterScope fromDb(String value) {
    for (ParameterScope scope : values()) {
      if (scope.dbValue.equalsIgnoreCase(value)) {
        return scope;
      }
    }
    throw new IllegalArgumentException("Unknown parameter scope: " + value);
  }
}
