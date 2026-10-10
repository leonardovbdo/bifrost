package com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto;

public record UserPatchRequest(Boolean active, String role) {}
