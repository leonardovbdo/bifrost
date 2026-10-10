package com.bifrost.backend.application.usecase.user;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserUseCase {
  private final UserRepository userRepository;

  public UpdateUserUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User execute(UUID userId, Boolean active, UserRole role) {
    if (active == null && role == null) {
      throw new ValidationException("USER_PATCH_EMPTY", "At least one of active or role is required");
    }
    User existing =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));
    UserRole newRole = role != null ? role : existing.role();
    boolean newActive = active != null ? active : existing.active();
    boolean wasActiveAdmin = existing.role() == UserRole.ADMIN && existing.active();
    boolean willBeActiveAdmin = newRole == UserRole.ADMIN && newActive;
    if (wasActiveAdmin && !willBeActiveAdmin) {
      if (userRepository.countActiveByRole(UserRole.ADMIN) <= 1) {
        throw new ValidationException(
            "USER_LAST_ADMIN", "Cannot demote or deactivate the last active admin");
      }
    }
    User patched = existing.applyAdminPatch(active, role);
    return userRepository.save(patched);
  }
}
