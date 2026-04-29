package com.example.matharchive.dto;

public record StatCardDto(
        String title,
        String value,
        String sub,
        String icon,
        boolean alert
) {
}
