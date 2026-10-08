package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  Optional<User> findById(UUID id);

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  User save(User user);
}
