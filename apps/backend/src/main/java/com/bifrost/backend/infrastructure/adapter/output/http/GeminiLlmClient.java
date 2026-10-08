package com.bifrost.backend.infrastructure.adapter.output.http;

import com.bifrost.backend.domain.exception.LlmException;
import com.bifrost.backend.domain.port.output.LlmClient;
import com.bifrost.backend.infrastructure.config.BifrostProperties;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GeminiLlmClient implements LlmClient {
  private static final Logger log = LoggerFactory.getLogger(GeminiLlmClient.class);
  private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
      new ParameterizedTypeReference<>() {};

  private final BifrostProperties properties;
  private final RestClient restClient;

  public GeminiLlmClient(BifrostProperties properties) {
    this.properties = properties;
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofMillis(properties.llm().timeoutMs()));
    factory.setReadTimeout(Duration.ofMillis(properties.llm().timeoutMs()));
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  @Override
  public LlmReply ask(String prompt, Map<String, Object> context) {
    String apiKey = properties.llm().apiKey();
    if (apiKey == null || apiKey.isBlank()) {
      if (properties.llm().stubWhenMissingKey()) {
        return stubReply(prompt, context);
      }
      throw new LlmException("LLM_NOT_CONFIGURED", "LLM API key is not configured");
    }

    String model = properties.llm().model();
    String url = properties.llm().baseUrl() + "/models/" + model + ":generateContent";

    String text = buildPrompt(prompt, context);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", text)))));

    try {
      Map<String, Object> response =
          restClient
              .post()
              .uri(url)
              .header("x-goog-api-key", apiKey)
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .body(MAP_TYPE);

      return new LlmReply(extractText(response), model);
    } catch (LlmException ex) {
      throw ex;
    } catch (RestClientResponseException ex) {
      log.warn("LLM provider error model={} status={}", model, ex.getStatusCode().value());
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM provider request failed");
    } catch (Exception ex) {
      log.warn("LLM provider request failed model={}", model);
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM provider request failed");
    }
  }

  private String buildPrompt(String prompt, Map<String, Object> context) {
    if (context == null || context.isEmpty()) {
      return prompt;
    }
    return prompt + "\n\nContext:\n" + context;
  }

  @SuppressWarnings("unchecked")
  private String extractText(Map<String, Object> response) {
    if (response == null) {
      throw new LlmException("LLM_PROVIDER_ERROR", "Empty LLM response");
    }
    Object candidatesObj = response.get("candidates");
    if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM response missing candidates");
    }
    Object first = candidates.get(0);
    if (!(first instanceof Map<?, ?> candidate)) {
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM response missing candidates");
    }
    Object contentObj = candidate.get("content");
    if (!(contentObj instanceof Map<?, ?> content)) {
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM response missing parts");
    }
    Object partsObj = content.get("parts");
    if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM response missing parts");
    }
    Object part0 = parts.get(0);
    if (!(part0 instanceof Map<?, ?> part)) {
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM response missing text");
    }
    Object textObj = part.get("text");
    if (!(textObj instanceof String text) || text.isBlank()) {
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM response missing text");
    }
    return text;
  }

  private LlmReply stubReply(String prompt, Map<String, Object> context) {
    String model = properties.llm().model() + "-stub";
    String reply =
        "Stub LLM reply (no API key configured). Prompt length="
            + prompt.length()
            + (context == null || context.isEmpty() ? "" : ", contextKeys=" + context.keySet());
    return new LlmReply(reply, model);
  }
}
