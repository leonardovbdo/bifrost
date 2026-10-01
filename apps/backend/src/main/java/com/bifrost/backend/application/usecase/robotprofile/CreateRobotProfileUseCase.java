package com.bifrost.backend.application.usecase.robotprofile;

import com.bifrost.backend.domain.enums.ProfileEnvironment;
import com.bifrost.backend.domain.exception.ConflictException;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRobotProfileUseCase {
  private final RobotProfileRepository profileRepository;
  private final UserProfileAccessRepository accessRepository;

  public CreateRobotProfileUseCase(
      RobotProfileRepository profileRepository, UserProfileAccessRepository accessRepository) {
    this.profileRepository = profileRepository;
    this.accessRepository = accessRepository;
  }

  @Transactional
  public RobotProfile execute(
      UUID adminUserId,
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
      boolean grantAccessToCreator) {
    if (profileRepository.existsBySlug(slug)) {
      throw new ConflictException("PROFILE_SLUG_EXISTS", "Robot profile slug already exists");
    }
    RobotProfile created =
        RobotProfile.create(
            slug,
            displayName,
            project,
            prefix,
            environment,
            technology,
            capabilities,
            topics,
            frames,
            rosbridgeUrl,
            videoBaseUrl,
            active);
    RobotProfile saved = profileRepository.save(created);
    if (grantAccessToCreator) {
      accessRepository.grant(adminUserId, saved.id());
    }
    return saved;
  }
}
