package com.eaos.eaos_backend.identity.dto;

import java.util.List;
import java.util.UUID;

/** What the API sends for a role. We choose these fields; the database entity is never exposed. */
public record RoleResponse(UUID id, String name, String description, List<String> permissions) {}
