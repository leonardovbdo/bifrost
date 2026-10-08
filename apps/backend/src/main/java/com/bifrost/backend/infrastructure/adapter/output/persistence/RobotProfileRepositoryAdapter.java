package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.domain.repository.RobotProfileRepository;
import com.bifrost.backend.domain.service.RobotProfileValidator;
import com.bifrost.backend.infrastructure.adapter.output.persistence.jpa.RobotProfileJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class RobotProfileRepositoryAdapter implements RobotProfileRepository {
  private final RobotProfileJpaRepository jpaRepository;

  public RobotProfileRepositoryAdapter(RobotProfileJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<RobotProfile> findById(UUID id) {
    return jpaRepository.findById(id).map(RobotProfileMapper::toDomain);
  }

  @Override
  public Optional<RobotProfile> findBySlug(String slug) {
    return jpaRepository.findBySlug(slug).map(RobotProfileMapper::toDomain);
  }

  @Override
  public List<RobotProfile> findAll() {
    return jpaRepository.findAll(Sort.by("slug")).stream().map(RobotProfileMapper::toDomain).toList();
  }

  @Override
  public List<RobotProfile> findActiveByIds(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findByIdInAndActiveTrue(ids).stream()
        .map(RobotProfileMapper::toDomain)
        .toList();
  }

  @Override
  public List<RobotProfile> findByIds(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findByIdIn(ids).stream().map(RobotProfileMapper::toDomain).toList();
  }

  @Override
  public RobotProfile save(RobotProfile profile) {
    RobotProfileValidator.validate(profile);
    return RobotProfileMapper.toDomain(jpaRepository.save(RobotProfileMapper.toEntity(profile)));
  }

  @Override
  public boolean existsBySlug(String slug) {
    return jpaRepository.existsBySlug(slug);
  }
}
