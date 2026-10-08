package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.enums.ParameterScope;
import com.bifrost.backend.domain.model.Parameter;
import com.bifrost.backend.domain.repository.ParameterRepository;
import com.bifrost.backend.infrastructure.adapter.output.persistence.jpa.ParameterJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ParameterRepositoryAdapter implements ParameterRepository {
  private final ParameterJpaRepository jpaRepository;

  public ParameterRepositoryAdapter(ParameterJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Parameter> find(ParameterScope scope, UUID scopeId, String key) {
    if (scope == ParameterScope.GLOBAL) {
      return jpaRepository.findByScopeAndScopeIdIsNullAndKey(scope.dbValue(), key).map(ParameterMapper::toDomain);
    }
    return jpaRepository.findByScopeAndScopeIdAndKey(scope.dbValue(), scopeId, key).map(ParameterMapper::toDomain);
  }

  @Override
  public List<Parameter> findByScope(ParameterScope scope, UUID scopeId) {
    if (scope == ParameterScope.GLOBAL) {
      return jpaRepository.findByScopeAndScopeIdIsNull(scope.dbValue()).stream()
          .map(ParameterMapper::toDomain)
          .toList();
    }
    return jpaRepository.findByScopeAndScopeId(scope.dbValue(), scopeId).stream()
        .map(ParameterMapper::toDomain)
        .toList();
  }

  @Override
  public Parameter save(Parameter parameter) {
    return ParameterMapper.toDomain(jpaRepository.save(ParameterMapper.toEntity(parameter)));
  }
}
