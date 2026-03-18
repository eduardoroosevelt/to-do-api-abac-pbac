package com.example.todoauth.support;

import com.example.todoauth.infrastructure.persistence.*;
import java.util.Set;

public final class TestPolicyFactory {
    private TestPolicyFactory() {
    }

    public static AuthPolicyEntity policy(String code, String action, String effect, int priority) {
        AuthPolicyEntity policy = new AuthPolicyEntity();
        policy.setId((long) priority);
        policy.setPolicyCode(code);
        policy.setPolicyName(code);
        policy.setActionCode(action);
        policy.setEffect(effect);
        policy.setPriority(priority);
        policy.setActive(true);
        AuthResourceEntity resource = new AuthResourceEntity();
        resource.setId(1L);
        resource.setResourceType("TODO");
        resource.setResourceName("ToDo");
        resource.setActive(true);
        policy.setResource(resource);
        return policy;
    }

    public static AuthPolicyConditionEntity attributeReferenceCondition(String sourceType, String attribute, String operator, String comparisonSource, String comparisonAttribute) {
        AuthPolicyConditionEntity condition = new AuthPolicyConditionEntity();
        condition.setConditionOrder(1);
        condition.setSourceType(sourceType);
        condition.setAttributeName(attribute);
        condition.setOperator(operator);
        condition.setComparisonType("ATTRIBUTE_REFERENCE");
        condition.setComparisonSourceType(comparisonSource);
        condition.setComparisonAttributeName(comparisonAttribute);
        condition.setActive(true);
        return condition;
    }

    public static AuthPolicyConditionEntity literalCondition(String sourceType, String attribute, String operator, String expectedValue) {
        AuthPolicyConditionEntity condition = new AuthPolicyConditionEntity();
        condition.setConditionOrder(1);
        condition.setSourceType(sourceType);
        condition.setAttributeName(attribute);
        condition.setOperator(operator);
        condition.setComparisonType("LITERAL");
        condition.setExpectedValue(expectedValue);
        condition.setActive(true);
        return condition;
    }

    public static AuthPolicyFieldTargetEntity fieldTarget(String fieldName, String maskStrategy) {
        AuthPolicyFieldTargetEntity target = new AuthPolicyFieldTargetEntity();
        AuthResourceFieldEntity field = new AuthResourceFieldEntity();
        field.setFieldName(fieldName);
        field.setMaskStrategy(maskStrategy);
        field.setDefaultVisibility("VISIBLE");
        field.setActive(true);
        target.setResourceField(field);
        return target;
    }

    public static AuthPolicyFileTargetEntity fileTarget(String fileTypeCode) {
        AuthPolicyFileTargetEntity target = new AuthPolicyFileTargetEntity();
        AuthResourceFileTypeEntity fileType = new AuthResourceFileTypeEntity();
        fileType.setFileTypeCode(fileTypeCode);
        fileType.setDefaultAccessLevel("DENY");
        fileType.setActive(true);
        target.setResourceFileType(fileType);
        return target;
    }

    public static void attachConditions(AuthPolicyEntity policy, AuthPolicyConditionEntity... conditions) {
        Set<AuthPolicyConditionEntity> list = policy.getConditions();
        for (AuthPolicyConditionEntity condition : conditions) {
            condition.setPolicy(policy);
            list.add(condition);
        }
    }

    public static void attachFieldTargets(AuthPolicyEntity policy, AuthPolicyFieldTargetEntity... targets) {
        Set<AuthPolicyFieldTargetEntity> list = policy.getFieldTargets();
        for (AuthPolicyFieldTargetEntity target : targets) {
            target.setPolicy(policy);
            list.add(target);
        }
    }

    public static void attachFileTargets(AuthPolicyEntity policy, AuthPolicyFileTargetEntity... targets) {
        Set<AuthPolicyFileTargetEntity> list = policy.getFileTargets();
        for (AuthPolicyFileTargetEntity target : targets) {
            target.setPolicy(policy);
            list.add(target);
        }
    }
}
