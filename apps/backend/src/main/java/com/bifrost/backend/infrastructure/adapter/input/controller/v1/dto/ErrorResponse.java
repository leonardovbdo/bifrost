package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import java.util.List;

public record ErrorResponse(ErrorBody error) {
  public record ErrorBody(String code, String message, List<Detail> details) {}

  public record Detail(String field, String message) {}

  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(new ErrorBody(code, message, null));
  }

  public static ErrorResponse of(String code, String message, List<Detail> details) {
    return new ErrorResponse(new ErrorBody(code, message, details));
  }
}
