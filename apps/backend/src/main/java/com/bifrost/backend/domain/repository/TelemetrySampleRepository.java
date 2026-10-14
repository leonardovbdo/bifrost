package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.model.TelemetrySample;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetrySampleRepository {
  TelemetrySample save(TelemetrySample sample);

  Optional<TelemetrySample> findLatest(UUID robotProfileId, String metric);

  List<TelemetrySample> findRecent(String metric, UUID robotProfileId, int limit);
}
