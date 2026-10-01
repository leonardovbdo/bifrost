package com.bifrost.backend.application.usecase.auth;

import com.bifrost.backend.application.dto.AuthenticatedUserView;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCurrentUserUseCase {
  private final UserRepository userRepository;

  public GetCurrentUserUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public AuthenticatedUserView execute(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));
    return new AuthenticatedUserView(user.id(), user.username(), user.role());
  }
}
