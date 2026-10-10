package com.bifrost.backend.application.usecase.user;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.ConflictException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.PasswordHasher;
import com.bifrost.backend.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserUseCase {
  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;

  public CreateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
  }

  @Transactional
  public User execute(
      String username, String password, UserRole role, String email, boolean active) {
    if (userRepository.existsByUsername(username)) {
      throw new ConflictException("USER_USERNAME_EXISTS", "Username already exists");
    }
    if (email != null && !email.isBlank() && userRepository.existsByEmail(email.trim())) {
      throw new ConflictException("USER_EMAIL_EXISTS", "Email already exists");
    }
    String normalizedEmail = email == null || email.isBlank() ? null : email.trim();
    User created =
        User.create(
            username.trim(),
            normalizedEmail,
            passwordHasher.hash(password),
            role,
            active);
    return userRepository.save(created);
  }
}
