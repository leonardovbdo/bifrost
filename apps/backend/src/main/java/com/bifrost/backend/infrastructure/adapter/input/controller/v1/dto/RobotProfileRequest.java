package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record RobotProfileRequest(
    @NotBlank String slug,
    @NotBlank String displayName,
    @NotBlank String project,
    @NotBlank String prefix,
    @NotBlank String environment,
    @NotBlank String technology,
    @NotEmpty List<String> capabilities,
    @NotNull Map<String, String> topics,
    Map<String, String> frames,
    @NotBlank String rosbridgeUrl,
    @NotBlank String videoBaseUrl,
    Boolean active,
    Boolean grantAccessToCreator) {}
