package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import java.util.List;
import java.util.Map;

public record RobotProfilePatchRequest(
    String displayName,
    String project,
    String prefix,
    String environment,
    String technology,
    List<String> capabilities,
    Map<String, String> topics,
    Map<String, String> frames,
    String rosbridgeUrl,
    String videoBaseUrl,
    Boolean active) {}
