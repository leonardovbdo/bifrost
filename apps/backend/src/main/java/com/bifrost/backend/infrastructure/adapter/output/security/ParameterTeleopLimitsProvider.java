package com.bifrost.backend.infrastructure.adapter.output.security;

import com.bifrost.backend.domain.enums.ParameterScope;
import com.bifrost.backend.domain.port.output.TeleopLimitsProvider;
import com.bifrost.backend.domain.repository.ParameterRepository;
import com.bifrost.backend.domain.service.TeleopLimitsMerger;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ParameterTeleopLimitsProvider implements TeleopLimitsProvider {
  private final ParameterRepository parameterRepository;
  private final BifrostProperties properties;

  public ParameterTeleopLimitsProvider(
      ParameterRepository parameterRepository, BifrostProperties properties) {
    this.parameterRepository = parameterRepository;
    this.properties = properties;
  }

  @Override
  public Map<String, Object> resolveForUser(UUID userId) {
    Map<String, Object> globalPresets =
        parameterRepository
            .find(ParameterScope.GLOBAL, null, TeleopLimitsMerger.KEY_PRESETS)
            .map(p -> p.value())
            .orElse(null);

    String globalActive =
        parameterRepository
            .find(ParameterScope.GLOBAL, null, TeleopLimitsMerger.KEY_ACTIVE)
            .map(p -> String.valueOf(p.value().getOrDefault("value", "normal")))
            .orElse(null);

    String userActive = null;
    if (userId != null) {
      userActive =
          parameterRepository
              .find(ParameterScope.USER, userId, TeleopLimitsMerger.KEY_ACTIVE)
              .map(p -> String.valueOf(p.value().getOrDefault("value", "")))
              .filter(s -> !s.isBlank())
              .orElse(null);
    }

    return TeleopLimitsMerger.merge(globalPresets, globalActive, userActive, fallback());
  }

  private Map<String, Object> fallback() {
    Map<String, Object> teleop = new LinkedHashMap<>();
    teleop.put("linearMax", properties.teleop().linearMax());
    teleop.put("angularMax", properties.teleop().angularMax());
    teleop.put("profile", properties.teleop().profile());
    return teleop;
  }
}
