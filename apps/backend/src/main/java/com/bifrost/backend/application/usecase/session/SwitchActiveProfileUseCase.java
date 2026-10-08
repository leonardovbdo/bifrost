package com.bifrost.backend.application.usecase.session;

import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SwitchActiveProfileUseCase {
  private final UserRepository userRepository;
  private final UserProfileAccessRepository accessRepository;
  private final RobotProfileRepository profileRepository;
  private final AuditRecorder auditRecorder;

  public SwitchActiveProfileUseCase(
      UserRepository userRepository,
      UserProfileAccessRepository accessRepository,
      RobotProfileRepository profileRepository,
      AuditRecorder auditRecorder) {
    this.userRepository = userRepository;
    this.accessRepository = accessRepository;
    this.profileRepository = profileRepository;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public void execute(UUID userId, UUID profileId) {
    User user =
        userRepository
            .findById(userId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    RobotProfile profile =
        profileRepository
            .findById(profileId)
            .filter(RobotProfile::active)
            .orElseThrow(() -> new NotFoundException("PROFILE_NOT_FOUND", "Robot profile not found"));

    if (!accessRepository.hasAccess(user.id(), profile.id())) {
      throw new ForbiddenException("PROFILE_FORBIDDEN", "Robot profile is not allowed for this user");
    }

    user.switchActiveProfile(profile.id());
    userRepository.save(user);
    auditRecorder.record(
        "profile_switch",
        user.id(),
        profile.id(),
        Map.of("profileId", profile.id().toString(), "slug", profile.slug()));
  }
}
