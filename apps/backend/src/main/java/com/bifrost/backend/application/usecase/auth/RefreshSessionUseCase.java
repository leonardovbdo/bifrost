package com.bifrost.backend.application.usecase.auth;

import com.bifrost.backend.application.dto.AuthTokens;
import com.bifrost.backend.application.dto.AuthenticatedUserView;
import com.bifrost.backend.application.dto.LoginResult;
import com.bifrost.backend.domain.exception.AuthenticationFailedException;
import com.bifrost.backend.domain.model.RefreshToken;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.TokenProvider;
import com.bifrost.backend.domain.repository.RefreshTokenRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshSessionUseCase {
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final TokenProvider tokenProvider;

  public RefreshSessionUseCase(
      RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository,
      TokenProvider tokenProvider) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.tokenProvider = tokenProvider;
  }

  @Transactional
  public LoginResult execute(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
      throw new AuthenticationFailedException();
    }

    String hash = tokenProvider.hashRefreshToken(rawRefreshToken);
    RefreshToken existing =
        refreshTokenRepository.findByTokenHash(hash).orElseThrow(AuthenticationFailedException::new);

    if (!existing.isValid(Instant.now())) {
      throw new AuthenticationFailedException();
    }

    existing.revoke();
    refreshTokenRepository.save(existing);

    User user =
        userRepository
            .findById(existing.userId())
            .filter(User::active)
            .orElseThrow(AuthenticationFailedException::new);

    String access = tokenProvider.createAccessToken(user.id(), user.username(), user.role());
    String newRefreshRaw = tokenProvider.createRefreshTokenValue();
    RefreshToken rotated =
        RefreshToken.issue(
            user.id(), tokenProvider.hashRefreshToken(newRefreshRaw), tokenProvider.refreshExpiresAt());
    refreshTokenRepository.save(rotated);

    return new LoginResult(
        new AuthenticatedUserView(user.id(), user.username(), user.role()),
        new AuthTokens(access, newRefreshRaw));
  }
}
