package com.bifrost.backend.application.usecase.auth;

import com.bifrost.backend.application.dto.AuthTokens;
import com.bifrost.backend.application.dto.AuthenticatedUserView;
import com.bifrost.backend.application.dto.LoginResult;
import com.bifrost.backend.domain.exception.AuthenticationFailedException;
import com.bifrost.backend.domain.model.RefreshToken;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.port.output.PasswordHasher;
import com.bifrost.backend.domain.port.output.TokenProvider;
import com.bifrost.backend.domain.repository.RefreshTokenRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.domain.service.AuditUsernameSanitizer;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserUseCase {
  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuditRecorder auditRecorder;

  public LoginUserUseCase(
      UserRepository userRepository,
      PasswordHasher passwordHasher,
      TokenProvider tokenProvider,
      RefreshTokenRepository refreshTokenRepository,
      AuditRecorder auditRecorder) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.tokenProvider = tokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public LoginResult execute(String username, String password) {
    var maybeUser = userRepository.findByUsername(username).filter(User::active);
    String auditUsername = AuditUsernameSanitizer.forAudit(username);
    if (maybeUser.isEmpty()) {
      auditRecorder.record("login_failure", null, null, Map.of("username", auditUsername));
      throw new AuthenticationFailedException();
    }

    User user = maybeUser.get();
    if (!passwordHasher.matches(password, user.passwordHash())) {
      auditRecorder.record("login_failure", user.id(), null, Map.of("username", auditUsername));
      throw new AuthenticationFailedException();
    }

    // New login invalidates prior refresh sessions for this account.
    refreshTokenRepository.revokeAllForUser(user.id());

    String access = tokenProvider.createAccessToken(user.id(), user.username(), user.role());
    String refreshRaw = tokenProvider.createRefreshTokenValue();
    RefreshToken refresh =
        RefreshToken.issue(
            user.id(), tokenProvider.hashRefreshToken(refreshRaw), tokenProvider.refreshExpiresAt());
    refreshTokenRepository.save(refresh);

    auditRecorder.record("login_success", user.id(), user.lastActiveProfileId(), Map.of());

    return new LoginResult(
        new AuthenticatedUserView(user.id(), user.username(), user.role()),
        new AuthTokens(access, refreshRaw));
  }
}
