package com.bifrost.backend.domain.exception;

public class ValidationException extends DomainException {
  public ValidationException(String code, String message) {
    super(code, message);
  }
}
