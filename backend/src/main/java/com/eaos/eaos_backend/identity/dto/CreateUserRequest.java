package com.eaos.eaos_backend.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Only these two fields can be sent. A client can never choose its own role or organization. */
public record CreateUserRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 12, max = 72, message = "must be between 12 and 72 characters")
        String password) {}
