package com.example.todoauth.application.usecase;

import com.example.todoauth.application.exception.ForbiddenException;
import com.example.todoauth.domain.model.AuthorizationDecision;
import com.example.todoauth.domain.model.ResourceContext;
import com.example.todoauth.domain.model.SubjectContext;
import com.example.todoauth.domain.policy.PolicyEvaluationRequest;
import com.example.todoauth.domain.service.PolicyEngine;
import com.example.todoauth.domain.valueobject.Action;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    private final PolicyEngine policyEngine;

    public AuthorizationService(PolicyEngine policyEngine) {
        this.policyEngine = policyEngine;
    }

    public AuthorizationDecision authorize(SubjectContext subject, ResourceContext resource, Action action) {
        return policyEngine.evaluate(new PolicyEvaluationRequest(subject, resource, action, Map.of(), null, null));
    }

    public AuthorizationDecision authorize(SubjectContext subject, ResourceContext resource, Action action, Map<String, Object> context) {
        return policyEngine.evaluate(new PolicyEvaluationRequest(subject, resource, action, context, null, null));
    }

    public void checkAllowed(SubjectContext subject, ResourceContext resource, Action action) {
        AuthorizationDecision decision = authorize(subject, resource, action);
        if (!decision.allowed()) {
            throw new ForbiddenException(decision.reason());
        }
    }
}
