package com.eaos.eaos_backend.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** PATCH body: every field is optional, and only the fields that are sent are changed. */
public record UpdateUserRequest(@Email @Size(max = 254) String email, Boolean enabled) {}
