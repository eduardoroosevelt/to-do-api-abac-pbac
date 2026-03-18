package com.example.todoauth.domain.model;

import com.example.todoauth.domain.valueobject.PolicyEffect;
import java.util.List;

public record AuthorizationDecision(
        boolean allowed,
        PolicyEffect effect,
        String reason,
        List<String> matchedPolicies
) {
    public static AuthorizationDecision allow(String reason, List<String> matchedPolicies) {
        return new AuthorizationDecision(true, PolicyEffect.ALLOW, reason, matchedPolicies);
    }

    public static AuthorizationDecision deny(String reason, List<String> matchedPolicies) {
        return new AuthorizationDecision(false, PolicyEffect.DENY, reason, matchedPolicies);
    }
}
