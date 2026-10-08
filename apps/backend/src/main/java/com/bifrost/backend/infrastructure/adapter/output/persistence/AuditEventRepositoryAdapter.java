package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.AuditEvent;
import com.bifrost.backend.domain.repository.AuditEventRepository;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.AuditEventJpaEntity;
import com.bifrost.backend.infrastructure.adapter.output.persistence.jpa.AuditEventJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuditEventRepositoryAdapter implements AuditEventRepository {
  private final AuditEventJpaRepository jpaRepository;

  public AuditEventRepositoryAdapter(AuditEventJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public AuditEvent save(AuditEvent event) {
    return AuditEventMapper.toDomain(jpaRepository.save(AuditEventMapper.toEntity(event)));
  }

  @Override
  public List<AuditEvent> findRecent(String type, UUID userId, int limit) {
    var page = PageRequest.of(0, Math.max(1, Math.min(limit, 100)));
    List<AuditEventJpaEntity> rows;
    if (type != null && !type.isBlank() && userId != null) {
      rows = jpaRepository.findByTypeAndUserIdOrderByCreatedAtDesc(type, userId, page);
    } else if (type != null && !type.isBlank()) {
      rows = jpaRepository.findByTypeOrderByCreatedAtDesc(type, page);
    } else if (userId != null) {
      rows = jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, page);
    } else {
      rows = jpaRepository.findByOrderByCreatedAtDesc(page);
    }
    return rows.stream().map(AuditEventMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public int deleteOlderThan(Instant cutoff) {
    return jpaRepository.deleteByCreatedAtBefore(cutoff);
  }
}
