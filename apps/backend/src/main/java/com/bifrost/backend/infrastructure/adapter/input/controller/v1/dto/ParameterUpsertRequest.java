package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ParameterUpsertRequest(
    @NotBlank String scope, @NotBlank String key, @NotNull Map<String, Object> value) {}
