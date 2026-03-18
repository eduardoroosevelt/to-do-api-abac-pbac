package com.example.todoauth.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PolicyCreateRequest(
        @NotBlank String policyCode,
        @NotBlank String policyName,
        String description,
        @NotBlank String resourceType,
        @NotBlank String actionCode,
        @NotBlank String effect,
        Integer priority,
        Boolean active,
        Long appliesToRoleId,
        Long appliesToPermissionId,
        String scopeLevel,
        String conditionLogic,
        List<String> fieldNames,
        List<String> fileTypeCodes,
        @Valid List<PolicyConditionRequest> conditions
) {
}
