package com.bifrost.backend.application.usecase.telemetry;

import com.bifrost.backend.domain.model.TelemetrySample;
import com.bifrost.backend.domain.repository.TelemetrySampleRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTelemetrySamplesUseCase {
  private final TelemetrySampleRepository telemetrySampleRepository;

  public ListTelemetrySamplesUseCase(TelemetrySampleRepository telemetrySampleRepository) {
    this.telemetrySampleRepository = telemetrySampleRepository;
  }

  @Transactional(readOnly = true)
  public List<TelemetrySample> execute(String metric, UUID robotProfileId, int limit) {
    return telemetrySampleRepository.findRecent(
        normalizeMetricFilter(metric), robotProfileId, limit <= 0 ? 50 : limit);
  }

  /** Ingest stores metrics lowercased; the admin filter must match that form. */
  private static String normalizeMetricFilter(String metric) {
    if (metric == null || metric.isBlank()) {
      return null;
    }
    return metric.trim().toLowerCase(Locale.ROOT);
  }
}
