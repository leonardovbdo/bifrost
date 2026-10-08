package com.bifrost.backend.application.dto;

public record LoginResult(AuthenticatedUserView user, AuthTokens tokens) {}
