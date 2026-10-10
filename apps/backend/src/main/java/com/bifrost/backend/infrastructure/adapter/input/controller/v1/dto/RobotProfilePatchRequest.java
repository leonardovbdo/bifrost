package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

public record RobotProfilePatchRequest(
    String displayName,
    String project,
    String prefix,
    @Pattern(regexp = "sim|physical", flags = Pattern.Flag.CASE_INSENSITIVE, message = "environment must be sim or physical")
        String environment,
    String technology,
    List<String> capabilities,
    Map<String, String> topics,
    Map<String, String> frames,
    @Pattern(
            regexp = "^(ws|wss)://\\S+$",
            message = "rosbridgeUrl must use ws or wss scheme")
        String rosbridgeUrl,
    @Pattern(
            regexp = "^(http|https)://\\S+$",
            message = "videoBaseUrl must use http or https scheme")
        String videoBaseUrl,
    Boolean active) {}
