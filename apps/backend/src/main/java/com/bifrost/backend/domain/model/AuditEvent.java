package com.bifrost.backend.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AuditEvent {
  private final UUID id;
  private final UUID userId;
  private final String type;
  private final UUID robotProfileId;
  private final Map<String, Object> payload;
  private final Instant createdAt;

  public AuditEvent(
      UUID id,
      UUID userId,
      String type,
      UUID robotProfileId,
      Map<String, Object> payload,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.userId = userId;
    this.type = Objects.requireNonNull(type);
    this.robotProfileId = robotProfileId;
    this.payload = Map.copyOf(payload == null ? Map.of() : payload);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static AuditEvent create(
      String type, UUID userId, UUID robotProfileId, Map<String, Object> payload) {
    return new AuditEvent(UUID.randomUUID(), userId, type, robotProfileId, payload, Instant.now());
  }

  public UUID id() { return id; }
  public UUID userId() { return userId; }
  public String type() { return type; }
  public UUID robotProfileId() { return robotProfileId; }
  public Map<String, Object> payload() { return payload; }
  public Instant createdAt() { return createdAt; }
}
