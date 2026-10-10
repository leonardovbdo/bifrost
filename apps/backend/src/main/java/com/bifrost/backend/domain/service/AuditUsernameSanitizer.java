package com.bifrost.backend.domain.service;

public final class AuditUsernameSanitizer {
  public static final int MAX_AUDIT_USERNAME_LENGTH = 64;

  private AuditUsernameSanitizer() {}

  public static String forAudit(String username) {
    if (username == null) {
      return "";
    }
    if (username.length() <= MAX_AUDIT_USERNAME_LENGTH) {
      return username;
    }
    return username.substring(0, MAX_AUDIT_USERNAME_LENGTH);
  }
}
