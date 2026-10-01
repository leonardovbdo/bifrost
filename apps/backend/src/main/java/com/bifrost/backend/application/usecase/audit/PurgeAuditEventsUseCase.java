package com.bifrost.backend.application.usecase.audit;

import com.bifrost.backend.domain.repository.AuditEventRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurgeAuditEventsUseCase {
  private final AuditEventRepository auditEventRepository;

  public PurgeAuditEventsUseCase(AuditEventRepository auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
  }

  @Transactional
  public int execute(int retentionDays) {
    Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
    return auditEventRepository.deleteOlderThan(cutoff);
  }
}
