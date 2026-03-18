package com.example.todoauth.interfaces.rest;

import com.example.todoauth.application.dto.*;
import com.example.todoauth.application.usecase.AdminAuthorizationUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthorizationController {
    private final AdminAuthorizationUseCase adminAuthorizationUseCase;

    public AdminAuthorizationController(AdminAuthorizationUseCase adminAuthorizationUseCase) {
        this.adminAuthorizationUseCase = adminAuthorizationUseCase;
    }

    @PostMapping("/resources")
    public ResourceResponse createResource(@Valid @RequestBody ResourceCreateRequest request) {
        return adminAuthorizationUseCase.createResource(request);
    }

    @GetMapping("/resources")
    public List<ResourceResponse> listResources() {
        return adminAuthorizationUseCase.listResources();
    }

    @PostMapping("/resources/{resourceType}/fields")
    public ResourceResponse addField(@PathVariable String resourceType, @Valid @RequestBody ResourceFieldCreateRequest request) {
        return adminAuthorizationUseCase.addField(resourceType, request);
    }

    @PostMapping("/resources/{resourceType}/file-types")
    public ResourceResponse addFileType(@PathVariable String resourceType, @Valid @RequestBody ResourceFileTypeCreateRequest request) {
        return adminAuthorizationUseCase.addFileType(resourceType, request);
    }

    @PostMapping("/policies")
    public PolicyResponse createPolicy(@Valid @RequestBody PolicyCreateRequest request) {
        return adminAuthorizationUseCase.createPolicy(request);
    }

    @GetMapping("/policies")
    public List<PolicyResponse> listPolicies() {
        return adminAuthorizationUseCase.listPolicies();
    }

    @PostMapping("/authorization/evaluate")
    public AuthorizationEvaluateResponse evaluate(@Valid @RequestBody AuthorizationEvaluateRequest request) {
        return adminAuthorizationUseCase.evaluate(request);
    }
}
