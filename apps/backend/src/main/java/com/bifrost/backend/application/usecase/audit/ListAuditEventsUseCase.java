package com.bifrost.backend.application.usecase.audit;

import com.bifrost.backend.domain.model.AuditEvent;
import com.bifrost.backend.domain.repository.AuditEventRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAuditEventsUseCase {
  private final AuditEventRepository auditEventRepository;

  public ListAuditEventsUseCase(AuditEventRepository auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
  }

  @Transactional(readOnly = true)
  public List<AuditEvent> execute(String type, UUID userId, int limit) {
    return auditEventRepository.findRecent(type, userId, limit <= 0 ? 50 : limit);
  }
}
