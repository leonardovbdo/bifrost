package com.bifrost.backend.infrastructure.adapter.output.persistence.jpa;

import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.RefreshTokenJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
  Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

  @Modifying
  @Query("update RefreshTokenJpaEntity r set r.revoked = true where r.userId = :userId and r.revoked = false")
  int revokeAllForUser(@Param("userId") UUID userId);

  boolean existsByUserIdAndRevokedFalseAndExpiresAtAfter(UUID userId, Instant now);
}
