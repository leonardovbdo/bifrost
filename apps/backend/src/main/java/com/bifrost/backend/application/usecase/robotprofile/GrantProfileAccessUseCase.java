package com.bifrost.backend.application.usecase.robotprofile;

import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrantProfileAccessUseCase {
  private final UserRepository userRepository;
  private final RobotProfileRepository profileRepository;
  private final UserProfileAccessRepository accessRepository;

  public GrantProfileAccessUseCase(
      UserRepository userRepository,
      RobotProfileRepository profileRepository,
      UserProfileAccessRepository accessRepository) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.accessRepository = accessRepository;
  }

  @Transactional
  public void execute(UUID profileId, UUID targetUserId) {
    if (userRepository.findById(targetUserId).filter(u -> u.active()).isEmpty()) {
      throw new NotFoundException("USER_NOT_FOUND", "User not found");
    }
    if (profileRepository.findById(profileId).isEmpty()) {
      throw new NotFoundException("PROFILE_NOT_FOUND", "Robot profile not found");
    }
    accessRepository.grant(targetUserId, profileId);
  }
}
