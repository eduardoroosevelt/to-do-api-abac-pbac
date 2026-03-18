package com.example.todoauth.application.dto;

import java.util.List;

public record PolicyResponse(
        Long id,
        String policyCode,
        String policyName,
        String resourceType,
        String actionCode,
        String effect,
        Integer priority,
        boolean active,
        List<String> fieldTargets,
        List<String> fileTargets,
        List<String> conditions
) {
}
