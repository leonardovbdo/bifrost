package com.bifrost.backend.application.usecase.session;

import com.bifrost.backend.application.dto.AuthenticatedUserView;
import com.bifrost.backend.application.dto.SessionConfigView;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.TeleopLimitsProvider;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.domain.service.SessionPermissionResolver;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSessionConfigUseCase {
  private final UserRepository userRepository;
  private final UserProfileAccessRepository accessRepository;
  private final RobotProfileRepository profileRepository;
  private final TeleopLimitsProvider teleopLimitsProvider;

  public GetSessionConfigUseCase(
      UserRepository userRepository,
      UserProfileAccessRepository accessRepository,
      RobotProfileRepository profileRepository,
      TeleopLimitsProvider teleopLimitsProvider) {
    this.userRepository = userRepository;
    this.accessRepository = accessRepository;
    this.profileRepository = profileRepository;
    this.teleopLimitsProvider = teleopLimitsProvider;
  }

  @Transactional
  public SessionConfigView execute(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    List<UUID> allowedIds = accessRepository.findProfileIdsByUserId(user.id());
    List<RobotProfile> allowedProfiles =
        profileRepository.findActiveByIds(allowedIds).stream().filter(RobotProfile::active).toList();

    if (allowedProfiles.isEmpty()) {
      throw new NotFoundException("PROFILE_REQUIRED", "User has no active robot profiles");
    }

    RobotProfile active = resolveActive(user, allowedProfiles);
    if (user.lastActiveProfileId() == null || !user.lastActiveProfileId().equals(active.id())) {
      user.switchActiveProfile(active.id());
      userRepository.save(user);
    }

    List<Map<String, Object>> allowedSummaries = new ArrayList<>();
    for (RobotProfile profile : allowedProfiles) {
      Map<String, Object> summary = new LinkedHashMap<>();
      summary.put("id", profile.id().toString());
      summary.put("slug", profile.slug());
      summary.put("displayName", profile.displayName());
      allowedSummaries.add(summary);
    }

    Map<String, Object> limits = new LinkedHashMap<>();
    limits.put("teleop", teleopLimitsProvider.defaultTeleopLimits());

    return new SessionConfigView(
        1,
        new AuthenticatedUserView(user.id(), user.username(), user.role()),
        active.toSessionMap(),
        allowedSummaries,
        SessionPermissionResolver.resolve(user.role()),
        limits);
  }

  private RobotProfile resolveActive(User user, List<RobotProfile> allowedProfiles) {
    if (user.lastActiveProfileId() != null) {
      for (RobotProfile profile : allowedProfiles) {
        if (profile.id().equals(user.lastActiveProfileId())) {
          return profile;
        }
      }
    }
    return allowedProfiles.getFirst();
  }
}
