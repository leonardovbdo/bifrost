package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.TelemetrySample;
import com.bifrost.backend.domain.repository.TelemetrySampleRepository;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.TelemetrySampleJpaEntity;
import com.bifrost.backend.infrastructure.adapter.output.persistence.jpa.TelemetrySampleJpaRepository;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.Session;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class TelemetrySampleRepositoryAdapter implements TelemetrySampleRepository {
  private final TelemetrySampleJpaRepository jpaRepository;
  private final EntityManager entityManager;

  public TelemetrySampleRepositoryAdapter(
      TelemetrySampleJpaRepository jpaRepository, EntityManager entityManager) {
    this.jpaRepository = jpaRepository;
    this.entityManager = entityManager;
  }

  @Override
  public void lockProfileMetric(UUID robotProfileId, String metric) {
    long key = advisoryKey(robotProfileId, metric);
    entityManager.unwrap(Session.class).doWork(connection -> {
      try (PreparedStatement ps =
          connection.prepareStatement("SELECT pg_advisory_xact_lock(?)")) {
        ps.setLong(1, key);
        try (ResultSet rs = ps.executeQuery()) {
          rs.next();
        }
      }
    });
  }

  private static long advisoryKey(UUID robotProfileId, String metric) {
    String token = robotProfileId + "\0" + metric;
    return UUID.nameUUIDFromBytes(token.getBytes(StandardCharsets.UTF_8)).getMostSignificantBits();
  }

  @Override
  public TelemetrySample save(TelemetrySample sample) {
    return TelemetrySampleMapper.toDomain(jpaRepository.save(TelemetrySampleMapper.toEntity(sample)));
  }

  @Override
  public Optional<TelemetrySample> findLatest(UUID robotProfileId, String metric) {
    return jpaRepository
        .findFirstByRobotProfileIdAndMetricOrderByCapturedAtDesc(robotProfileId, metric)
        .map(TelemetrySampleMapper::toDomain);
  }

  @Override
  public List<TelemetrySample> findRecent(String metric, UUID robotProfileId, int limit) {
    var page = PageRequest.of(0, Math.max(1, Math.min(limit, 100)));
    List<TelemetrySampleJpaEntity> rows;
    boolean hasMetric = metric != null && !metric.isBlank();
    if (hasMetric && robotProfileId != null) {
      rows =
          jpaRepository.findByMetricAndRobotProfileIdOrderByCapturedAtDesc(
              metric, robotProfileId, page);
    } else if (hasMetric) {
      rows = jpaRepository.findByMetricOrderByCapturedAtDesc(metric, page);
    } else if (robotProfileId != null) {
      rows = jpaRepository.findByRobotProfileIdOrderByCapturedAtDesc(robotProfileId, page);
    } else {
      rows = jpaRepository.findByOrderByCapturedAtDesc(page);
    }
    return rows.stream().map(TelemetrySampleMapper::toDomain).toList();
  }
}
