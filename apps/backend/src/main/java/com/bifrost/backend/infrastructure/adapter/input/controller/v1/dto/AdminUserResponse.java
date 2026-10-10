package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import com.bifrost.backend.domain.model.User;

public record AdminUserResponse(
    String id,
    String username,
    String email,
    String role,
    boolean active,
    String createdAt,
    String updatedAt) {
  public static AdminUserResponse from(User user) {
    return new AdminUserResponse(
        user.id().toString(),
        user.username(),
        user.email(),
        user.role().dbValue(),
        user.active(),
        user.createdAt().toString(),
        user.updatedAt().toString());
  }
}
