package com.example.matharchive.dto;

import java.util.List;

public record AdminDataStatsDto(
        long questionTotal,
        long userTotal,
        long mistakeTotal,
        long favoriteTotal,
        List<AdminChartPointDto> userTrend,
        List<AdminChartPointDto> mistakeTrend,
        List<AdminChartPointDto> questionTypeDistribution,
        List<AdminChartPointDto> hotKnowledgeTop,
        List<AdminChartPointDto> highMistakeTop
) {
}
