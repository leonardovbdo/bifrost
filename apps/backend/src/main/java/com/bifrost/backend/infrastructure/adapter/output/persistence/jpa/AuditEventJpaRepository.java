package com.bifrost.backend.infrastructure.adapter.output.persistence.jpa;

import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.AuditEventJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, UUID> {
  List<AuditEventJpaEntity> findByOrderByCreatedAtDesc(Pageable pageable);

  List<AuditEventJpaEntity> findByTypeOrderByCreatedAtDesc(String type, Pageable pageable);

  List<AuditEventJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  List<AuditEventJpaEntity> findByTypeAndUserIdOrderByCreatedAtDesc(
      String type, UUID userId, Pageable pageable);

  @Modifying
  @Query("delete from AuditEventJpaEntity e where e.createdAt < :cutoff")
  int deleteByCreatedAtBefore(@Param("cutoff") Instant cutoff);
}
