package com.bifrost.backend.domain.port.output;

public interface PasswordHasher {
  String hash(String rawPassword);

  boolean matches(String rawPassword, String passwordHash);
}
