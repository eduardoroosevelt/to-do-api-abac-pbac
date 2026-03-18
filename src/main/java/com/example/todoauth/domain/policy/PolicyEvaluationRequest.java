package com.example.todoauth.domain.policy;

import com.example.todoauth.domain.model.ResourceContext;
import com.example.todoauth.domain.model.SubjectContext;
import com.example.todoauth.domain.valueobject.Action;
import java.util.Map;

public record PolicyEvaluationRequest(
        SubjectContext subject,
        ResourceContext resource,
        Action action,
        Map<String, Object> contextAttributes,
        String fieldName,
        String fileType
) {
}
