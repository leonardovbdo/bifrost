package com.bifrost.backend.infrastructure.adapter.output.security;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.AuthenticationFailedException;
import com.bifrost.backend.domain.port.output.TokenProvider;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {
  static final String PUBLIC_PLACEHOLDER_SECRET = "change-me-bifrost-dev-secret-at-least-32-chars";

  private final BifrostProperties properties;
  private final SecretKey key;

  public JwtTokenProvider(BifrostProperties properties, Environment environment) {
    this.properties = properties;
    String secret = properties.jwt().secret();
    boolean allowDevPlaceholder =
        Arrays.asList(environment.getActiveProfiles()).contains("dev");
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException(
          "bifrost.jwt.secret (BIFROST_JWT_SECRET) must be set (≥32 chars)");
    }
    if (PUBLIC_PLACEHOLDER_SECRET.equals(secret) && !allowDevPlaceholder) {
      throw new IllegalStateException(
          "bifrost.jwt.secret is the public placeholder; set a unique BIFROST_JWT_SECRET "
              + "or activate spring profile 'dev'");
    }
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < 32) {
      throw new IllegalStateException("bifrost.jwt.secret must be at least 32 characters");
    }
    this.key = Keys.hmacShaKeyFor(secretBytes);
  }

  @Override
  public String createAccessToken(UUID userId, String username, UserRole role) {
    Instant now = Instant.now();
    Instant exp = now.plus(properties.jwt().accessTtl());
    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .claim("role", role.dbValue())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  @Override
  public AccessTokenClaims parseAccessToken(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      return new AccessTokenClaims(
          UUID.fromString(claims.getSubject()),
          claims.get("username", String.class),
          UserRole.fromDb(claims.get("role", String.class)));
    } catch (Exception ex) {
      throw new AuthenticationFailedException();
    }
  }

  @Override
  public String createRefreshTokenValue() {
    return UUID.randomUUID() + "." + UUID.randomUUID();
  }

  @Override
  public String hashRefreshToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public Instant refreshExpiresAt() {
    return Instant.now().plus(properties.jwt().refreshTtl());
  }
}
