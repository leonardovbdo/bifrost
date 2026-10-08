package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.usecase.llm.AskLlmUseCase;
import com.bifrost.backend.domain.port.output.LlmClient;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.LlmAskRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.LlmAskResponse;
import com.bifrost.backend.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/llm")
public class LlmController {
  private final AskLlmUseCase askLlmUseCase;

  public LlmController(AskLlmUseCase askLlmUseCase) {
    this.askLlmUseCase = askLlmUseCase;
  }

  @PostMapping("/ask")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public LlmAskResponse ask(@Valid @RequestBody LlmAskRequest request) {
    LlmClient.LlmReply reply =
        askLlmUseCase.execute(SecurityUtils.currentUserId(), request.prompt(), request.context());
    return new LlmAskResponse(reply.reply(), reply.model());
  }
}
