package com.example.matharchive.dto;

public record LoginResponse(
        Long userId,
        String username,
        String displayName,
        String studentNo,
        String role,
        String avatarKey,
        String avatarImage,
        String token
) {
}
