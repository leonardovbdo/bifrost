package com.bifrost.backend.application.usecase.robotprofile;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListRobotProfilesUseCase {
  private final UserRepository userRepository;
  private final RobotProfileRepository profileRepository;
  private final UserProfileAccessRepository accessRepository;

  public ListRobotProfilesUseCase(
      UserRepository userRepository,
      RobotProfileRepository profileRepository,
      UserProfileAccessRepository accessRepository) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.accessRepository = accessRepository;
  }

  @Transactional(readOnly = true)
  public List<RobotProfile> execute(UUID requesterId) {
    User user =
        userRepository
            .findById(requesterId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    if (user.role() == UserRole.ADMIN) {
      return profileRepository.findAll();
    }

    List<UUID> allowed = accessRepository.findProfileIdsByUserId(user.id());
    return profileRepository.findActiveByIds(allowed);
  }
}
