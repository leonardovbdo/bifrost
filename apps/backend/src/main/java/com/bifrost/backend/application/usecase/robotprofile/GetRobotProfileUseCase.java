package com.bifrost.backend.application.usecase.robotprofile;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetRobotProfileUseCase {
  private final UserRepository userRepository;
  private final RobotProfileRepository profileRepository;
  private final UserProfileAccessRepository accessRepository;

  public GetRobotProfileUseCase(
      UserRepository userRepository,
      RobotProfileRepository profileRepository,
      UserProfileAccessRepository accessRepository) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.accessRepository = accessRepository;
  }

  @Transactional(readOnly = true)
  public RobotProfile execute(UUID requesterId, UUID profileId) {
    User user =
        userRepository
            .findById(requesterId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    RobotProfile profile =
        profileRepository
            .findById(profileId)
            .orElseThrow(() -> new NotFoundException("PROFILE_NOT_FOUND", "Robot profile not found"));

    if (user.role() == UserRole.ADMIN) {
      return profile;
    }

    if (!accessRepository.hasAccess(user.id(), profile.id())) {
      throw new ForbiddenException("PROFILE_FORBIDDEN", "Robot profile is not allowed for this user");
    }
    if (!profile.active()) {
      throw new NotFoundException("PROFILE_NOT_FOUND", "Robot profile not found");
    }
    return profile;
  }
}
