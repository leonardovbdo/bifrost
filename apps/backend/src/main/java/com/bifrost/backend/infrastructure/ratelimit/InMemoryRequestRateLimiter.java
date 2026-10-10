package com.bifrost.backend.infrastructure.ratelimit;

import com.bifrost.backend.domain.port.output.RequestRateLimiter;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRequestRateLimiter implements RequestRateLimiter {
  private record WindowKey(UUID userId, String scope, long bucket, long windowMillis) {}

  private final BifrostProperties properties;
  private final ConcurrentHashMap<WindowKey, AtomicInteger> counters = new ConcurrentHashMap<>();

  public InMemoryRequestRateLimiter(BifrostProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean allowLlmAsk(UUID userId) {
    var limit = properties.llm().rateLimit();
    return tryConsume(userId, "llm_ask", limit.maxRequests(), limit.window());
  }

  @Override
  public boolean allowClientGoalPoseAudit(UUID userId) {
    var limit = properties.audit().clientEvents();
    return tryConsume(userId, "audit_goal_pose", limit.maxGoalPosePerWindow(), limit.window());
  }

  @Override
  public boolean allowLlmRateLimitAudit(UUID userId) {
    var limit = properties.llm().rateLimit();
    return tryConsume(userId, "llm_rate_limit_audit", 1, limit.window());
  }

  boolean tryConsume(UUID userId, String scope, int maxRequests, Duration window) {
    if (maxRequests <= 0) {
      return true;
    }
    long windowMillis = Math.max(1L, window.toMillis());
    long bucket = System.currentTimeMillis() / windowMillis;
    WindowKey key = new WindowKey(userId, scope, bucket, windowMillis);
    AtomicInteger counter = counters.computeIfAbsent(key, ignored -> new AtomicInteger(0));
    int attempt = counter.incrementAndGet();
    if (attempt > maxRequests) {
      counter.decrementAndGet();
      return false;
    }
    pruneIfNeeded();
    return true;
  }

  private void pruneIfNeeded() {
    if (counters.size() <= 10_000) {
      return;
    }
    long now = System.currentTimeMillis();
    for (Map.Entry<WindowKey, AtomicInteger> entry : counters.entrySet()) {
      WindowKey key = entry.getKey();
      if (isBucketExpired(key.bucket(), key.windowMillis(), now)) {
        counters.remove(key, entry.getValue());
      }
    }
  }

  static boolean isBucketExpired(long bucket, long windowMillis, long nowMillis) {
    return (bucket + 1L) * windowMillis <= nowMillis;
  }
}
