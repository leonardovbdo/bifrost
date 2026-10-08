package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import com.bifrost.backend.application.dto.AuthenticatedUserView;

public record UserResponse(String id, String username, String role) {
  public static UserResponse from(AuthenticatedUserView view) {
    return new UserResponse(view.id().toString(), view.username(), view.role().dbValue());
  }
}
