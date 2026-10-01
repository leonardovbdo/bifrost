package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GrantAccessRequest(@NotNull UUID userId) {}
