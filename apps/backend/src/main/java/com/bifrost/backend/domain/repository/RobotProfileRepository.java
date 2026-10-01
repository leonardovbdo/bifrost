package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.model.RobotProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RobotProfileRepository {
  Optional<RobotProfile> findById(UUID id);

  Optional<RobotProfile> findBySlug(String slug);

  List<RobotProfile> findAll();

  List<RobotProfile> findActiveByIds(List<UUID> ids);

  List<RobotProfile> findByIds(List<UUID> ids);

  RobotProfile save(RobotProfile profile);

  boolean existsBySlug(String slug);
}
