package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.enums.ParameterScope;
import com.bifrost.backend.domain.model.Parameter;
import com.bifrost.backend.infrastructure.adapter.output.persistence.entity.ParameterJpaEntity;
import java.util.HashMap;

public final class ParameterMapper {
  private ParameterMapper() {}

  public static Parameter toDomain(ParameterJpaEntity entity) {
    return new Parameter(
        entity.getId(),
        ParameterScope.fromDb(entity.getScope()),
        entity.getScopeId(),
        entity.getKey(),
        entity.getValueJson(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static ParameterJpaEntity toEntity(Parameter parameter) {
    ParameterJpaEntity entity = new ParameterJpaEntity();
    entity.setId(parameter.id());
    entity.setScope(parameter.scope().dbValue());
    entity.setScopeId(parameter.scopeId());
    entity.setKey(parameter.key());
    entity.setValueJson(new HashMap<>(parameter.value()));
    entity.setCreatedAt(parameter.createdAt());
    entity.setUpdatedAt(parameter.updatedAt());
    return entity;
  }
}
