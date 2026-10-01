package com.bifrost.backend.domain.repository;

import java.util.List;
import java.util.UUID;

public interface UserProfileAccessRepository {
  List<UUID> findProfileIdsByUserId(UUID userId);

  boolean hasAccess(UUID userId, UUID profileId);

  void grant(UUID userId, UUID profileId);
}
