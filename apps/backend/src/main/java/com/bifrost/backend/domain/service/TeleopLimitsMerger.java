package com.bifrost.backend.domain.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TeleopLimitsMerger {
  public static final String KEY_PRESETS = "teleop.presets";
  public static final String KEY_ACTIVE = "teleop.activeProfile";

  private TeleopLimitsMerger() {}

  @SuppressWarnings("unchecked")
  public static Map<String, Object> merge(
      Map<String, Object> globalPresets,
      String globalActive,
      String userActive,
      Map<String, Object> fallback) {
    String active = userActive != null && !userActive.isBlank() ? userActive : globalActive;
    if (active == null || active.isBlank()) {
      active = String.valueOf(fallback.getOrDefault("profile", "normal"));
    }

    Map<String, Object> presets =
        globalPresets != null ? globalPresets : Map.of();
    Object selected = presets.get(active);
    if (!(selected instanceof Map<?, ?> selectedMap) || selectedMap.isEmpty()) {
      Map<String, Object> copy = new LinkedHashMap<>(fallback);
      copy.put("profile", active);
      return copy;
    }

    Map<String, Object> limits = new LinkedHashMap<>();
    Object linear = selectedMap.get("linearMax");
    Object angular = selectedMap.get("angularMax");
    limits.put("linearMax", linear != null ? linear : fallback.get("linearMax"));
    limits.put("angularMax", angular != null ? angular : fallback.get("angularMax"));
    limits.put("profile", active);
    return limits;
  }
}
