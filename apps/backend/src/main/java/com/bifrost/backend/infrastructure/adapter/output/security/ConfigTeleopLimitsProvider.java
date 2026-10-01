package com.bifrost.backend.infrastructure.adapter.output.security;

import com.bifrost.backend.domain.port.output.TeleopLimitsProvider;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ConfigTeleopLimitsProvider implements TeleopLimitsProvider {
  private final BifrostProperties properties;

  public ConfigTeleopLimitsProvider(BifrostProperties properties) {
    this.properties = properties;
  }

  @Override
  public Map<String, Object> defaultTeleopLimits() {
    Map<String, Object> teleop = new LinkedHashMap<>();
    teleop.put("linearMax", properties.teleop().linearMax());
    teleop.put("angularMax", properties.teleop().angularMax());
    teleop.put("profile", properties.teleop().profile());
    return teleop;
  }
}
