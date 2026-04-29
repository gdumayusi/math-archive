package com.example.matharchive.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminReviewActionRequest(
        @NotBlank String action,
        String rejectReason
) {
}
