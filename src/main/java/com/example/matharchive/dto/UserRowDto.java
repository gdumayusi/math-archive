package com.example.matharchive.dto;

public record UserRowDto(
        Long id,
        String username,
        String displayName,
        String role,
        String email,
        String phone,
        boolean active,
        String createdAt,
        String lastLoginAt,
        int practiceCount
) {
}
