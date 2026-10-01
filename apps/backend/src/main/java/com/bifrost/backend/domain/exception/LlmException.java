package com.bifrost.backend.domain.exception;

public class LlmException extends DomainException {
  public LlmException(String code, String message) {
    super(code, message);
  }
}
