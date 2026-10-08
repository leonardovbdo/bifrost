package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.RefreshToken;
import com.bifrost.backend.domain.repository.RefreshTokenRepository;
import com.bifrost.backend.infrastructure.adapter.output.persistence.jpa.RefreshTokenJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
  private final RefreshTokenJpaRepository jpaRepository;

  public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public RefreshToken save(RefreshToken token) {
    return RefreshTokenMapper.toDomain(jpaRepository.save(RefreshTokenMapper.toEntity(token)));
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return jpaRepository.findByTokenHash(tokenHash).map(RefreshTokenMapper::toDomain);
  }

  @Override
  @Transactional
  public void revokeAllForUser(UUID userId) {
    jpaRepository.revokeAllForUser(userId);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void revokeAllForUserNow(UUID userId) {
    jpaRepository.revokeAllForUser(userId);
  }

  @Override
  public boolean hasActiveSession(UUID userId) {
    return jpaRepository.existsByUserIdAndRevokedFalseAndExpiresAtAfter(userId, Instant.now());
  }
}
