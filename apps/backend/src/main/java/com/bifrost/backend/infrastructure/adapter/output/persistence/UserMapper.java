package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.UserJpaEntity;

public final class UserMapper {
  private UserMapper() {}

  public static User toDomain(UserJpaEntity entity) {
    return new User(
        entity.getId(),
        entity.getUsername(),
        entity.getEmail(),
        entity.getPasswordHash(),
        UserRole.fromDb(entity.getRole()),
        entity.isActive(),
        entity.getLastActiveProfileId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static UserJpaEntity toEntity(User user) {
    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(user.id());
    entity.setUsername(user.username());
    entity.setEmail(user.email());
    entity.setPasswordHash(user.passwordHash());
    entity.setRole(user.role().dbValue());
    entity.setActive(user.active());
    entity.setLastActiveProfileId(user.lastActiveProfileId());
    entity.setCreatedAt(user.createdAt());
    entity.setUpdatedAt(user.updatedAt());
    return entity;
  }
}
