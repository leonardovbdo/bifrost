package com.bifrost.backend.infrastructure.security;

import com.bifrost.backend.infrastructure.config.BifrostProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieService {
  private final BifrostProperties properties;

  public AuthCookieService(BifrostProperties properties) {
    this.properties = properties;
  }

  public void writeAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
    // Drop legacy Path=/ cookies so they stop leaking to rosbridge/video on localhost.
    clearLegacyRootPathCookies(response);
    writeCookie(
        response,
        properties.cookie().accessName(),
        accessToken,
        properties.jwt().accessTtl());
    writeCookie(
        response,
        properties.cookie().refreshName(),
        refreshToken,
        properties.jwt().refreshTtl());
  }

  public void clearAuthCookies(HttpServletResponse response) {
    clearLegacyRootPathCookies(response);
    writeCookie(response, properties.cookie().accessName(), "", Duration.ZERO);
    writeCookie(response, properties.cookie().refreshName(), "", Duration.ZERO);
  }

  public String readCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  public String accessCookieName() {
    return properties.cookie().accessName();
  }

  public String refreshCookieName() {
    return properties.cookie().refreshName();
  }

  /** Expire pre-migration cookies so they stop reaching rosbridge/video on localhost. */
  public void clearLegacyRootPathCookies(HttpServletResponse response) {
    expireCookie(response, properties.cookie().accessName(), "/");
    expireCookie(response, properties.cookie().refreshName(), "/");
  }

  private void writeCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from(name, value == null ? "" : value)
            .httpOnly(true)
            .secure(properties.cookie().secure())
            // Scope to /api so localhost rosbridge (:9090) / video (:8080) do not receive JWTs.
            .path("/api")
            .sameSite(properties.cookie().sameSite());
    if (maxAge.isZero() || maxAge.isNegative()) {
      builder.maxAge(0);
    } else {
      builder.maxAge(maxAge);
    }
    response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
  }

  private void expireCookie(HttpServletResponse response, String name, String path) {
    ResponseCookie cookie =
        ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(properties.cookie().secure())
            .path(path)
            .sameSite(properties.cookie().sameSite())
            .maxAge(0)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
