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
import org.springframework.transaction.annotation.Transactional;

@Service
public class AskLlmUseCase {
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

  @Transactional
  public LlmClient.LlmReply execute(UUID userId, String prompt, Map<String, Object> context) {
    User user =
        userRepository
            .findById(userId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    if (prompt == null || prompt.isBlank()) {
      throw new ValidationException("LLM_PROMPT_REQUIRED", "prompt is required");
    }
    if (prompt.length() > maxPromptLength) {
      throw new ValidationException(
          "LLM_PROMPT_TOO_LONG", "prompt exceeds max length of " + maxPromptLength);
    }

    try {
      LlmClient.LlmReply reply = llmClient.ask(prompt, context == null ? Map.of() : context);
      Map<String, Object> auditPayload = new LinkedHashMap<>();
      auditPayload.put("model", reply.model());
      auditPayload.put("promptLength", prompt.length());
      auditPayload.put("replyLength", reply.reply() == null ? 0 : reply.reply().length());
      if (context != null && !context.isEmpty()) {
        auditPayload.put("contextKeys", context.keySet());
      }
      auditRecorder.record("llm_ask", user.id(), user.lastActiveProfileId(), auditPayload);
      return reply;
    } catch (LlmException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new LlmException("LLM_PROVIDER_ERROR", "LLM provider request failed");
    }
  }
}
