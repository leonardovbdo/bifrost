package com.bifrost.backend.application.usecase.audit;

import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.AuditEvent;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.repository.UserProfileAccessRepository;
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

  public RecordClientAuditEventUseCase(
      AuditRecorder auditRecorder, UserProfileAccessRepository accessRepository) {
    this.auditRecorder = auditRecorder;
    this.accessRepository = accessRepository;
  }

  @Transactional
  public void execute(UUID userId, String type, UUID profileId, Map<String, Object> payload) {
    if (!ALLOWLIST.contains(type)) {
      throw new ValidationException("AUDIT_TYPE_FORBIDDEN", "Event type not allowed from client");
    }
    if (profileId != null && !accessRepository.hasAccess(userId, profileId)) {
      throw new ForbiddenException("PROFILE_FORBIDDEN", "Robot profile is not allowed for this user");
    }
    auditRecorder.record(type, userId, profileId, payload == null ? Map.of() : payload);
  }
}
