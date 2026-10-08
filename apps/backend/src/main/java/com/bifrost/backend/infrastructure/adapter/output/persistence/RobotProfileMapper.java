package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.enums.ProfileEnvironment;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.RobotProfileJpaEntity;
import java.util.ArrayList;
import java.util.HashMap;

public final class RobotProfileMapper {
  private RobotProfileMapper() {}

  public static RobotProfile toDomain(RobotProfileJpaEntity entity) {
    return new RobotProfile(
        entity.getId(),
        entity.getSlug(),
        entity.getDisplayName(),
        entity.getProject(),
        entity.getPrefix(),
        ProfileEnvironment.fromDb(entity.getEnvironment()),
        entity.getTechnology(),
        entity.getCapabilities(),
        entity.getTopics(),
        entity.getFrames(),
        entity.getRosbridgeUrl(),
        entity.getVideoBaseUrl(),
        entity.isActive(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static RobotProfileJpaEntity toEntity(RobotProfile profile) {
    RobotProfileJpaEntity entity = new RobotProfileJpaEntity();
    entity.setId(profile.id());
    entity.setSlug(profile.slug());
    entity.setDisplayName(profile.displayName());
    entity.setProject(profile.project());
    entity.setPrefix(profile.prefix());
    entity.setEnvironment(profile.environment().dbValue());
    entity.setTechnology(profile.technology());
    entity.setCapabilities(new ArrayList<>(profile.capabilities()));
    entity.setTopics(new HashMap<>(profile.topics()));
    entity.setFrames(new HashMap<>(profile.frames()));
    entity.setRosbridgeUrl(profile.rosbridgeUrl());
    entity.setVideoBaseUrl(profile.videoBaseUrl());
    entity.setActive(profile.active());
    entity.setCreatedAt(profile.createdAt());
    entity.setUpdatedAt(profile.updatedAt());
    return entity;
  }
}
