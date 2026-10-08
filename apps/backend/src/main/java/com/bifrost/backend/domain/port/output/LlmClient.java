package com.bifrost.backend.domain.port.output;

import java.util.Map;

public interface LlmClient {
  LlmReply ask(String prompt, Map<String, Object> context);

  record LlmReply(String reply, String model) {}
}
