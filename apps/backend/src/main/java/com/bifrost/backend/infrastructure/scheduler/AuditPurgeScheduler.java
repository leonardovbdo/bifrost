package com.bifrost.backend.infrastructure.scheduler;

import com.bifrost.backend.application.usecase.audit.PurgeAuditEventsUseCase;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditPurgeScheduler {
  private static final Logger log = LoggerFactory.getLogger(AuditPurgeScheduler.class);

  private final PurgeAuditEventsUseCase purgeAuditEventsUseCase;
  private final BifrostProperties properties;

  public AuditPurgeScheduler(
      PurgeAuditEventsUseCase purgeAuditEventsUseCase, BifrostProperties properties) {
    this.purgeAuditEventsUseCase = purgeAuditEventsUseCase;
    this.properties = properties;
  }

  @Scheduled(cron = "${bifrost.audit.purge-cron:0 30 3 * * *}")
  public void purge() {
    int deleted = purgeAuditEventsUseCase.execute(properties.audit().retentionDays());
    if (deleted > 0) {
      log.info("Purged {} audit events older than {} days", deleted, properties.audit().retentionDays());
    }
  }
}
