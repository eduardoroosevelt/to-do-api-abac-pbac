package com.example.todoauth.application.dto;

import java.util.List;

public record AuthorizationEvaluateResponse(
        boolean allowed,
        String effect,
        String reason,
        List<String> matchedPolicies
) {
}
