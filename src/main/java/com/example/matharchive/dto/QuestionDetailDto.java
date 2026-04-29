package com.example.matharchive.dto;

public record QuestionDetailDto(
        Long id,
        String archiveCode,
        Integer yearValue,
        String paperName,
        String subject,
        String chapterName,
        String knowledgeTags,
        String questionType,
        String difficultyLevel,
        String stemText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String answerText,
        String analysisText,
        String createdDate,
        boolean favorited,
        boolean mistake,
        String latestPracticeStatus,
        String latestPracticedAt
) {
}
