package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import com.bifrost.backend.domain.model.RobotProfile;
import java.util.List;
import java.util.Map;

public record RobotProfileResponse(
    String id,
    String slug,
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
    boolean active) {
  public static RobotProfileResponse from(RobotProfile profile) {
    return new RobotProfileResponse(
        profile.id().toString(),
        profile.slug(),
        profile.displayName(),
        profile.project(),
        profile.prefix(),
        profile.environment().dbValue(),
        profile.technology(),
        profile.capabilities(),
        profile.topics(),
        profile.frames(),
        profile.rosbridgeUrl(),
        profile.videoBaseUrl(),
        profile.active());
  }
}
