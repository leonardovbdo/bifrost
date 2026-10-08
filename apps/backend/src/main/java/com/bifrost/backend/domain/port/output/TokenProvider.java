package com.bifrost.backend.domain.port.output;

import com.bifrost.backend.domain.enums.UserRole;
import java.time.Instant;
import java.util.UUID;

public interface TokenProvider {
  String createAccessToken(UUID userId, String username, UserRole role);

  AccessTokenClaims parseAccessToken(String token);

  String createRefreshTokenValue();

  String hashRefreshToken(String rawToken);

  Instant refreshExpiresAt();

  record AccessTokenClaims(UUID userId, String username, UserRole role) {}
}
