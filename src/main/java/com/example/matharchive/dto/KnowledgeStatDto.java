package com.example.matharchive.dto;

public record KnowledgeStatDto(
        String chapterName,
        String knowledgeTags,
        int totalCount,
        int masteredCount,
        int mistakeCount,
        int accuracyRate
) {
}
