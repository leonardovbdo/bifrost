package com.bifrost.backend.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TeleopLimitsMergerTest {

  @Test
  void userActiveOverridesGlobal() {
    Map<String, Object> presets =
        Map.of(
            "safety", Map.of("linearMax", 0.2, "angularMax", 0.6),
            "normal", Map.of("linearMax", 0.5, "angularMax", 1.0),
            "fast", Map.of("linearMax", 0.8, "angularMax", 1.4));
    Map<String, Object> fallback = Map.of("linearMax", 0.5, "angularMax", 1.0, "profile", "normal");

    Map<String, Object> limits = TeleopLimitsMerger.merge(presets, "normal", "safety", fallback);

    assertEquals("safety", limits.get("profile"));
    assertEquals(0.2, ((Number) limits.get("linearMax")).doubleValue());
  }
}
