package com.bifrost.backend.application.usecase.llm;

import com.bifrost.backend.domain.exception.LlmException;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.port.output.LlmClient;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AskLlmUseCase {
  private static final String CONTEXT_PREFIX = "\n\nContext:\n";

  private final UserRepository userRepository;
  private final LlmClient llmClient;
  private final AuditRecorder auditRecorder;
  private final int maxPromptLength;

  public AskLlmUseCase(
      UserRepository userRepository,
      LlmClient llmClient,
      AuditRecorder auditRecorder,
      @Value("${bifrost.llm.max-prompt-length:4000}") int maxPromptLength) {
    this.userRepository = userRepository;
    this.llmClient = llmClient;
    this.auditRecorder = auditRecorder;
    this.maxPromptLength = maxPromptLength;
  }

  /**
   * HTTP to the LLM provider runs outside a DB transaction. User lookup and audit each use short
   * repository transactions.
   */
  public LlmClient.LlmReply execute(UUID userId, String prompt, Map<String, Object> context) {
    User user =
        userRepository
            .findById(userId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    if (prompt == null || prompt.isBlank()) {
      throw new ValidationException("LLM_PROMPT_REQUIRED", "prompt is required");
    }

    Map<String, Object> safeContext = context == null ? Map.of() : context;
    int composedLength = composedPromptLength(prompt, safeContext);
    if (composedLength > maxPromptLength) {
      throw new ValidationException(
          "LLM_PROMPT_TOO_LONG",
          "prompt+context exceeds max length of " + maxPromptLength);
    }

    LlmClient.LlmReply reply = llmClient.ask(prompt, safeContext);

    Map<String, Object> auditPayload = new LinkedHashMap<>();
    auditPayload.put("model", reply.model());
    auditPayload.put("promptLength", prompt.length());
    auditPayload.put("composedLength", composedLength);
    auditPayload.put("replyLength", reply.reply() == null ? 0 : reply.reply().length());
    if (!safeContext.isEmpty()) {
      auditPayload.put("contextKeys", safeContext.keySet());
    }
    try {
      auditRecorder.record("llm_ask", user.id(), user.lastActiveProfileId(), auditPayload);
    } catch (RuntimeException ex) {
      throw new LlmException("LLM_AUDIT_FAILED", "LLM reply ok but audit record failed");
    }
    return reply;
  }

  static int composedPromptLength(String prompt, Map<String, Object> context) {
    if (context == null || context.isEmpty()) {
      return prompt.length();
    }
    return prompt.length() + CONTEXT_PREFIX.length() + String.valueOf(context).length();
  }
}
