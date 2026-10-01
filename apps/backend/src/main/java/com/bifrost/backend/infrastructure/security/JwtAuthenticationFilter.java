package com.bifrost.backend.infrastructure.security;

import com.bifrost.backend.domain.port.output.TokenProvider;
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

  public JwtAuthenticationFilter(TokenProvider tokenProvider, AuthCookieService cookieService) {
    this.tokenProvider = tokenProvider;
    this.cookieService = cookieService;
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
          var auth =
              new UsernamePasswordAuthenticationToken(
                  claims.userId(),
                  null,
                  List.of(new SimpleGrantedAuthority(claims.role().springRole())));
          auth.setDetails(claims);
          SecurityContextHolder.getContext().setAuthentication(auth);
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
