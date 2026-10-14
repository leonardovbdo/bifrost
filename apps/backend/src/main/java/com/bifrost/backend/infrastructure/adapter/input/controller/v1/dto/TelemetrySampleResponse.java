package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import com.bifrost.backend.domain.model.TelemetrySample;
import java.time.Instant;
import java.util.Map;

public record TelemetrySampleResponse(
    String id,
    String robotProfileId,
    String metric,
    Map<String, Object> value,
    Instant capturedAt) {
  public static TelemetrySampleResponse from(TelemetrySample sample) {
    return new TelemetrySampleResponse(
        sample.id().toString(),
        sample.robotProfileId().toString(),
        sample.metric(),
        sample.value(),
        sample.capturedAt());
  }
}
