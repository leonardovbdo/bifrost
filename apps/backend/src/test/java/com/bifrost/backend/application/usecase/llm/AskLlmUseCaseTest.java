package com.bifrost.backend.application.usecase.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.port.output.LlmClient;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AskLlmUseCaseTest {

  @Test
  void rejectsBlankPrompt() {
    UserRepository users = mock(UserRepository.class);
    LlmClient llm = mock(LlmClient.class);
    AuditRecorder audit = mock(AuditRecorder.class);
    User user = User.create("op", null, "hash", UserRole.OPERATOR);
    when(users.findById(user.id())).thenReturn(Optional.of(user));

    AskLlmUseCase useCase = new AskLlmUseCase(users, llm, audit, 100);
    assertThrows(ValidationException.class, () -> useCase.execute(user.id(), "  ", Map.of()));
  }

  @Test
  void rejectsOversizedPromptPlusContext() {
    UserRepository users = mock(UserRepository.class);
    LlmClient llm = mock(LlmClient.class);
    AuditRecorder audit = mock(AuditRecorder.class);
    User user = User.create("op", null, "hash", UserRole.OPERATOR);
    when(users.findById(user.id())).thenReturn(Optional.of(user));

    AskLlmUseCase useCase = new AskLlmUseCase(users, llm, audit, 20);
    assertThrows(
        ValidationException.class,
        () -> useCase.execute(user.id(), "hello", Map.of("blob", "x".repeat(50))));
  }

  @Test
  void recordsAuditMetadataOnly() {
    UserRepository users = mock(UserRepository.class);
    LlmClient llm = mock(LlmClient.class);
    AuditRecorder audit = mock(AuditRecorder.class);
    User user = User.create("op", null, "hash", UserRole.OPERATOR);
    when(users.findById(user.id())).thenReturn(Optional.of(user));
    when(llm.ask(eq("hi"), anyMap())).thenReturn(new LlmClient.LlmReply("hello", "stub"));

    AskLlmUseCase useCase = new AskLlmUseCase(users, llm, audit, 4000);
    LlmClient.LlmReply reply =
        useCase.execute(user.id(), "hi", Map.of("profileSlug", "nara-sim-alfa"));

    assertEquals("hello", reply.reply());
    verify(audit).record(eq("llm_ask"), eq(user.id()), any(), anyMap());
  }
}
