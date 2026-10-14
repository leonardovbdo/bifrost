package com.bifrost.backend.domain.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TelemetrySample {
  private final UUID id;
  private final UUID robotProfileId;
  private final String metric;
  private final Map<String, Object> value;
  private final Instant capturedAt;

  public TelemetrySample(
      UUID id, UUID robotProfileId, String metric, Map<String, Object> value, Instant capturedAt) {
    this.id = Objects.requireNonNull(id);
    this.robotProfileId = Objects.requireNonNull(robotProfileId);
    this.metric = Objects.requireNonNull(metric);
    this.value = copyWithoutNulls(value);
    this.capturedAt = Objects.requireNonNull(capturedAt);
  }

  public static TelemetrySample create(
      UUID robotProfileId, String metric, Map<String, Object> value) {
    return new TelemetrySample(
        UUID.randomUUID(), robotProfileId, metric, value, Instant.now());
  }

  private static Map<String, Object> copyWithoutNulls(Map<String, Object> value) {
    if (value == null || value.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> copy = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : value.entrySet()) {
      if (entry.getKey() != null && entry.getValue() != null) {
        copy.put(entry.getKey(), entry.getValue());
      }
    }
    return Map.copyOf(copy);
  }

  public UUID id() {
    return id;
  }

  public UUID robotProfileId() {
    return robotProfileId;
  }

  public String metric() {
    return metric;
  }

  public Map<String, Object> value() {
    return value;
  }

  public Instant capturedAt() {
    return capturedAt;
  }
}
