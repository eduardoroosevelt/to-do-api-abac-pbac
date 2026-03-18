package com.example.todoauth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourceFileTypeCreateRequest(
        @NotBlank String fileTypeCode,
        @NotBlank String displayName,
        Boolean sensitive,
        String defaultAccessLevel
) {
}
