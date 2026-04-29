package com.example.matharchive.dto;

import java.util.List;

public record CategoryNodeDto(
        String key,
        String label,
        int count,
        List<CategoryNodeDto> children
) {
}
