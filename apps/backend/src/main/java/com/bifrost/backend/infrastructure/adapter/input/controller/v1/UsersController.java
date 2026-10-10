package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.usecase.user.CreateUserUseCase;
import com.bifrost.backend.application.usecase.user.ListUsersUseCase;
import com.bifrost.backend.application.usecase.user.UpdateUserUseCase;
import com.bifrost.backend.domain.enums.UserRole;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.AdminUserResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.CreateUserRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.UserListResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.UserPatchRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UsersController {
  private final ListUsersUseCase listUsersUseCase;
  private final CreateUserUseCase createUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;

  public UsersController(
      ListUsersUseCase listUsersUseCase,
      CreateUserUseCase createUserUseCase,
      UpdateUserUseCase updateUserUseCase) {
    this.listUsersUseCase = listUsersUseCase;
    this.createUserUseCase = createUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
  }

  @GetMapping
  public UserListResponse list() {
    return new UserListResponse(
        listUsersUseCase.execute().stream().map(AdminUserResponse::from).toList());
  }

  @PostMapping
  public ResponseEntity<AdminUserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    UserRole role = parseRole(request.role());
    User created =
        createUserUseCase.execute(
            request.username(),
            request.password(),
            role,
            request.email(),
            request.active() == null || request.active());
    return ResponseEntity.status(HttpStatus.CREATED).body(AdminUserResponse.from(created));
  }

  @PatchMapping("/{id}")
  public AdminUserResponse patch(@PathVariable UUID id, @RequestBody UserPatchRequest request) {
    UserRole role = request.role() == null ? null : parseRole(request.role());
    User updated = updateUserUseCase.execute(id, request.active(), role);
    return AdminUserResponse.from(updated);
  }

  private static UserRole parseRole(String role) {
    try {
      return UserRole.fromDb(role);
    } catch (IllegalArgumentException ex) {
      throw new ValidationException("USER_ROLE_INVALID", "Unknown role: " + role);
    }
  }
}
