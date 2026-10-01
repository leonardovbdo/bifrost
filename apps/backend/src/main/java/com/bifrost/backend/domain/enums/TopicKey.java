package com.bifrost.backend.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public enum TopicKey {
  CMD_VEL("cmd_vel"),
  CAMERA_LINK("camera_link"),
  CAMERA_USER("camera_user"),
  SCAN("scan"),
  MAP("map"),
  ODOM("odom"),
  BATTERY("battery"),
  GOAL_POSE("goal_pose"),
  JOINT_STATES("joint_states");

  private final String wireValue;

  TopicKey(String wireValue) {
    this.wireValue = wireValue;
  }

  public String wireValue() {
    return wireValue;
  }

  public static TopicKey fromWire(String value) {
    for (TopicKey key : values()) {
      if (key.wireValue.equals(value)) {
        return key;
      }
    }
    throw new IllegalArgumentException("Unknown topic key: " + value);
  }

  public static Set<String> allowedWireValues() {
    return Arrays.stream(values()).map(TopicKey::wireValue).collect(Collectors.toSet());
  }
}
