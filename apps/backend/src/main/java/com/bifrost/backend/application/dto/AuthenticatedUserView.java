package com.bifrost.backend.application.dto;

import com.bifrost.backend.domain.enums.UserRole;
import java.util.UUID;

public record AuthenticatedUserView(UUID id, String username, UserRole role) {}
