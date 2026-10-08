package com.bifrost.backend.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "robot_profiles")
public class RobotProfileJpaEntity {
  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(name = "display_name", nullable = false, length = 200)
  private String displayName;

  @Column(nullable = false, length = 100)
  private String project;

  @Column(nullable = false, length = 100)
  private String prefix;

  @Column(nullable = false, length = 20)
  private String environment;

  @Column(nullable = false, length = 100)
  private String technology;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private List<String> capabilities = new ArrayList<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, String> topics = new HashMap<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, String> frames = new HashMap<>();

  @Column(name = "rosbridge_url", nullable = false, length = 500)
  private String rosbridgeUrl;

  @Column(name = "video_base_url", nullable = false, length = 500)
  private String videoBaseUrl;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getSlug() { return slug; }
  public void setSlug(String slug) { this.slug = slug; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public String getProject() { return project; }
  public void setProject(String project) { this.project = project; }
  public String getPrefix() { return prefix; }
  public void setPrefix(String prefix) { this.prefix = prefix; }
  public String getEnvironment() { return environment; }
  public void setEnvironment(String environment) { this.environment = environment; }
  public String getTechnology() { return technology; }
  public void setTechnology(String technology) { this.technology = technology; }
  public List<String> getCapabilities() { return capabilities; }
  public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
  public Map<String, String> getTopics() { return topics; }
  public void setTopics(Map<String, String> topics) { this.topics = topics; }
  public Map<String, String> getFrames() { return frames; }
  public void setFrames(Map<String, String> frames) { this.frames = frames; }
  public String getRosbridgeUrl() { return rosbridgeUrl; }
  public void setRosbridgeUrl(String rosbridgeUrl) { this.rosbridgeUrl = rosbridgeUrl; }
  public String getVideoBaseUrl() { return videoBaseUrl; }
  public void setVideoBaseUrl(String videoBaseUrl) { this.videoBaseUrl = videoBaseUrl; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
