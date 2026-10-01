package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.dto.LoginResult;
import com.bifrost.backend.application.usecase.auth.LoginUserUseCase;
import com.bifrost.backend.application.usecase.auth.LogoutUserUseCase;
import com.bifrost.backend.application.usecase.auth.RefreshSessionUseCase;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.LoginRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.LoginResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.UserResponse;
import com.bifrost.backend.infrastructure.security.AuthCookieService;
import com.bifrost.backend.infrastructure.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final LoginUserUseCase loginUserUseCase;
  private final RefreshSessionUseCase refreshSessionUseCase;
  private final LogoutUserUseCase logoutUserUseCase;
  private final AuthCookieService cookieService;

  public AuthController(
      LoginUserUseCase loginUserUseCase,
      RefreshSessionUseCase refreshSessionUseCase,
      LogoutUserUseCase logoutUserUseCase,
      AuthCookieService cookieService) {
    this.loginUserUseCase = loginUserUseCase;
    this.refreshSessionUseCase = refreshSessionUseCase;
    this.logoutUserUseCase = logoutUserUseCase;
    this.cookieService = cookieService;
  }

  @PostMapping("/login")
  public LoginResponse login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    LoginResult result = loginUserUseCase.execute(request.username(), request.password());
    cookieService.writeAuthCookies(
        response, result.tokens().accessToken(), result.tokens().refreshToken());
    return new LoginResponse(UserResponse.from(result.user()));
  }

  @PostMapping("/refresh")
  public LoginResponse refresh(HttpServletRequest request, HttpServletResponse response) {
    String refresh = cookieService.readCookie(request, cookieService.refreshCookieName());
    LoginResult result = refreshSessionUseCase.execute(refresh);
    cookieService.writeAuthCookies(
        response, result.tokens().accessToken(), result.tokens().refreshToken());
    return new LoginResponse(UserResponse.from(result.user()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    UUID userId = null;
    try {
      userId = SecurityUtils.currentUserId();
    } catch (Exception ignored) {
      // logout still clears cookies / refresh if present
    }
    String refresh = cookieService.readCookie(request, cookieService.refreshCookieName());
    logoutUserUseCase.execute(userId, refresh);
    cookieService.clearAuthCookies(response);
    return ResponseEntity.noContent().build();
  }
}
