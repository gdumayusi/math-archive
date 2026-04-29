package com.example.matharchive.dto;

public record AdminSystemSettingsDto(
        String siteName,
        String adminEmail,
        String contactPhone,
        String siteIntro,
        String recordNumber,
        String version,
        String publishDate,
        String teamName,
        int totalStorageGb,
        int usedStorageGb,
        String currentTime
) {
}
