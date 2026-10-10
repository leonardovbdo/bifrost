package com.bifrost.backend.domain.exception;

public class RateLimitExceededException extends DomainException {
  public RateLimitExceededException(String code, String message) {
    super(code, message);
  }
}
