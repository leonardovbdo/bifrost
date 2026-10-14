package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

public record TelemetrySampleRequest(
    @NotNull UUID robotProfileId,
    @NotBlank @Size(max = 64) String metric,
    Map<String, Object> value) {}
