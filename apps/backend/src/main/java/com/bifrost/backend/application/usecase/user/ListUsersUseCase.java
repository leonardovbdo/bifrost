package com.bifrost.backend.application.usecase.user;

import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsersUseCase {
  private final UserRepository userRepository;

  public ListUsersUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<User> execute() {
    return userRepository.findAllOrderByUsernameAsc();
  }
}
