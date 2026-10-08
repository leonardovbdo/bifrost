package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.AuditEvent;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.AuditEventJpaEntity;
import java.util.HashMap;

public final class AuditEventMapper {
  private AuditEventMapper() {}

  public static AuditEvent toDomain(AuditEventJpaEntity entity) {
    return new AuditEvent(
        entity.getId(),
        entity.getUserId(),
        entity.getType(),
        entity.getRobotProfileId(),
        entity.getPayloadJson(),
        entity.getCreatedAt());
  }

  public static AuditEventJpaEntity toEntity(AuditEvent event) {
    AuditEventJpaEntity entity = new AuditEventJpaEntity();
    entity.setId(event.id());
    entity.setUserId(event.userId());
    entity.setType(event.type());
    entity.setRobotProfileId(event.robotProfileId());
    entity.setPayloadJson(new HashMap<>(event.payload()));
    entity.setCreatedAt(event.createdAt());
    return entity;
  }
}
