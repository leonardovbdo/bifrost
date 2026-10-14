package com.bifrost.backend.application.usecase.telemetry;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.RateLimitExceededException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.TelemetrySample;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.repository.TelemetrySampleRepository;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestTelemetrySampleUseCase {
  private static final Set<String> ALLOWED_METRICS = Set.of("battery", "pose");
  private static final Set<String> FORBIDDEN_METRICS = Set.of("cmd_vel", "cmd_vel_stamped");
  private static final int MAX_VALUE_ENTRIES = 16;

  private final TelemetrySampleRepository telemetrySampleRepository;
  private final RobotProfileRepository robotProfileRepository;
  private final UserProfileAccessRepository accessRepository;
  private final UserRepository userRepository;
  private final BifrostProperties properties;

  public IngestTelemetrySampleUseCase(
      TelemetrySampleRepository telemetrySampleRepository,
      RobotProfileRepository robotProfileRepository,
      UserProfileAccessRepository accessRepository,
      UserRepository userRepository,
      BifrostProperties properties) {
    this.telemetrySampleRepository = telemetrySampleRepository;
    this.robotProfileRepository = robotProfileRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.properties = properties;
  }

  @Transactional
  public TelemetrySample execute(
      UUID userId, UUID robotProfileId, String metric, Map<String, Object> value) {
    User user =
        userRepository
            .findById(userId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    if (user.role() == UserRole.VIEWER) {
      throw new ForbiddenException("TELEMETRY_FORBIDDEN", "Role cannot ingest telemetry samples");
    }

    if (robotProfileId == null) {
      throw new ValidationException("TELEMETRY_PROFILE_REQUIRED", "robotProfileId is required");
    }

    robotProfileRepository
        .findById(robotProfileId)
        .orElseThrow(() -> new NotFoundException("PROFILE_NOT_FOUND", "Robot profile not found"));

    if (!accessRepository.hasAccess(userId, robotProfileId)) {
      throw new ForbiddenException("PROFILE_FORBIDDEN", "Robot profile is not allowed for this user");
    }

    String normalizedMetric = normalizeMetric(metric);
    Map<String, Object> validated = validateValue(normalizedMetric, value);

    Duration minInterval = properties.telemetry().minSampleInterval();
    if (!minInterval.isZero() && !minInterval.isNegative()) {
      telemetrySampleRepository
          .findLatest(robotProfileId, normalizedMetric)
          .ifPresent(
              latest -> {
                Instant earliestNext = latest.capturedAt().plus(minInterval);
                if (Instant.now().isBefore(earliestNext)) {
                  throw new RateLimitExceededException(
                      "TELEMETRY_SAMPLE_TOO_FREQUENT",
                      "Sample interval too short for metric " + normalizedMetric + "; try again later");
                }
              });
    }

    return telemetrySampleRepository.save(
        TelemetrySample.create(robotProfileId, normalizedMetric, validated));
  }

  private String normalizeMetric(String metric) {
    if (metric == null || metric.isBlank()) {
      throw new ValidationException("TELEMETRY_METRIC_INVALID", "metric is required");
    }
    String trimmed = metric.trim().toLowerCase();
    if (FORBIDDEN_METRICS.contains(trimmed)) {
      throw new ValidationException(
          "TELEMETRY_METRIC_FORBIDDEN", "Continuous teleop metrics are not persisted");
    }
    if (!ALLOWED_METRICS.contains(trimmed)) {
      throw new ValidationException(
          "TELEMETRY_METRIC_FORBIDDEN", "metric must be one of: battery, pose");
    }
    return trimmed;
  }

  private Map<String, Object> validateValue(String metric, Map<String, Object> value) {
    if (value == null || value.isEmpty()) {
      throw new ValidationException("TELEMETRY_VALUE_INVALID", "value is required");
    }
    if (value.size() > MAX_VALUE_ENTRIES) {
      throw new ValidationException("TELEMETRY_VALUE_INVALID", "value has too many entries");
    }
    for (Map.Entry<String, Object> entry : value.entrySet()) {
      if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
        throw new ValidationException("TELEMETRY_VALUE_INVALID", "value rejects nulls/blank keys");
      }
    }

    return switch (metric) {
      case "battery" -> validateBattery(value);
      case "pose" -> validatePose(value);
      default -> throw new ValidationException("TELEMETRY_METRIC_FORBIDDEN", "Unsupported metric");
    };
  }

  private Map<String, Object> validateBattery(Map<String, Object> value) {
    Object percentage = value.get("percentage");
    Object voltage = value.get("voltage");
    if (percentage == null && voltage == null) {
      throw new ValidationException(
          "TELEMETRY_VALUE_INVALID", "battery requires percentage and/or voltage");
    }
    Map<String, Object> clean = new LinkedHashMap<>();
    if (percentage != null) {
      if (!(percentage instanceof Number number) || !Double.isFinite(number.doubleValue())) {
        throw new ValidationException(
            "TELEMETRY_VALUE_INVALID", "battery.percentage must be a finite number");
      }
      double pct = number.doubleValue();
      if (pct < 0 || pct > 100) {
        throw new ValidationException(
            "TELEMETRY_VALUE_INVALID", "battery.percentage must be between 0 and 100");
      }
      clean.put("percentage", pct);
    }
    if (voltage != null) {
      if (!(voltage instanceof Number number) || !Double.isFinite(number.doubleValue())) {
        throw new ValidationException(
            "TELEMETRY_VALUE_INVALID", "battery.voltage must be a finite number");
      }
      clean.put("voltage", number.doubleValue());
    }
    return clean;
  }

  private Map<String, Object> validatePose(Map<String, Object> value) {
    Map<String, Object> clean = new LinkedHashMap<>();
    for (String field : List.of("x", "y")) {
      Object raw = value.get(field);
      if (!(raw instanceof Number number) || !Double.isFinite(number.doubleValue())) {
        throw new ValidationException(
            "TELEMETRY_VALUE_INVALID", "pose." + field + " must be a finite number");
      }
      clean.put(field, number.doubleValue());
    }
    Object yaw = value.get("yaw");
    if (yaw != null) {
      if (!(yaw instanceof Number number) || !Double.isFinite(number.doubleValue())) {
        throw new ValidationException(
            "TELEMETRY_VALUE_INVALID", "pose.yaw must be a finite number");
      }
      clean.put("yaw", number.doubleValue());
    }
    return clean;
  }
}
