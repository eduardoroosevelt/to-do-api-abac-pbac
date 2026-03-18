package com.example.todoauth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourceCreateRequest(
        @NotBlank String resourceType,
        @NotBlank String resourceName,
        String description,
        String tableName,
        String idFieldName,
        String tenantFieldName,
        String escolaFieldName,
        String ownerFieldName,
        String createdByFieldName,
        String sensitivityFieldName,
        Boolean supportsAttachments
) {
}
