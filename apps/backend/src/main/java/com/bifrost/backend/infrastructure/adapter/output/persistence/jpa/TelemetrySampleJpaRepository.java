package com.bifrost.backend.infrastructure.adapter.output.persistence.jpa;

import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.TelemetrySampleJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetrySampleJpaRepository extends JpaRepository<TelemetrySampleJpaEntity, UUID> {
  Optional<TelemetrySampleJpaEntity> findFirstByRobotProfileIdAndMetricOrderByCapturedAtDesc(
      UUID robotProfileId, String metric);

  List<TelemetrySampleJpaEntity> findByOrderByCapturedAtDesc(Pageable pageable);

  List<TelemetrySampleJpaEntity> findByMetricOrderByCapturedAtDesc(String metric, Pageable pageable);

  List<TelemetrySampleJpaEntity> findByRobotProfileIdOrderByCapturedAtDesc(
      UUID robotProfileId, Pageable pageable);

  List<TelemetrySampleJpaEntity> findByMetricAndRobotProfileIdOrderByCapturedAtDesc(
      String metric, UUID robotProfileId, Pageable pageable);
}
