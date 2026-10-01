package com.bifrost.backend.domain.model;

import com.bifrost.backend.domain.enums.ParameterScope;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Parameter {
  private final UUID id;
  private final ParameterScope scope;
  private final UUID scopeId;
  private final String key;
  private final Map<String, Object> value;
  private final Instant createdAt;
  private final Instant updatedAt;

  public Parameter(
      UUID id,
      ParameterScope scope,
      UUID scopeId,
      String key,
      Map<String, Object> value,
      Instant createdAt,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id);
    this.scope = Objects.requireNonNull(scope);
    this.scopeId = scopeId;
    this.key = Objects.requireNonNull(key);
    this.value = Map.copyOf(value);
    this.createdAt = Objects.requireNonNull(createdAt);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static Parameter create(ParameterScope scope, UUID scopeId, String key, Map<String, Object> value) {
    Instant now = Instant.now();
    return new Parameter(UUID.randomUUID(), scope, scopeId, key, value, now, now);
  }

  public Parameter withValue(Map<String, Object> newValue) {
    return new Parameter(id, scope, scopeId, key, newValue, createdAt, Instant.now());
  }

  public UUID id() { return id; }
  public ParameterScope scope() { return scope; }
  public UUID scopeId() { return scopeId; }
  public String key() { return key; }
  public Map<String, Object> value() { return value; }
  public Instant createdAt() { return createdAt; }
  public Instant updatedAt() { return updatedAt; }
}
