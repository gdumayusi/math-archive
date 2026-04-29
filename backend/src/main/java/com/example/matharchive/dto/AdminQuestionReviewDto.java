package com.example.matharchive.dto;

public record AdminQuestionReviewDto(
        Long id,
        String archiveCode,
        String stemText,
        String chapterName,
        String knowledgeTags,
        String submitterName,
        String questionType,
        String difficultyLevel,
        String reviewStatus,
        String submittedAt,
        String answerText,
        String analysisText,
        String sourceOrg,
        String rejectReason
) {
}
