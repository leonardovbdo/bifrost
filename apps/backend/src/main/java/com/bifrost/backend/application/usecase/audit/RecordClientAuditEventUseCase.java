package com.bifrost.backend.application.usecase.audit;

import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.domain.service.SessionPermissionResolver;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecordClientAuditEventUseCase {
  private static final Set<String> ALLOWLIST = Set.of("goal_pose");

  private final AuditRecorder auditRecorder;
  private final UserProfileAccessRepository accessRepository;
  private final UserRepository userRepository;

  public RecordClientAuditEventUseCase(
      AuditRecorder auditRecorder,
      UserProfileAccessRepository accessRepository,
      UserRepository userRepository) {
    this.auditRecorder = auditRecorder;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
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
    auditRecorder.record(type, userId, profileId, payload == null ? Map.of() : payload);
  }
}
