package com.example.todoauth.application.dto;

public record ResourceResponse(
        Long id,
        String resourceType,
        String resourceName,
        String description,
        String tableName,
        boolean supportsAttachments,
        boolean active
) {
}
