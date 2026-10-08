package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.usecase.robotprofile.CreateRobotProfileUseCase;
import com.bifrost.backend.application.usecase.robotprofile.GetRobotProfileUseCase;
import com.bifrost.backend.application.usecase.robotprofile.GrantProfileAccessUseCase;
import com.bifrost.backend.application.usecase.robotprofile.ListRobotProfilesUseCase;
import com.bifrost.backend.application.usecase.robotprofile.UpdateRobotProfileUseCase;
import com.bifrost.backend.domain.enums.ProfileEnvironment;
import com.bifrost.backend.domain.model.RobotProfile;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.GrantAccessRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.RobotProfileListResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.RobotProfilePatchRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.RobotProfileRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.RobotProfileResponse;
import com.bifrost.backend.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/robot-profiles")
public class RobotProfilesController {
  private final ListRobotProfilesUseCase listRobotProfilesUseCase;
  private final GetRobotProfileUseCase getRobotProfileUseCase;
  private final CreateRobotProfileUseCase createRobotProfileUseCase;
  private final UpdateRobotProfileUseCase updateRobotProfileUseCase;
  private final GrantProfileAccessUseCase grantProfileAccessUseCase;

  public RobotProfilesController(
      ListRobotProfilesUseCase listRobotProfilesUseCase,
      GetRobotProfileUseCase getRobotProfileUseCase,
      CreateRobotProfileUseCase createRobotProfileUseCase,
      UpdateRobotProfileUseCase updateRobotProfileUseCase,
      GrantProfileAccessUseCase grantProfileAccessUseCase) {
    this.listRobotProfilesUseCase = listRobotProfilesUseCase;
    this.getRobotProfileUseCase = getRobotProfileUseCase;
    this.createRobotProfileUseCase = createRobotProfileUseCase;
    this.updateRobotProfileUseCase = updateRobotProfileUseCase;
    this.grantProfileAccessUseCase = grantProfileAccessUseCase;
  }

  @GetMapping
  public RobotProfileListResponse list() {
    List<RobotProfileResponse> items =
        listRobotProfilesUseCase.execute(SecurityUtils.currentUserId()).stream()
            .map(RobotProfileResponse::from)
            .toList();
    return new RobotProfileListResponse(items);
  }

  @GetMapping("/{id}")
  public RobotProfileResponse get(@PathVariable UUID id) {
    return RobotProfileResponse.from(getRobotProfileUseCase.execute(SecurityUtils.currentUserId(), id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<RobotProfileResponse> create(@Valid @RequestBody RobotProfileRequest request) {
    UUID adminId = SecurityUtils.currentUserId();
    RobotProfile created =
        createRobotProfileUseCase.execute(
            adminId,
            request.slug(),
            request.displayName(),
            request.project(),
            request.prefix(),
            ProfileEnvironment.fromDb(request.environment()),
            request.technology(),
            request.capabilities(),
            request.topics(),
            request.frames() == null ? Map.of() : request.frames(),
            request.rosbridgeUrl(),
            request.videoBaseUrl(),
            request.active() == null || request.active(),
            request.grantAccessToCreator() == null || request.grantAccessToCreator());
    return ResponseEntity.status(HttpStatus.CREATED).body(RobotProfileResponse.from(created));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public RobotProfileResponse patch(
      @PathVariable UUID id, @RequestBody RobotProfilePatchRequest request) {
    ProfileEnvironment environment =
        request.environment() == null ? null : ProfileEnvironment.fromDb(request.environment());
    RobotProfile updated =
        updateRobotProfileUseCase.execute(
            id,
            request.displayName(),
            request.project(),
            request.prefix(),
            environment,
            request.technology(),
            request.capabilities(),
            request.topics(),
            request.frames(),
            request.rosbridgeUrl(),
            request.videoBaseUrl(),
            request.active());
    return RobotProfileResponse.from(updated);
  }

  @PostMapping("/{id}/access")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> grantAccess(
      @PathVariable UUID id, @Valid @RequestBody GrantAccessRequest request) {
    grantProfileAccessUseCase.execute(id, request.userId());
    return ResponseEntity.noContent().build();
  }
}
