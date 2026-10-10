package com.bifrost.backend.application.usecase.audit;

import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.RateLimitExceededException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.port.output.RequestRateLimiter;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.domain.service.SessionPermissionResolver;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecordClientAuditEventUseCase {
  private static final Set<String> ALLOWLIST = Set.of("goal_pose");
  private static final Set<String> GOAL_POSE_FIELDS = Set.of("x", "y", "yaw");
  private static final int MAX_PAYLOAD_ENTRIES = 8;

  private final AuditRecorder auditRecorder;
  private final UserProfileAccessRepository accessRepository;
  private final UserRepository userRepository;
  private final RequestRateLimiter rateLimiter;

  public RecordClientAuditEventUseCase(
      AuditRecorder auditRecorder,
      UserProfileAccessRepository accessRepository,
      UserRepository userRepository,
      RequestRateLimiter rateLimiter) {
    this.auditRecorder = auditRecorder;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.rateLimiter = rateLimiter;
  }

  @Transactional
  public void execute(UUID userId, String type, UUID profileId, Map<String, Object> payload) {
    if (!ALLOWLIST.contains(type)) {
      throw new ValidationException("AUDIT_TYPE_FORBIDDEN", "Event type not allowed from client");
    }

    User user =
        userRepository
            .findById(userId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    Boolean canSendGoal = SessionPermissionResolver.resolve(user.role()).get("canSendGoal");
    if (!Boolean.TRUE.equals(canSendGoal)) {
      throw new ForbiddenException("AUDIT_FORBIDDEN", "Role cannot emit goal_pose events");
    }

    if (profileId != null && !accessRepository.hasAccess(userId, profileId)) {
      throw new ForbiddenException("PROFILE_FORBIDDEN", "Robot profile is not allowed for this user");
    }

    Map<String, Object> validated = validateGoalPosePayload(payload);

    if (!rateLimiter.allowClientGoalPoseAudit(userId)) {
      throw new RateLimitExceededException(
          "AUDIT_RATE_LIMIT", "Too many goal_pose audit events; try again later");
    }

    auditRecorder.record(type, userId, profileId, validated);
  }

  private Map<String, Object> validateGoalPosePayload(Map<String, Object> payload) {
    if (payload == null || payload.isEmpty()) {
      throw new ValidationException("AUDIT_PAYLOAD_INVALID", "goal_pose payload is required");
    }
    if (payload.size() > MAX_PAYLOAD_ENTRIES) {
      throw new ValidationException("AUDIT_PAYLOAD_INVALID", "goal_pose payload is too large");
    }
    for (Map.Entry<String, Object> entry : payload.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        throw new ValidationException("AUDIT_PAYLOAD_INVALID", "goal_pose payload rejects nulls");
      }
      if (!GOAL_POSE_FIELDS.contains(entry.getKey())) {
        throw new ValidationException(
            "AUDIT_PAYLOAD_INVALID", "goal_pose allows only x, y, yaw");
      }
    }
    Map<String, Object> clean = new LinkedHashMap<>();
    for (String field : GOAL_POSE_FIELDS) {
      Object raw = payload.get(field);
      if (!(raw instanceof Number number) || !Double.isFinite(number.doubleValue())) {
        throw new ValidationException(
            "AUDIT_PAYLOAD_INVALID", "goal_pose." + field + " must be a finite number");
      }
      clean.put(field, number.doubleValue());
    }
    return clean;
  }
}
