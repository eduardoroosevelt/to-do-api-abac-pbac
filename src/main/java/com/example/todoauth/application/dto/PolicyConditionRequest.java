package com.example.todoauth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyConditionRequest(
        Integer conditionOrder,
        @NotBlank String sourceType,
        @NotBlank String attributeName,
        @NotBlank String operator,
        String comparisonType,
        String expectedValue,
        String comparisonSourceType,
        String comparisonAttributeName
) {
}
