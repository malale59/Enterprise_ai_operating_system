package com.eaos.eaos_backend.identity.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** What the API sends for a user. There is deliberately no password field here. */
public record UserResponse(
    UUID id,
    UUID organizationId,
    String email,
    boolean enabled,
    List<String> roles,
    Instant createdAt,
    Instant lastLoginAt) {}
