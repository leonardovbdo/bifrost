package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.UserProfileAccessJpaEntity;
import com.bifrost.backend.infrastructure.adapter.output.persistence.jpa.UserProfileAccessJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserProfileAccessRepositoryAdapter implements UserProfileAccessRepository {
  private final UserProfileAccessJpaRepository jpaRepository;

  public UserProfileAccessRepositoryAdapter(UserProfileAccessJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<UUID> findProfileIdsByUserId(UUID userId) {
    return jpaRepository.findByUserId(userId).stream()
        .map(UserProfileAccessJpaEntity::getRobotProfileId)
        .toList();
  }

  @Override
  public boolean hasAccess(UUID userId, UUID profileId) {
    return jpaRepository.existsByUserIdAndRobotProfileId(userId, profileId);
  }

  @Override
  public void grant(UUID userId, UUID profileId) {
    if (hasAccess(userId, profileId)) {
      return;
    }
    UserProfileAccessJpaEntity entity = new UserProfileAccessJpaEntity();
    entity.setUserId(userId);
    entity.setRobotProfileId(profileId);
    jpaRepository.save(entity);
  }
}
