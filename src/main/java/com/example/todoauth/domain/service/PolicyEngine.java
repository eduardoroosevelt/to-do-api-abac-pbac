package com.example.todoauth.domain.service;

import com.example.todoauth.domain.model.AuthorizationDecision;
import com.example.todoauth.domain.model.FieldDecision;
import com.example.todoauth.domain.model.FileDecision;
import com.example.todoauth.domain.policy.PolicyEvaluationRequest;
import java.util.List;

public interface PolicyEngine {
    AuthorizationDecision evaluate(PolicyEvaluationRequest request);

    FieldDecision evaluateField(PolicyEvaluationRequest request);

    FileDecision evaluateFile(PolicyEvaluationRequest request);

    List<String> explain(PolicyEvaluationRequest request);
}
