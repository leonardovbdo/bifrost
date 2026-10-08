package com.bifrost.backend.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JsonUnauthorizedEntryPoint implements AuthenticationEntryPoint {
  private static final byte[] BODY =
      "{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\",\"details\":null}}"
          .getBytes(StandardCharsets.UTF_8);

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getOutputStream().write(BODY);
  }
}
