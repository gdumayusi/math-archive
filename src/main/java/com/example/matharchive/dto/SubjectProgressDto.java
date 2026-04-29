package com.example.matharchive.dto;

public record SubjectProgressDto(
        String subject,
        int totalCount,
        int masteredCount,
        int mistakeCount,
        int accuracyRate
) {
}
