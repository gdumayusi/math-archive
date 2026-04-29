package com.example.matharchive.domain;

public enum PracticeStatus {
    VIEWED,
    MASTERED,
    REVIEW,
    MISTAKE;

    public static PracticeStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("练习状态不能为空");
        }
        try {
            return PracticeStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("不支持的练习状态");
        }
    }
}
