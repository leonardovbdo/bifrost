package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  Optional<User> findById(UUID id);

  Optional<User> findByUsername(String username);

  List<User> findAllOrderByUsernameAsc();

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  User save(User user);
}
