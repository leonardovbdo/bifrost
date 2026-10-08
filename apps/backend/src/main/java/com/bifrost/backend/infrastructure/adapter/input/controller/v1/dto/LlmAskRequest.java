package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record LlmAskRequest(@NotBlank String prompt, Map<String, Object> context) {}
