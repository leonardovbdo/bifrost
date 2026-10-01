package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.model.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditEventRepository {
  AuditEvent save(AuditEvent event);

  List<AuditEvent> findRecent(String type, UUID userId, int limit);

  int deleteOlderThan(Instant cutoff);
}
