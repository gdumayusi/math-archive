package com.example.matharchive.dto;

public record StudyTimelinePointDto(
        String date,
        int viewedCount,
        int masteredCount,
        int reviewCount,
        int mistakeCount
) {
}
