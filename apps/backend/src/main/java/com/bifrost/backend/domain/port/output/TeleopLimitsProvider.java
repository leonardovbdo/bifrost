package com.bifrost.backend.domain.port.output;

import java.util.Map;

public interface TeleopLimitsProvider {
  Map<String, Object> defaultTeleopLimits();
}
