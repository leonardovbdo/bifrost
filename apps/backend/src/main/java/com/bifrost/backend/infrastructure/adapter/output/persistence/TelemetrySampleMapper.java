package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.TelemetrySample;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.TelemetrySampleJpaEntity;
import java.util.HashMap;

public final class TelemetrySampleMapper {
  private TelemetrySampleMapper() {}

  public static TelemetrySample toDomain(TelemetrySampleJpaEntity entity) {
    return new TelemetrySample(
        entity.getId(),
        entity.getRobotProfileId(),
        entity.getMetric(),
        entity.getValueJson(),
        entity.getCapturedAt());
  }

  public static TelemetrySampleJpaEntity toEntity(TelemetrySample sample) {
    TelemetrySampleJpaEntity entity = new TelemetrySampleJpaEntity();
    entity.setId(sample.id());
    entity.setRobotProfileId(sample.robotProfileId());
    entity.setMetric(sample.metric());
    entity.setValueJson(new HashMap<>(sample.value()));
    entity.setCapturedAt(sample.capturedAt());
    return entity;
  }
}
