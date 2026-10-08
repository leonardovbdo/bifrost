package com.bifrost.backend.domain.port.output;

import java.util.Map;
import java.util.UUID;

public interface AuditRecorder {
  void record(String type, UUID userId, UUID robotProfileId, Map<String, Object> payload);
}
