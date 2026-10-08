package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
  RefreshToken save(RefreshToken token);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  void revokeAllForUser(UUID userId);

  boolean hasActiveSession(UUID userId);
}
