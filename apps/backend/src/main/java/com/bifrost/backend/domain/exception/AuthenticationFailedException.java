package com.bifrost.backend.domain.exception;

public class AuthenticationFailedException extends DomainException {
  public AuthenticationFailedException() {
    super("AUTH_FAILED", "Invalid username or password");
  }
}
