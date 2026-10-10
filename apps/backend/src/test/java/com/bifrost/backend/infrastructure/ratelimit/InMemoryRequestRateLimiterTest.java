package com.bifrost.backend.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.bifrost.backend.infrastructure.config.BifrostProperties;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryRequestRateLimiterTest {

  @Test
  void enforcesMaxRequestsPerWindow() {
    BifrostProperties properties = new BifrostProperties();
    properties.llm().rateLimit().setMaxRequests(2);
    properties.llm().rateLimit().setWindow(Duration.ofMinutes(1));
    InMemoryRequestRateLimiter limiter = new InMemoryRequestRateLimiter(properties);
    UUID userId = UUID.randomUUID();

    assertThat(limiter.tryConsume(userId, "test", 2, Duration.ofMinutes(1))).isTrue();
    assertThat(limiter.tryConsume(userId, "test", 2, Duration.ofMinutes(1))).isTrue();
    assertThat(limiter.tryConsume(userId, "test", 2, Duration.ofMinutes(1))).isFalse();
  }
}
