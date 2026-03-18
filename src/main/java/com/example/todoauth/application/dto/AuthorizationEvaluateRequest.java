package com.example.todoauth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record AuthorizationEvaluateRequest(
        @NotNull Long userId,
        @NotBlank String resourceType,
        Long resourceId,
        @NotBlank String action,
        String fieldName,
        String fileType,
        Map<String, Object> contextAttributes
) {
}
