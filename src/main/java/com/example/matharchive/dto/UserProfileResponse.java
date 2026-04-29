package com.example.matharchive.dto;

public record UserProfileResponse(
        Long userId,
        String username,
        String displayName,
        String email,
        String studentNo,
        String avatarKey,
        String avatarImage,
        String bio,
        String role,
        boolean active,
        String createdAt,
        String updatedAt,
        String lastPracticedAt,
        int favoriteCount,
        int mistakeCount,
        int practiceCount,
        int masteredCount
) {
}
