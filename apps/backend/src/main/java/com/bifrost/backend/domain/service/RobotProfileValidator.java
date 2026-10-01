package com.bifrost.backend.domain.service;

import com.bifrost.backend.domain.enums.Capability;
import com.bifrost.backend.domain.enums.TopicKey;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.RobotProfile;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RobotProfileValidator {
  private RobotProfileValidator() {}

  public static void validate(RobotProfile profile) {
    validateCapabilities(profile.capabilities());
    validateTopics(profile.topics());
    validateConsistency(profile.capabilities(), profile.topics());
    if (profile.rosbridgeUrl() == null || profile.rosbridgeUrl().isBlank()) {
      throw new ValidationException("PROFILE_URL_REQUIRED", "rosbridgeUrl is required");
    }
    if (profile.videoBaseUrl() == null || profile.videoBaseUrl().isBlank()) {
      throw new ValidationException("PROFILE_URL_REQUIRED", "videoBaseUrl is required");
    }
  }

  public static void validateCapabilities(List<String> capabilities) {
    Set<String> allowed = Capability.allowedWireValues();
    for (String capability : capabilities) {
      if (!allowed.contains(capability)) {
        throw new ValidationException("PROFILE_CAPABILITY_INVALID", "Unknown capability: " + capability);
      }
    }
  }

  public static void validateTopics(Map<String, String> topics) {
    Set<String> allowed = TopicKey.allowedWireValues();
    for (String key : topics.keySet()) {
      if (!allowed.contains(key)) {
        throw new ValidationException("PROFILE_TOPIC_INVALID", "Unknown topic key: " + key);
      }
      String value = topics.get(key);
      if (value == null || value.isBlank()) {
        throw new ValidationException("PROFILE_TOPIC_INVALID", "Empty topic for key: " + key);
      }
    }
  }

  public static void validateConsistency(List<String> capabilities, Map<String, String> topics) {
    if (capabilities.contains(Capability.TELEOP.wireValue()) && !topics.containsKey(TopicKey.CMD_VEL.wireValue())) {
      throw new ValidationException("PROFILE_INCONSISTENT", "teleop requires cmd_vel topic");
    }
    if (capabilities.contains(Capability.CAMERAS.wireValue())
        && !topics.containsKey(TopicKey.CAMERA_LINK.wireValue())
        && !topics.containsKey(TopicKey.CAMERA_USER.wireValue())) {
      throw new ValidationException(
          "PROFILE_INCONSISTENT", "cameras requires camera_link or camera_user");
    }
    if (capabilities.contains(Capability.NAV2.wireValue()) && !topics.containsKey(TopicKey.GOAL_POSE.wireValue())) {
      throw new ValidationException("PROFILE_INCONSISTENT", "nav2 requires goal_pose topic");
    }
    if (capabilities.contains(Capability.SLAM.wireValue()) && !topics.containsKey(TopicKey.MAP.wireValue())) {
      throw new ValidationException("PROFILE_INCONSISTENT", "slam requires map topic");
    }
    if (capabilities.contains(Capability.BATTERY.wireValue()) && !topics.containsKey(TopicKey.BATTERY.wireValue())) {
      throw new ValidationException("PROFILE_INCONSISTENT", "battery requires battery topic");
    }
  }
}
