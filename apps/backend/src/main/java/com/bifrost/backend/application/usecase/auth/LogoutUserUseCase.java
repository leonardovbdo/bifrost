package com.bifrost.backend.application.usecase.auth;

import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.port.output.TokenProvider;
import com.bifrost.backend.domain.repository.RefreshTokenRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUserUseCase {
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenProvider tokenProvider;
  private final AuditRecorder auditRecorder;

  public LogoutUserUseCase(
      RefreshTokenRepository refreshTokenRepository,
      TokenProvider tokenProvider,
      AuditRecorder auditRecorder) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.tokenProvider = tokenProvider;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public void execute(UUID userId, String rawRefreshToken) {
    if (userId != null) {
      refreshTokenRepository.revokeAllForUser(userId);
      auditRecorder.record("logout", userId, null, Map.of());
      return;
    }
    if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
      refreshTokenRepository
          .findByTokenHash(tokenProvider.hashRefreshToken(rawRefreshToken))
          .ifPresent(
              token -> {
                token.revoke();
                refreshTokenRepository.save(token);
                auditRecorder.record("logout", token.userId(), null, Map.of());
              });
    }
  }
}
