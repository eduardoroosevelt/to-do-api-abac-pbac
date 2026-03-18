package com.example.todoauth.infrastructure.authorization;

import com.example.todoauth.domain.model.ResourceContext;
import com.example.todoauth.domain.model.SubjectContext;
import com.example.todoauth.domain.policy.PolicyEvaluationRequest;
import com.example.todoauth.domain.service.PolicyEngine;
import com.example.todoauth.domain.valueobject.Action;
import com.example.todoauth.domain.valueobject.FieldAccessMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FieldVisibilityResolver {
    private final PolicyEngine policyEngine;
    private final DataMaskingService dataMaskingService;

    public FieldVisibilityResolver(PolicyEngine policyEngine, DataMaskingService dataMaskingService) {
        this.policyEngine = policyEngine;
        this.dataMaskingService = dataMaskingService;
    }

    public Map<String, Object> resolve(SubjectContext subject, ResourceContext resource, Map<String, Object> originalFields) {
        Map<String, Object> result = new LinkedHashMap<>();
        originalFields.forEach((field, value) -> {
            var decision = policyEngine.evaluateField(new PolicyEvaluationRequest(subject, resource, Action.VIEW_FIELD, Map.of(), field, null));
            if (decision.mode() == FieldAccessMode.ALLOW) {
                result.put(field, value);
            } else if (decision.mode() == FieldAccessMode.MASK) {
                result.put(field, dataMaskingService.mask(value, decision.maskStrategy()));
            }
        });
        return result;
    }
}
