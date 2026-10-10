package com.bifrost.backend.domain.port.output;

import java.util.UUID;

public interface RequestRateLimiter {
  boolean allowLlmAsk(UUID userId);

  boolean allowClientGoalPoseAudit(UUID userId);

  /** Returns true at most once per LLM rate-limit window (audit slot for 429 responses). */
  boolean allowLlmRateLimitAudit(UUID userId);
}
