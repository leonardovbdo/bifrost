package com.bifrost.backend.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "parameters")
public class ParameterJpaEntity {
  @Id
  private UUID id;

  @Column(nullable = false, length = 20)
  private String scope;

  @Column(name = "scope_id")
  private UUID scopeId;

  @Column(name = "key", nullable = false, length = 200)
  private String key;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "value_json", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> valueJson = new HashMap<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getScope() { return scope; }
  public void setScope(String scope) { this.scope = scope; }
  public UUID getScopeId() { return scopeId; }
  public void setScopeId(UUID scopeId) { this.scopeId = scopeId; }
  public String getKey() { return key; }
  public void setKey(String key) { this.key = key; }
  public Map<String, Object> getValueJson() { return valueJson; }
  public void setValueJson(Map<String, Object> valueJson) { this.valueJson = valueJson; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
