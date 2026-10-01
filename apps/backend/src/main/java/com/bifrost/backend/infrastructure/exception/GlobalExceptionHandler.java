package com.bifrost.backend.infrastructure.exception;

import com.bifrost.backend.domain.exception.AuthenticationFailedException;
import com.bifrost.backend.domain.exception.ConflictException;
import com.bifrost.backend.domain.exception.DomainException;
import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.ErrorResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(AuthenticationFailedException.class)
  public ResponseEntity<ErrorResponse> authFailed(AuthenticationFailedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of(ex.code(), ex.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> conflict(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(ex.code(), ex.getMessage()));
  }

  @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> forbidden(RuntimeException ex) {
    if (ex instanceof ForbiddenException forbidden) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ErrorResponse.of(forbidden.code(), forbidden.getMessage()));
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.of("ACCESS_DENIED", "Access denied"));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(ex.code(), ex.getMessage()));
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> validation(ValidationException ex) {
    return ResponseEntity.badRequest().body(ErrorResponse.of(ex.code(), ex.getMessage()));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> domain(DomainException ex) {
    return ResponseEntity.badRequest().body(ErrorResponse.of(ex.code(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> beanValidation(MethodArgumentNotValidException ex) {
    List<ErrorResponse.Detail> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::toDetail)
            .toList();
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", details));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> generic(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected error"));
  }

  private ErrorResponse.Detail toDetail(FieldError error) {
    return new ErrorResponse.Detail(error.getField(), error.getDefaultMessage());
  }
}
