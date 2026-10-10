package com.bifrost.backend.domain.model;

import com.bifrost.backend.domain.enums.UserRole;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class User {
  private final UUID id;
  private final String username;
  private final String email;
  private final String passwordHash;
  private final UserRole role;
  private final boolean active;
  private UUID lastActiveProfileId;
  private final Instant createdAt;
  private Instant updatedAt;

  public User(
      UUID id,
      String username,
      String email,
      String passwordHash,
      UserRole role,
      boolean active,
      UUID lastActiveProfileId,
      Instant createdAt,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id);
    this.username = Objects.requireNonNull(username);
    this.email = email;
    this.passwordHash = Objects.requireNonNull(passwordHash);
    this.role = Objects.requireNonNull(role);
    this.active = active;
    this.lastActiveProfileId = lastActiveProfileId;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static User create(String username, String email, String passwordHash, UserRole role) {
    return create(username, email, passwordHash, role, true);
  }

  public static User create(
      String username, String email, String passwordHash, UserRole role, boolean active) {
    Instant now = Instant.now();
    return new User(
        UUID.randomUUID(), username, email, passwordHash, role, active, null, now, now);
  }

  public User applyAdminPatch(Boolean active, UserRole role) {
    return new User(
        id,
        username,
        email,
        passwordHash,
        role != null ? role : this.role,
        active != null ? active : this.active,
        lastActiveProfileId,
        createdAt,
        Instant.now());
  }

  public UUID id() {
    return id;
  }

  public String username() {
    return username;
  }

  public String email() {
    return email;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public UserRole role() {
    return role;
  }

  public boolean active() {
    return active;
  }

  public UUID lastActiveProfileId() {
    return lastActiveProfileId;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public void switchActiveProfile(UUID profileId) {
    this.lastActiveProfileId = profileId;
    this.updatedAt = Instant.now();
  }
}
