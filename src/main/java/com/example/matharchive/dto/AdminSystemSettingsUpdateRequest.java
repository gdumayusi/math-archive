package com.example.matharchive.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminSystemSettingsUpdateRequest(
        @NotBlank String siteName,
        String adminEmail,
        String contactPhone,
        String siteIntro,
        String recordNumber
) {
}
