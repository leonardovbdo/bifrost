package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Size(max = 100) String username, @NotBlank @Size(max = 72) String password) {}
