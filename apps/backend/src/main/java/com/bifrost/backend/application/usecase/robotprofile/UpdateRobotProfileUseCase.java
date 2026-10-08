package com.bifrost.backend.application.usecase.robotprofile;

import com.bifrost.backend.domain.enums.ProfileEnvironment;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateRobotProfileUseCase {
  private final RobotProfileRepository profileRepository;

  public UpdateRobotProfileUseCase(RobotProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  @Transactional
  public RobotProfile execute(
      UUID profileId,
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
      Boolean active) {
    RobotProfile existing =
        profileRepository
            .findById(profileId)
            .orElseThrow(() -> new NotFoundException("PROFILE_NOT_FOUND", "Robot profile not found"));

    RobotProfile patched =
        existing.applyPatch(
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
    return profileRepository.save(patched);
  }
}
