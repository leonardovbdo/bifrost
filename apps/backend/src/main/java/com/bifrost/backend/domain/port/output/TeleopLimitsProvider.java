package com.bifrost.backend.domain.port.output;

import java.util.Map;
import java.util.UUID;

public interface TeleopLimitsProvider {
  Map<String, Object> resolveForUser(UUID userId);
}
