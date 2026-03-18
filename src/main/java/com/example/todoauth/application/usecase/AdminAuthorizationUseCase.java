package com.example.todoauth.application.usecase;

import com.example.todoauth.application.dto.*;
import java.util.List;

public interface AdminAuthorizationUseCase {
    ResourceResponse createResource(ResourceCreateRequest request);
    List<ResourceResponse> listResources();
    ResourceResponse addField(String resourceType, ResourceFieldCreateRequest request);
    ResourceResponse addFileType(String resourceType, ResourceFileTypeCreateRequest request);
    PolicyResponse createPolicy(PolicyCreateRequest request);
    List<PolicyResponse> listPolicies();
    AuthorizationEvaluateResponse evaluate(AuthorizationEvaluateRequest request);
}
