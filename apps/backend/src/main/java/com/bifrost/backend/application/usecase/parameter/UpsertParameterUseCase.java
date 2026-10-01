package com.bifrost.backend.application.usecase.parameter;

import com.bifrost.backend.domain.enums.ParameterScope;
import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.ForbiddenException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.Parameter;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.repository.ParameterRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import com.bifrost.backend.domain.service.TeleopLimitsMerger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpsertParameterUseCase {
  private static final Set<String> ACTIVE_PRESETS = Set.of("safety", "normal", "fast");

  private final UserRepository userRepository;
  private final ParameterRepository parameterRepository;
  private final AuditRecorder auditRecorder;

  public UpsertParameterUseCase(
      UserRepository userRepository,
      ParameterRepository parameterRepository,
      AuditRecorder auditRecorder) {
    this.userRepository = userRepository;
    this.parameterRepository = parameterRepository;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public Parameter execute(UUID requesterId, ParameterScope scope, String key, Map<String, Object> value) {
    User user =
        userRepository
            .findById(requesterId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    if (scope == ParameterScope.GLOBAL) {
      if (user.role() != UserRole.ADMIN) {
        throw new ForbiddenException("PARAMETER_FORBIDDEN", "Only admin can write global parameters");
      }
      validateGlobal(key, value);
      return upsert(ParameterScope.GLOBAL, null, key, value, user.id());
    }

    if (scope == ParameterScope.USER) {
      validateUserKey(key, value);
      return upsert(ParameterScope.USER, user.id(), key, value, user.id());
    }

    throw new ValidationException("PARAMETER_SCOPE_INVALID", "Unsupported scope");
  }

  private void validateGlobal(String key, Map<String, Object> value) {
    if (TeleopLimitsMerger.KEY_PRESETS.equals(key)) {
      if (value == null || value.isEmpty()) {
        throw new ValidationException("PARAMETER_VALUE_INVALID", "presets cannot be empty");
      }
      for (String preset : ACTIVE_PRESETS) {
        if (!value.containsKey(preset)) {
          throw new ValidationException("PARAMETER_VALUE_INVALID", "Missing preset: " + preset);
        }
      }
      return;
    }
    if (TeleopLimitsMerger.KEY_ACTIVE.equals(key)) {
      validateActiveProfileValue(value);
      return;
    }
    throw new ValidationException("PARAMETER_KEY_INVALID", "Unknown global key: " + key);
  }

  private void validateUserKey(String key, Map<String, Object> value) {
    if (!TeleopLimitsMerger.KEY_ACTIVE.equals(key)) {
      throw new ValidationException(
          "PARAMETER_KEY_INVALID", "User scope only allows teleop.activeProfile");
    }
    validateActiveProfileValue(value);
  }

  private void validateActiveProfileValue(Map<String, Object> value) {
    Object profile = value == null ? null : value.get("value");
    if (profile == null) {
      profile = value == null ? null : value.get("profile");
    }
    if (profile == null || !ACTIVE_PRESETS.contains(String.valueOf(profile))) {
      throw new ValidationException(
          "PARAMETER_VALUE_INVALID", "activeProfile must be safety|normal|fast");
    }
  }

  private Parameter upsert(
      ParameterScope scope, UUID scopeId, String key, Map<String, Object> value, UUID actorId) {
    Map<String, Object> normalized = normalizeValue(key, value);
    Parameter saved =
        parameterRepository
            .find(scope, scopeId, key)
            .map(existing -> parameterRepository.save(existing.withValue(normalized)))
            .orElseGet(
                () -> parameterRepository.save(Parameter.create(scope, scopeId, key, normalized)));

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("scope", scope.dbValue());
    payload.put("key", key);
    auditRecorder.record("parameter_change", actorId, null, payload);
    return saved;
  }

  private Map<String, Object> normalizeValue(String key, Map<String, Object> value) {
    if (TeleopLimitsMerger.KEY_ACTIVE.equals(key)) {
      Object profile = value.get("value");
      if (profile == null) {
        profile = value.get("profile");
      }
      return Map.of("value", String.valueOf(profile));
    }
    return value;
  }
}
