package com.bifrost.backend.domain.port.output;

import java.util.UUID;

public interface RequestRateLimiter {
  boolean allowLlmAsk(UUID userId);

  boolean allowClientGoalPoseAudit(UUID userId);
}
