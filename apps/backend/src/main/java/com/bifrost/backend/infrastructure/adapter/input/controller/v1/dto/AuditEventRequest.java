package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record AuditEventRequest(
    @NotBlank String type, UUID robotProfileId, Map<String, Object> payload) {}
