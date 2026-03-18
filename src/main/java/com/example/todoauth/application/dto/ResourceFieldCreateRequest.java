package com.example.todoauth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourceFieldCreateRequest(
        @NotBlank String fieldName,
        @NotBlank String displayName,
        @NotBlank String dataType,
        Boolean sensitive,
        String maskStrategy,
        String defaultVisibility,
        Boolean queryable,
        Boolean sortable
) {
}
