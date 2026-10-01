package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.RefreshToken;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.RefreshTokenJpaEntity;

public final class RefreshTokenMapper {
  private RefreshTokenMapper() {}

  public static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
    return new RefreshToken(
        entity.getId(),
        entity.getUserId(),
        entity.getTokenHash(),
        entity.getExpiresAt(),
        entity.isRevoked(),
        entity.getCreatedAt());
  }

  public static RefreshTokenJpaEntity toEntity(RefreshToken token) {
    RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
    entity.setId(token.id());
    entity.setUserId(token.userId());
    entity.setTokenHash(token.tokenHash());
    entity.setExpiresAt(token.expiresAt());
    entity.setRevoked(token.revoked());
    entity.setCreatedAt(token.createdAt());
    return entity;
  }
}
