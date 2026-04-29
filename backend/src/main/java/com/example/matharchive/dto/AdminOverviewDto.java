package com.example.matharchive.dto;

public record AdminOverviewDto(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long todayNewUsers,
        long totalQuestions,
        long pendingQuestions,
        long approvedQuestions,
        long rejectedQuestions,
        long totalMistakes,
        long totalFavorites
) {
}
