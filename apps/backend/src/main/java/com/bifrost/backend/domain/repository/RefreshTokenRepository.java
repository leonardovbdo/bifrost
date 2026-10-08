package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
  RefreshToken save(RefreshToken token);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  void revokeAllForUser(UUID userId);

  /**
   * Revoke in a committed nested transaction. Use when the caller will throw
   * (refresh reuse) so the revoke survives rollback of the outer tx.
   */
  void revokeAllForUserNow(UUID userId);

  boolean hasActiveSession(UUID userId);
}
