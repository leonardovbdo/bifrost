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
@Table(name = "telemetry_samples")
public class TelemetrySampleJpaEntity {
  @Id
  private UUID id;

  @Column(name = "robot_profile_id", nullable = false)
  private UUID robotProfileId;

  @Column(nullable = false, length = 64)
  private String metric;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "value_json", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> valueJson = new HashMap<>();

  @Column(name = "captured_at", nullable = false)
  private Instant capturedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getRobotProfileId() {
    return robotProfileId;
  }

  public void setRobotProfileId(UUID robotProfileId) {
    this.robotProfileId = robotProfileId;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public Map<String, Object> getValueJson() {
    return valueJson;
  }

  public void setValueJson(Map<String, Object> valueJson) {
    this.valueJson = valueJson;
  }

  public Instant getCapturedAt() {
    return capturedAt;
  }

  public void setCapturedAt(Instant capturedAt) {
    this.capturedAt = capturedAt;
  }
}
