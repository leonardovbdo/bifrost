package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.infrastructure.adapter.output.persistence.jpa.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {
  private final UserJpaRepository jpaRepository;

  public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaRepository.findById(id).map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaRepository.findByUsername(username).map(UserMapper::toDomain);
  }

  @Override
  public List<User> findAllOrderByUsernameAsc() {
    return jpaRepository.findAllByOrderByUsernameAsc().stream().map(UserMapper::toDomain).toList();
  }

  @Override
  public boolean existsByUsername(String username) {
    return jpaRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public long countActiveByRole(UserRole role) {
    return jpaRepository.countByRoleAndActiveTrue(role.dbValue());
  }

  @Override
  public User save(User user) {
    return UserMapper.toDomain(jpaRepository.save(UserMapper.toEntity(user)));
  }
}
