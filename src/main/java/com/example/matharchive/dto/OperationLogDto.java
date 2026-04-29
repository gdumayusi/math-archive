package com.example.matharchive.dto;

public record OperationLogDto(
        Long id,
        String operatorName,
        String operatorRole,
        String actionType,
        String actionModule,
        String actionDetail,
        String actionStatus,
        String operatedAt,
        String ipAddress
) {
}
