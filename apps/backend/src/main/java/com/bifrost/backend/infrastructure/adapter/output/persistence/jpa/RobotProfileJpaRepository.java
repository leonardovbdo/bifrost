package com.bifrost.backend.infrastructure.adapter.output.persistence.jpa;

import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.RobotProfileJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotProfileJpaRepository extends JpaRepository<RobotProfileJpaEntity, UUID> {
  Optional<RobotProfileJpaEntity> findBySlug(String slug);

  boolean existsBySlug(String slug);

  List<RobotProfileJpaEntity> findByIdInAndActiveTrue(Collection<UUID> ids);
}
