package com.example.matharchive.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionUpsertRequest(
        @NotBlank String archiveCode,
        @NotNull @Min(2000) @Max(2100) Integer yearValue,
        @NotBlank String paperName,
        String sourceOrg,
        @NotBlank String subject,
        @NotBlank String chapterName,
        @NotBlank String knowledgeTags,
        @NotBlank String questionType,
        @NotBlank String difficultyLevel,
        @NotBlank String stemText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String answerText,
        String analysisText
) {
}
