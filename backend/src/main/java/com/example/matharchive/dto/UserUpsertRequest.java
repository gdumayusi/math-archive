package com.example.matharchive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpsertRequest(
        @NotBlank String username,
        @NotBlank String displayName,
        String email,
        String phone,
        @NotBlank String role,
        @NotNull Boolean active,
        String password
) {
}
