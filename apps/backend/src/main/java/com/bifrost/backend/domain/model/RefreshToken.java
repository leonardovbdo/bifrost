package com.bifrost.backend.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RefreshToken {
  private final UUID id;
  private final UUID userId;
  private final String tokenHash;
  private final Instant expiresAt;
  private boolean revoked;
  private final Instant createdAt;

  public RefreshToken(
      UUID id, UUID userId, String tokenHash, Instant expiresAt, boolean revoked, Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.userId = Objects.requireNonNull(userId);
    this.tokenHash = Objects.requireNonNull(tokenHash);
    this.expiresAt = Objects.requireNonNull(expiresAt);
    this.revoked = revoked;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt) {
    return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, false, Instant.now());
  }

  public UUID id() {
    return id;
  }

  public UUID userId() {
    return userId;
  }

  public String tokenHash() {
    return tokenHash;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public boolean revoked() {
    return revoked;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public boolean isValid(Instant now) {
    return !revoked && expiresAt.isAfter(now);
  }

  public void revoke() {
    this.revoked = true;
  }
}
