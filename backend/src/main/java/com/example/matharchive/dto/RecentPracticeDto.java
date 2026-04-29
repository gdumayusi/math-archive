package com.example.matharchive.dto;

public record RecentPracticeDto(
        Long questionId,
        String archiveCode,
        String subject,
        String chapterName,
        String status,
        String practicedAt
) {
}
