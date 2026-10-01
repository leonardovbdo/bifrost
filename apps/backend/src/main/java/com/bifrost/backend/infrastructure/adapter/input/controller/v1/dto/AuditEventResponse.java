package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import com.bifrost.backend.domain.model.AuditEvent;
import java.time.Instant;
import java.util.Map;

public record AuditEventResponse(
    String id,
    String userId,
    String type,
    String robotProfileId,
    Map<String, Object> payload,
    Instant createdAt) {
  public static AuditEventResponse from(AuditEvent event) {
    return new AuditEventResponse(
        event.id().toString(),
        event.userId() == null ? null : event.userId().toString(),
        event.type(),
        event.robotProfileId() == null ? null : event.robotProfileId().toString(),
        event.payload(),
        event.createdAt());
  }
}
