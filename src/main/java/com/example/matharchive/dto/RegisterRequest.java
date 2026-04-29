package com.example.matharchive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "不能为空") @Size(min = 4, max = 32, message = "长度需在 4 到 32 个字符之间") String username,
        @NotBlank(message = "不能为空") @Size(min = 2, max = 32, message = "长度需在 2 到 32 个字符之间") String displayName,
        @Size(max = 128, message = "长度不能超过 128 个字符") String email,
        @NotBlank(message = "不能为空") @Size(min = 6, max = 32, message = "长度需在 6 到 32 个字符之间") String password
) {
}
