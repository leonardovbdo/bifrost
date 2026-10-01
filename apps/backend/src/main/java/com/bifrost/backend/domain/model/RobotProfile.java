package com.bifrost.backend.domain.model;

import com.bifrost.backend.domain.enums.ProfileEnvironment;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RobotProfile {
  private final UUID id;
  private final String slug;
  private final String displayName;
  private final String project;
  private final String prefix;
  private final ProfileEnvironment environment;
  private final String technology;
  private final List<String> capabilities;
  private final Map<String, String> topics;
  private final Map<String, String> frames;
  private final String rosbridgeUrl;
  private final String videoBaseUrl;
  private final boolean active;
  private final Instant createdAt;
  private final Instant updatedAt;

  public RobotProfile(
      UUID id,
      String slug,
      String displayName,
      String project,
      String prefix,
      ProfileEnvironment environment,
      String technology,
      List<String> capabilities,
      Map<String, String> topics,
      Map<String, String> frames,
      String rosbridgeUrl,
      String videoBaseUrl,
      boolean active,
      Instant createdAt,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id);
    this.slug = Objects.requireNonNull(slug);
    this.displayName = Objects.requireNonNull(displayName);
    this.project = Objects.requireNonNull(project);
    this.prefix = Objects.requireNonNull(prefix);
    this.environment = Objects.requireNonNull(environment);
    this.technology = Objects.requireNonNull(technology);
    this.capabilities = List.copyOf(capabilities);
    this.topics = Map.copyOf(topics);
    this.frames = Map.copyOf(frames);
    this.rosbridgeUrl = Objects.requireNonNull(rosbridgeUrl);
    this.videoBaseUrl = Objects.requireNonNull(videoBaseUrl);
    this.active = active;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public UUID id() {
    return id;
  }

  public String slug() {
    return slug;
  }

  public String displayName() {
    return displayName;
  }

  public String project() {
    return project;
  }

  public String prefix() {
    return prefix;
  }

  public ProfileEnvironment environment() {
    return environment;
  }

  public String technology() {
    return technology;
  }

  public List<String> capabilities() {
    return capabilities;
  }

  public Map<String, String> topics() {
    return topics;
  }

  public Map<String, String> frames() {
    return frames;
  }

  public String rosbridgeUrl() {
    return rosbridgeUrl;
  }

  public String videoBaseUrl() {
    return videoBaseUrl;
  }

  public boolean active() {
    return active;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public Map<String, Object> toSessionMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", id.toString());
    map.put("slug", slug);
    map.put("displayName", displayName);
    map.put("project", project);
    map.put("prefix", prefix);
    map.put("environment", environment.dbValue());
    map.put("technology", technology);
    map.put("capabilities", capabilities);
    map.put("topics", topics);
    map.put("frames", frames);
    map.put("rosbridgeUrl", rosbridgeUrl);
    map.put("videoBaseUrl", videoBaseUrl);
    return map;
  }
}
