package com.bifrost.backend.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public enum Capability {
  TELEOP,
  CAMERAS,
  SLAM,
  NAV2,
  BATTERY,
  ROSAPI;

  public String wireValue() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Capability fromWire(String value) {
    return valueOf(value.trim().toUpperCase(Locale.ROOT));
  }

  public static Set<String> allowedWireValues() {
    return Arrays.stream(values()).map(Capability::wireValue).collect(Collectors.toSet());
  }
}
