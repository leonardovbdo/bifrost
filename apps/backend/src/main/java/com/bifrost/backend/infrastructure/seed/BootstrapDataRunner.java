package com.bifrost.backend.infrastructure.seed;

import com.bifrost.backend.domain.enums.ProfileEnvironment;
import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.PasswordHasher;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class BootstrapDataRunner implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(BootstrapDataRunner.class);
  public static final String NARA_SIM_SLUG = "nara-sim-alfa";

  private final BifrostProperties properties;
  private final RobotProfileRepository profileRepository;
  private final UserRepository userRepository;
  private final UserProfileAccessRepository accessRepository;
  private final PasswordHasher passwordHasher;

  public BootstrapDataRunner(
      BifrostProperties properties,
      RobotProfileRepository profileRepository,
      UserRepository userRepository,
      UserProfileAccessRepository accessRepository,
      PasswordHasher passwordHasher) {
    this.properties = properties;
    this.profileRepository = profileRepository;
    this.userRepository = userRepository;
    this.accessRepository = accessRepository;
    this.passwordHasher = passwordHasher;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    RobotProfile profile = ensureNaraSimProfile();
    ensureAdmin(profile);
  }

  private RobotProfile ensureNaraSimProfile() {
    return profileRepository
        .findBySlug(NARA_SIM_SLUG)
        .orElseGet(
            () -> {
              Instant now = Instant.now();
              Map<String, String> topics = new LinkedHashMap<>();
              topics.put("cmd_vel", "/noblenara/alfa/cmd_vel");
              topics.put("camera_link", "/noblenara/alfa/camera_link/image");
              topics.put("camera_user", "/noblenara/alfa/camera_user");
              topics.put("scan", "/noblenara/alfa/scan_filtered");
              topics.put("map", "/noblenara/alfa/map");
              topics.put("odom", "/noblenara/alfa/odom");
              topics.put("battery", "/noblenara/alfa/battery_status");
              topics.put("goal_pose", "/noblenara/alfa/goal_pose");

              Map<String, String> frames = new LinkedHashMap<>();
              frames.put("map", "map");
              frames.put("odom", "odom");
              frames.put("base", "base_link");
              frames.put("camera", "camera_link");

              RobotProfile created =
                  new RobotProfile(
                      UUID.randomUUID(),
                      NARA_SIM_SLUG,
                      "NARA Sim Alfa",
                      "noblenara",
                      "alfa",
                      ProfileEnvironment.SIM,
                      "wheelchair_nara",
                      List.of("teleop", "cameras", "slam", "nav2", "battery"),
                      topics,
                      frames,
                      "ws://localhost:9090",
                      "http://localhost:8080",
                      true,
                      now,
                      now);
              RobotProfile saved = profileRepository.save(created);
              log.info("Seeded robot profile {}", saved.slug());
              return saved;
            });
  }

  private void ensureAdmin(RobotProfile profile) {
    BifrostProperties.AdminSeed admin = properties.admin();
    if (!admin.enabled()) {
      return;
    }
    if (admin.username() == null
        || admin.username().isBlank()
        || admin.password() == null
        || admin.password().isBlank()) {
      log.warn(
          "Admin seed enabled but BIFROST_ADMIN_USERNAME/PASSWORD not set; skipping admin creation");
      return;
    }
    if (userRepository.existsByUsername(admin.username())) {
      userRepository
          .findByUsername(admin.username())
          .ifPresent(existing -> accessRepository.grant(existing.id(), profile.id()));
      return;
    }
    User user =
        User.create(
            admin.username(),
            null,
            passwordHasher.hash(admin.password()),
            UserRole.ADMIN);
    user.switchActiveProfile(profile.id());
    User saved = userRepository.save(user);
    accessRepository.grant(saved.id(), profile.id());
    log.info("Seeded admin user '{}'", saved.username());
  }
}
