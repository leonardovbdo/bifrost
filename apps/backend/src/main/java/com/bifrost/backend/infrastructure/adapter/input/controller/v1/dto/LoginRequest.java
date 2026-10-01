package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
