package com.bifrost.backend.domain.repository;

import com.bifrost.backend.domain.enums.ParameterScope;
import com.bifrost.backend.domain.model.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParameterRepository {
  Optional<Parameter> find(ParameterScope scope, UUID scopeId, String key);

  List<Parameter> findByScope(ParameterScope scope, UUID scopeId);

  Parameter save(Parameter parameter);
}
