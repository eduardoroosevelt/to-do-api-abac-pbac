package com.example.todoauth.infrastructure.authorization;

import com.example.todoauth.domain.model.AuthorizationDecision;
import com.example.todoauth.domain.model.FieldDecision;
import com.example.todoauth.domain.model.FileDecision;
import com.example.todoauth.domain.policy.PolicyEvaluationRequest;
import com.example.todoauth.domain.service.PolicyEngine;
import com.example.todoauth.domain.valueobject.*;
import com.example.todoauth.infrastructure.persistence.*;
import com.example.todoauth.infrastructure.repository.AuthPolicyRepository;
import com.example.todoauth.infrastructure.repository.AuthResourceFieldRepository;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultPolicyEngine implements PolicyEngine {
    private final AuthPolicyRepository authPolicyRepository;
    private final AuthResourceFieldRepository fieldRepository;

    public DefaultPolicyEngine(AuthPolicyRepository authPolicyRepository, AuthResourceFieldRepository fieldRepository) {
        this.authPolicyRepository = authPolicyRepository;
        this.fieldRepository = fieldRepository;
    }

    @Override
    public AuthorizationDecision evaluate(PolicyEvaluationRequest request) {
        List<AuthPolicyEntity> policies = applicablePolicies(request);
        List<AuthPolicyEntity> matched = policies.stream().filter(policy -> matchesPolicy(policy, request)).toList();
        Optional<AuthPolicyEntity> deny = matched.stream().filter(p -> PolicyEffect.valueOf(p.getEffect()) == PolicyEffect.DENY).findFirst();
        if (deny.isPresent()) {
            return AuthorizationDecision.deny("Denied by policy " + deny.get().getPolicyCode(), matched.stream().map(AuthPolicyEntity::getPolicyCode).toList());
        }
        Optional<AuthPolicyEntity> allow = matched.stream().filter(p -> PolicyEffect.valueOf(p.getEffect()) == PolicyEffect.ALLOW).findFirst();
        if (allow.isPresent()) {
            return AuthorizationDecision.allow("Allowed by policy " + allow.get().getPolicyCode(), matched.stream().map(AuthPolicyEntity::getPolicyCode).toList());
        }
        return AuthorizationDecision.deny("Default deny", matched.stream().map(AuthPolicyEntity::getPolicyCode).toList());
    }

    @Override
    public FieldDecision evaluateField(PolicyEvaluationRequest request) {
        List<AuthPolicyEntity> matched = applicablePolicies(request).stream().filter(policy -> matchesPolicy(policy, request)).toList();
        for (AuthPolicyEntity policy : matched) {
            PolicyEffect effect = PolicyEffect.valueOf(policy.getEffect());
            if (effect == PolicyEffect.DENY) {
                return new FieldDecision(request.fieldName(), FieldAccessMode.DENY, null, policy.getPolicyCode());
            }
            if (effect == PolicyEffect.MASK) {
                String strategy = policy.getFieldTargets().stream().findFirst().map(AuthPolicyFieldTargetEntity::getResourceField).map(AuthResourceFieldEntity::getMaskStrategy).orElse("FIXED_MASK");
                return new FieldDecision(request.fieldName(), FieldAccessMode.MASK, strategy, policy.getPolicyCode());
            }
            if (effect == PolicyEffect.ALLOW) {
                return new FieldDecision(request.fieldName(), FieldAccessMode.ALLOW, null, policy.getPolicyCode());
            }
        }
        AuthResourceFieldEntity metadata = fieldRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc(request.resource().resourceType()).stream()
                .filter(field -> field.getFieldName().equals(request.fieldName()))
                .findFirst()
                .orElse(null);
        if (metadata != null && "HIDDEN".equalsIgnoreCase(metadata.getDefaultVisibility())) {
            return new FieldDecision(request.fieldName(), FieldAccessMode.DENY, metadata.getMaskStrategy(), "metadata-default-hidden");
        }
        return new FieldDecision(request.fieldName(), FieldAccessMode.ALLOW, metadata == null ? null : metadata.getMaskStrategy(), "metadata-default-allow");
    }

    @Override
    public FileDecision evaluateFile(PolicyEvaluationRequest request) {
        List<AuthPolicyEntity> matched = applicablePolicies(request).stream().filter(policy -> matchesPolicy(policy, request)).toList();
        for (AuthPolicyEntity policy : matched) {
            PolicyEffect effect = PolicyEffect.valueOf(policy.getEffect());
            if (effect == PolicyEffect.DENY) {
                return new FileDecision(request.fileType(), FileAccessMode.HIDE, policy.getPolicyCode());
            }
            if (effect == PolicyEffect.METADATA_ONLY) {
                return new FileDecision(request.fileType(), FileAccessMode.METADATA_ONLY, policy.getPolicyCode());
            }
            if (effect == PolicyEffect.ALLOW) {
                return new FileDecision(request.fileType(), FileAccessMode.DOWNLOAD, policy.getPolicyCode());
            }
        }
        return new FileDecision(request.fileType(), FileAccessMode.HIDE, "default-deny");
    }

    @Override
    public List<String> explain(PolicyEvaluationRequest request) {
        return applicablePolicies(request).stream().filter(policy -> matchesPolicy(policy, request)).map(AuthPolicyEntity::getPolicyCode).toList();
    }

    private List<AuthPolicyEntity> applicablePolicies(PolicyEvaluationRequest request) {
        return authPolicyRepository.findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc(
                request.resource().resourceType(), request.action().name()).stream()
                .filter(policy -> rolePermissionApplies(policy, request))
                .filter(policy -> scopeTargetApplies(policy, request))
                .filter(this::isPolicyWithinValidity)
                .collect(Collectors.toList());
    }

    private boolean isPolicyWithinValidity(AuthPolicyEntity policy) {
        OffsetDateTime now = OffsetDateTime.now();
        return (policy.getValidFrom() == null || !policy.getValidFrom().isAfter(now))
                && (policy.getValidUntil() == null || !policy.getValidUntil().isBefore(now));
    }

    private boolean rolePermissionApplies(AuthPolicyEntity policy, PolicyEvaluationRequest request) {
        boolean roleMatches = policy.getAppliesToRole() == null || request.subject().roles().contains(policy.getAppliesToRole().getRoleCode());
        boolean permissionMatches = policy.getAppliesToPermission() == null || request.subject().permissions().contains(policy.getAppliesToPermission().getPermissionCode());
        return roleMatches && permissionMatches;
    }

    private boolean scopeTargetApplies(AuthPolicyEntity policy, PolicyEvaluationRequest request) {
        if (request.fieldName() != null && !policy.getFieldTargets().isEmpty()) {
            return policy.getFieldTargets().stream().anyMatch(t -> request.fieldName().equals(t.getResourceField().getFieldName()));
        }
        if (request.fileType() != null && !policy.getFileTargets().isEmpty()) {
            return policy.getFileTargets().stream().anyMatch(t -> request.fileType().equals(t.getResourceFileType().getFileTypeCode()));
        }
        return request.fieldName() == null || policy.getFieldTargets().isEmpty();
    }

    private boolean matchesPolicy(AuthPolicyEntity policy, PolicyEvaluationRequest request) {
        if (policy.getConditions().isEmpty()) {
            return true;
        }
        List<Boolean> evaluations = policy.getConditions().stream()
                .filter(AuthPolicyConditionEntity::isActive)
                .sorted(Comparator.comparing(AuthPolicyConditionEntity::getConditionOrder))
                .map(condition -> evaluateCondition(condition, request))
                .toList();
        return "OR".equalsIgnoreCase(policy.getConditionLogic())
                ? evaluations.stream().anyMatch(Boolean::booleanValue)
                : evaluations.stream().allMatch(Boolean::booleanValue);
    }

    private boolean evaluateCondition(AuthPolicyConditionEntity condition, PolicyEvaluationRequest request) {
        Object left = readValue(AttributeSource.valueOf(condition.getSourceType()), condition.getAttributeName(), request);
        Object right = condition.getExpectedValue();
        if (ComparisonType.valueOf(condition.getComparisonType()) == ComparisonType.ATTRIBUTE_REFERENCE) {
            right = readValue(AttributeSource.valueOf(condition.getComparisonSourceType()), condition.getComparisonAttributeName(), request);
        }
        Operator operator = Operator.valueOf(condition.getOperator());
        if (operator == Operator.IS_TRUE) {
            return Boolean.TRUE.equals(toBoolean(left));
        }
        if (operator == Operator.IS_FALSE) {
            return Boolean.FALSE.equals(toBoolean(left));
        }
        if (operator == Operator.CONTAINS) {
            return left instanceof Collection<?> collection && collection.contains(right);
        }
        if (operator == Operator.IN || operator == Operator.NOT_IN) {
            List<String> values = right == null ? List.of() : Arrays.stream(String.valueOf(right).split(",")).map(String::trim).toList();
            boolean contained = values.contains(String.valueOf(left));
            return operator == Operator.IN ? contained : !contained;
        }
        Comparable<Object> comparableLeft = left == null ? null : (Comparable<Object>) normalize(left);
        Object comparableRight = normalize(right);
        if (operator == Operator.GREATER_THAN && comparableLeft != null) {
            return comparableLeft.compareTo(comparableRight) > 0;
        }
        if (operator == Operator.LESS_THAN && comparableLeft != null) {
            return comparableLeft.compareTo(comparableRight) < 0;
        }
        boolean equals = Objects.equals(normalize(left), comparableRight);
        return operator == Operator.EQUALS ? equals : !equals;
    }

    private Object readValue(AttributeSource source, String attributeName, PolicyEvaluationRequest request) {
        return switch (source) {
            case SUBJECT -> request.subject().getAttribute(attributeName);
            case RESOURCE -> request.resource().getAttribute(attributeName);
            case CONTEXT -> request.contextAttributes() == null ? null : request.contextAttributes().get(attributeName);
        };
    }

    private Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = String.valueOf(value);
        if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
            return Boolean.valueOf(text);
        }
        try { return Long.parseLong(text); } catch (Exception ignored) { }
        return text;
    }

    private Boolean toBoolean(Object value) {
        return value == null ? null : Boolean.valueOf(String.valueOf(value));
    }
}
