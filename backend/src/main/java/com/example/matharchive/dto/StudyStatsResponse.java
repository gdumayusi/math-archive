package com.example.matharchive.dto;

import java.util.List;

public record StudyStatsResponse(
        int totalPractices,
        int masteredCount,
        int reviewCount,
        int mistakeCount,
        int accuracyRate,
        int activeDays,
        int currentMasteredCount,
        int pendingReviewCount,
        List<SubjectProgressDto> subjectProgress,
        List<StudyTimelinePointDto> timeline,
        List<KnowledgeStatDto> weakPoints,
        List<RecentPracticeDto> recentRecords
) {
}
