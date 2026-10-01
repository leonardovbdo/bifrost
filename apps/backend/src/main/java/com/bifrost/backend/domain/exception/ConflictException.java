package com.bifrost.backend.domain.exception;

public class ConflictException extends DomainException {
  public ConflictException(String code, String message) {
    super(code, message);
  }
}
