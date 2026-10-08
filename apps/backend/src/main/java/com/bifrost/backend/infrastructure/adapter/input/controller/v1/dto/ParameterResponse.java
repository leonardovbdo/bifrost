package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

import com.bifrost.backend.domain.model.Parameter;
import java.util.Map;

public record ParameterResponse(
    String id, String scope, String scopeId, String key, Map<String, Object> value) {
  public static ParameterResponse from(Parameter parameter) {
    return new ParameterResponse(
        parameter.id().toString(),
        parameter.scope().dbValue(),
        parameter.scopeId() == null ? null : parameter.scopeId().toString(),
        parameter.key(),
        parameter.value());
  }
}
