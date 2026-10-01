package com.bifrost.backend.infrastructure.security;

import com.bifrost.backend.domain.exception.AuthenticationFailedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
  private SecurityUtils() {}

  public static UUID currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
      throw new AuthenticationFailedException();
    }
    return userId;
  }
}
