package com.example.matharchive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PracticeRecordRequest(
        @NotNull(message = "不能为空") Long questionId,
        @NotBlank(message = "不能为空") String status,
        String sourcePage
) {
}
