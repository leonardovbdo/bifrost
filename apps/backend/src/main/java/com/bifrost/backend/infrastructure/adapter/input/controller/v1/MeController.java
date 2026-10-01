package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.dto.SessionConfigView;
import com.bifrost.backend.application.usecase.auth.GetCurrentUserUseCase;
import com.bifrost.backend.application.usecase.session.GetSessionConfigUseCase;
import com.bifrost.backend.application.usecase.session.SwitchActiveProfileUseCase;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.ActiveProfileRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.UserResponse;
import com.bifrost.backend.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
  private final GetCurrentUserUseCase getCurrentUserUseCase;
  private final GetSessionConfigUseCase getSessionConfigUseCase;
  private final SwitchActiveProfileUseCase switchActiveProfileUseCase;

  public MeController(
      GetCurrentUserUseCase getCurrentUserUseCase,
      GetSessionConfigUseCase getSessionConfigUseCase,
      SwitchActiveProfileUseCase switchActiveProfileUseCase) {
    this.getCurrentUserUseCase = getCurrentUserUseCase;
    this.getSessionConfigUseCase = getSessionConfigUseCase;
    this.switchActiveProfileUseCase = switchActiveProfileUseCase;
  }

  @GetMapping
  public UserResponse me() {
    return UserResponse.from(getCurrentUserUseCase.execute(SecurityUtils.currentUserId()));
  }

  @GetMapping("/session-config")
  public Map<String, Object> sessionConfig() {
    SessionConfigView view = getSessionConfigUseCase.execute(SecurityUtils.currentUserId());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("schemaVersion", view.schemaVersion());
    body.put("user", UserResponse.from(view.user()));
    body.put("activeProfile", view.activeProfile());
    body.put("allowedProfiles", view.allowedProfiles());
    body.put("permissions", view.permissions());
    body.put("limits", view.limits());
    return body;
  }

  @PutMapping("/active-profile")
  public ResponseEntity<Void> activeProfile(@Valid @RequestBody ActiveProfileRequest request) {
    switchActiveProfileUseCase.execute(SecurityUtils.currentUserId(), request.profileId());
    return ResponseEntity.noContent().build();
  }
}
