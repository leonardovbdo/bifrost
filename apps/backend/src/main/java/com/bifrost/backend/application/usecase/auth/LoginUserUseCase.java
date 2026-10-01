package com.bifrost.backend.application.usecase.auth;

import com.bifrost.backend.application.dto.AuthTokens;
import com.bifrost.backend.application.dto.AuthenticatedUserView;
import com.bifrost.backend.application.dto.LoginResult;
import com.bifrost.backend.domain.exception.AuthenticationFailedException;
import com.bifrost.backend.domain.model.RefreshToken;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.PasswordHasher;
import com.bifrost.backend.domain.port.output.TokenProvider;
import com.bifrost.backend.domain.repository.RefreshTokenRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserUseCase {
  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  public LoginUserUseCase(
      UserRepository userRepository,
      PasswordHasher passwordHasher,
      TokenProvider tokenProvider,
      RefreshTokenRepository refreshTokenRepository) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.tokenProvider = tokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Transactional
  public LoginResult execute(String username, String password) {
    User user =
        userRepository
            .findByUsername(username)
            .filter(User::active)
            .orElseThrow(AuthenticationFailedException::new);

    if (!passwordHasher.matches(password, user.passwordHash())) {
      throw new AuthenticationFailedException();
    }

    String access =
        tokenProvider.createAccessToken(user.id(), user.username(), user.role());
    String refreshRaw = tokenProvider.createRefreshTokenValue();
    RefreshToken refresh =
        RefreshToken.issue(user.id(), tokenProvider.hashRefreshToken(refreshRaw), tokenProvider.refreshExpiresAt());
    refreshTokenRepository.save(refresh);

    return new LoginResult(
        new AuthenticatedUserView(user.id(), user.username(), user.role()),
        new AuthTokens(access, refreshRaw));
  }
}
