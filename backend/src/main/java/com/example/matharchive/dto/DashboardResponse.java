package com.example.matharchive.dto;

import java.util.List;

public record DashboardResponse(
        String role,
        String headline,
        String subHeadline,
        List<StatCardDto> stats,
        ResumeCardDto resumeCard
) {
}
