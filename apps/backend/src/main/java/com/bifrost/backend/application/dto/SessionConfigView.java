package com.bifrost.backend.application.dto;

import java.util.List;
import java.util.Map;

public record SessionConfigView(
    int schemaVersion,
    AuthenticatedUserView user,
    Map<String, Object> activeProfile,
    List<Map<String, Object>> allowedProfiles,
    Map<String, Boolean> permissions,
    Map<String, Object> limits) {}
