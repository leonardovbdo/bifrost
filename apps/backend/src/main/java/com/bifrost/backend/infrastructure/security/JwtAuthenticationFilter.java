package com.bifrost.backend.infrastructure.security;

import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.TokenProvider;
import com.bifrost.backend.domain.repository.RefreshTokenRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final TokenProvider tokenProvider;
  private final AuthCookieService cookieService;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  public JwtAuthenticationFilter(
      TokenProvider tokenProvider,
      AuthCookieService cookieService,
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository) {
    this.tokenProvider = tokenProvider;
    this.cookieService = cookieService;
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String token = resolveToken(request);
      if (token != null && !token.isBlank()) {
        try {
          TokenProvider.AccessTokenClaims claims = tokenProvider.parseAccessToken(token);
          User user =
              userRepository
                  .findById(claims.userId())
                  .filter(User::active)
                  .orElse(null);
          // Logout / refresh-reuse revoke all refresh rows → access stops immediately.
          if (user == null || !refreshTokenRepository.hasActiveSession(user.id())) {
            SecurityContextHolder.clearContext();
          } else {
            // Role/active come from DB so deactivate/downgrade apply before access TTL.
            var auth =
                new UsernamePasswordAuthenticationToken(
                    user.id(),
                    null,
                    List.of(new SimpleGrantedAuthority(user.role().springRole())));
            auth.setDetails(
                new TokenProvider.AccessTokenClaims(user.id(), user.username(), user.role()));
            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        } catch (Exception ignored) {
          SecurityContextHolder.clearContext();
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String cookie = cookieService.readCookie(request, cookieService.accessCookieName());
    if (cookie != null && !cookie.isBlank()) {
      return cookie;
    }
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
