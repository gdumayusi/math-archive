package com.example.matharchive.dto;

import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateRequest(
        @NotBlank(message = "显示名称不能为空") String displayName,
        String email,
        String bio,
        String avatarKey,
        String avatarImage
) {
}
