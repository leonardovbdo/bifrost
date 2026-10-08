package com.bifrost.backend.infrastructure.adapter.output.persistence.jpa;

import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.UserProfileAccessJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileAccessJpaRepository
    extends JpaRepository<UserProfileAccessJpaEntity, UserProfileAccessJpaEntity.Pk> {
  List<UserProfileAccessJpaEntity> findByUserId(UUID userId);

  boolean existsByUserIdAndRobotProfileId(UUID userId, UUID robotProfileId);
}
