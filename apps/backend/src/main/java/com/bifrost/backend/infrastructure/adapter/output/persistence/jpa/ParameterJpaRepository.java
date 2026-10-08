package com.bifrost.backend.infrastructure.adapter.output.persistence.jpa;

import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.ParameterJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterJpaRepository extends JpaRepository<ParameterJpaEntity, UUID> {
  Optional<ParameterJpaEntity> findByScopeAndScopeIdIsNullAndKey(String scope, String key);

  Optional<ParameterJpaEntity> findByScopeAndScopeIdAndKey(String scope, UUID scopeId, String key);

  List<ParameterJpaEntity> findByScopeAndScopeIdIsNull(String scope);

  List<ParameterJpaEntity> findByScopeAndScopeId(String scope, UUID scopeId);
}
