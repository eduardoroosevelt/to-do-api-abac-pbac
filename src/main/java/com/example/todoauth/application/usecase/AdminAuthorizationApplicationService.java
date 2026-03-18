package com.example.todoauth.application.usecase;

import com.example.todoauth.application.dto.*;
import com.example.todoauth.application.exception.NotFoundException;
import com.example.todoauth.application.gateway.CurrentSubjectGateway;
import com.example.todoauth.domain.model.ResourceContext;
import com.example.todoauth.domain.policy.PolicyEvaluationRequest;
import com.example.todoauth.domain.service.PolicyEngine;
import com.example.todoauth.domain.valueobject.Action;
import com.example.todoauth.infrastructure.persistence.*;
import com.example.todoauth.infrastructure.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminAuthorizationApplicationService implements AdminAuthorizationUseCase {
    private final AuthResourceRepository authResourceRepository;
    private final AuthResourceFieldRepository authResourceFieldRepository;
    private final AuthResourceFileTypeRepository authResourceFileTypeRepository;
    private final AuthPolicyRepository authPolicyRepository;
    private final AuthRoleRepository authRoleRepository;
    private final AuthPermissionRepository authPermissionRepository;
    private final UsuarioRepository usuarioRepository;
    private final TodoRepository todoRepository;
    private final AuthUserRoleRepository authUserRoleRepository;
    private final AuthRolePermissionRepository authRolePermissionRepository;
    private final CurrentSubjectGateway currentSubjectGateway;
    private final PolicyEngine policyEngine;

    public AdminAuthorizationApplicationService(AuthResourceRepository authResourceRepository, AuthResourceFieldRepository authResourceFieldRepository, AuthResourceFileTypeRepository authResourceFileTypeRepository, AuthPolicyRepository authPolicyRepository, AuthRoleRepository authRoleRepository, AuthPermissionRepository authPermissionRepository, UsuarioRepository usuarioRepository, TodoRepository todoRepository, AuthUserRoleRepository authUserRoleRepository, AuthRolePermissionRepository authRolePermissionRepository, CurrentSubjectGateway currentSubjectGateway, PolicyEngine policyEngine) {
        this.authResourceRepository = authResourceRepository;
        this.authResourceFieldRepository = authResourceFieldRepository;
        this.authResourceFileTypeRepository = authResourceFileTypeRepository;
        this.authPolicyRepository = authPolicyRepository;
        this.authRoleRepository = authRoleRepository;
        this.authPermissionRepository = authPermissionRepository;
        this.usuarioRepository = usuarioRepository;
        this.todoRepository = todoRepository;
        this.authUserRoleRepository = authUserRoleRepository;
        this.authRolePermissionRepository = authRolePermissionRepository;
        this.currentSubjectGateway = currentSubjectGateway;
        this.policyEngine = policyEngine;
    }

    @Override
    public ResourceResponse createResource(ResourceCreateRequest request) {
        AuthResourceEntity entity = new AuthResourceEntity();
        entity.setResourceType(request.resourceType());
        entity.setResourceName(request.resourceName());
        entity.setDescription(request.description());
        entity.setTableName(request.tableName());
        entity.setIdFieldName(request.idFieldName() == null ? "id" : request.idFieldName());
        entity.setTenantFieldName(request.tenantFieldName());
        entity.setEscolaFieldName(request.escolaFieldName());
        entity.setOwnerFieldName(request.ownerFieldName());
        entity.setCreatedByFieldName(request.createdByFieldName());
        entity.setSensitivityFieldName(request.sensitivityFieldName());
        entity.setSupportsAttachments(Boolean.TRUE.equals(request.supportsAttachments()));
        entity.setActive(true);
        return toResourceResponse(authResourceRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> listResources() {
        return authResourceRepository.findByActiveTrueOrderByResourceTypeAsc().stream().map(this::toResourceResponse).toList();
    }

    @Override
    public ResourceResponse addField(String resourceType, ResourceFieldCreateRequest request) {
        AuthResourceEntity resource = loadResource(resourceType);
        AuthResourceFieldEntity field = new AuthResourceFieldEntity();
        field.setResource(resource);
        field.setFieldName(request.fieldName());
        field.setDisplayName(request.displayName());
        field.setDataType(request.dataType());
        field.setSensitive(Boolean.TRUE.equals(request.sensitive()));
        field.setMaskStrategy(request.maskStrategy());
        field.setDefaultVisibility(request.defaultVisibility() == null ? "VISIBLE" : request.defaultVisibility());
        field.setQueryable(!Boolean.FALSE.equals(request.queryable()));
        field.setSortable(Boolean.TRUE.equals(request.sortable()));
        field.setActive(true);
        authResourceFieldRepository.save(field);
        return toResourceResponse(resource);
    }

    @Override
    public ResourceResponse addFileType(String resourceType, ResourceFileTypeCreateRequest request) {
        AuthResourceEntity resource = loadResource(resourceType);
        AuthResourceFileTypeEntity fileType = new AuthResourceFileTypeEntity();
        fileType.setResource(resource);
        fileType.setFileTypeCode(request.fileTypeCode());
        fileType.setDisplayName(request.displayName());
        fileType.setSensitive(Boolean.TRUE.equals(request.sensitive()));
        fileType.setDefaultAccessLevel(request.defaultAccessLevel() == null ? "DENY" : request.defaultAccessLevel());
        fileType.setActive(true);
        authResourceFileTypeRepository.save(fileType);
        return toResourceResponse(resource);
    }

    @Override
    public PolicyResponse createPolicy(PolicyCreateRequest request) {
        AuthPolicyEntity policy = new AuthPolicyEntity();
        policy.setPolicyCode(request.policyCode());
        policy.setPolicyName(request.policyName());
        policy.setDescription(request.description());
        policy.setResource(loadResource(request.resourceType()));
        policy.setActionCode(request.actionCode());
        policy.setEffect(request.effect());
        policy.setPriority(request.priority() == null ? 100 : request.priority());
        policy.setActive(!Boolean.FALSE.equals(request.active()));
        policy.setScopeLevel(request.scopeLevel() == null ? "RESOURCE" : request.scopeLevel());
        policy.setConditionLogic(request.conditionLogic() == null ? "AND" : request.conditionLogic());
        if (request.appliesToRoleId() != null) {
            policy.setAppliesToRole(authRoleRepository.findById(request.appliesToRoleId()).orElseThrow(() -> new NotFoundException("Role não encontrada")));
        }
        if (request.appliesToPermissionId() != null) {
            policy.setAppliesToPermission(authPermissionRepository.findById(request.appliesToPermissionId()).orElseThrow(() -> new NotFoundException("Permissão não encontrada")));
        }
        AuthPolicyEntity saved = authPolicyRepository.save(policy);
        if (request.conditions() != null) {
            for (PolicyConditionRequest conditionRequest : request.conditions()) {
                AuthPolicyConditionEntity condition = new AuthPolicyConditionEntity();
                condition.setPolicy(saved);
                condition.setConditionOrder(conditionRequest.conditionOrder() == null ? 1 : conditionRequest.conditionOrder());
                condition.setSourceType(conditionRequest.sourceType());
                condition.setAttributeName(conditionRequest.attributeName());
                condition.setOperator(conditionRequest.operator());
                condition.setComparisonType(conditionRequest.comparisonType() == null ? "LITERAL" : conditionRequest.comparisonType());
                condition.setExpectedValue(conditionRequest.expectedValue());
                condition.setComparisonSourceType(conditionRequest.comparisonSourceType());
                condition.setComparisonAttributeName(conditionRequest.comparisonAttributeName());
                condition.setActive(true);
                saved.getConditions().add(condition);
            }
        }
        if (request.fieldNames() != null) {
            var fields = authResourceFieldRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc(request.resourceType());
            for (String fieldName : request.fieldNames()) {
                for (AuthResourceFieldEntity field : fields) {
                    if (field.getFieldName().equals(fieldName)) {
                        AuthPolicyFieldTargetEntity target = new AuthPolicyFieldTargetEntity();
                        target.setPolicy(saved);
                        target.setResourceField(field);
                        saved.getFieldTargets().add(target);
                        break;
                    }
                }
            }
        }
        if (request.fileTypeCodes() != null) {
            var fileTypes = authResourceFileTypeRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc(request.resourceType());
            for (String fileTypeCode : request.fileTypeCodes()) {
                for (AuthResourceFileTypeEntity fileType : fileTypes) {
                    if (fileType.getFileTypeCode().equals(fileTypeCode)) {
                        AuthPolicyFileTargetEntity target = new AuthPolicyFileTargetEntity();
                        target.setPolicy(saved);
                        target.setResourceFileType(fileType);
                        saved.getFileTargets().add(target);
                        break;
                    }
                }
            }
        }
        saved = authPolicyRepository.save(saved);
        return toPolicyResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyResponse> listPolicies() {
        return authPolicyRepository.findByActiveTrueOrderByPriorityDescIdAsc().stream().map(this::toPolicyResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorizationEvaluateResponse evaluate(AuthorizationEvaluateRequest request) {
        var effectiveSubject = loadSubject(request.userId());
        ResourceContext resource = buildResourceContext(request.resourceType(), request.resourceId());
        var decision = policyEngine.evaluate(new PolicyEvaluationRequest(effectiveSubject, resource, Action.valueOf(request.action()), request.contextAttributes() == null ? Map.of() : request.contextAttributes(), request.fieldName(), request.fileType()));
        return new AuthorizationEvaluateResponse(decision.allowed(), decision.effect().name(), decision.reason(), decision.matchedPolicies());
    }

    private AuthResourceEntity loadResource(String resourceType) {
        return authResourceRepository.findByResourceType(resourceType).orElseThrow(() -> new NotFoundException("Recurso não encontrado"));
    }

    private com.example.todoauth.domain.model.SubjectContext loadSubject(Long userId) {
        var user = usuarioRepository.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        var roles = authUserRoleRepository.findByUser_Id(userId);
        var roleCodes = roles.stream().map(role -> role.getRole().getRoleCode()).collect(Collectors.toSet());
        var roleIds = roles.stream().map(role -> role.getRole().getId()).collect(Collectors.toSet());
        var permissionCodes = authRolePermissionRepository.findByRole_IdIn(roleIds).stream().map(permission -> permission.getPermission().getPermissionCode()).collect(Collectors.toSet());
        return new com.example.todoauth.domain.model.SubjectContext(user.getId(), user.getPessoa().getId(), roleCodes, permissionCodes, user.getTenantId(), user.getEscolaId(), null, Map.of("linkedPessoaIds", List.of(user.getPessoa().getId())));
    }

    private ResourceContext buildResourceContext(String resourceType, Long resourceId) {
        if ("TODO".equalsIgnoreCase(resourceType) && resourceId != null) {
            TodoEntity todo = todoRepository.findById(resourceId).orElseThrow(() -> new NotFoundException("ToDo não encontrado"));
            return new ResourceContext("TODO", todo.getId(), Map.of("tenantId", todo.getTenantId(), "escolaId", todo.getEscolaId(), "ownerPessoaId", todo.getOwnerPessoa().getId(), "sensivel", todo.isSensivel()), Map.of("titulo", todo.getTitulo(), "descricao", todo.getDescricao(), "observacaoInterna", todo.getObservacaoInterna()), java.util.Set.of(), todo.isSensivel());
        }
        return new ResourceContext(resourceType.toUpperCase(), resourceId, Map.of(), Map.of(), java.util.Set.of(), false);
    }

    private ResourceResponse toResourceResponse(AuthResourceEntity entity) {
        return new ResourceResponse(entity.getId(), entity.getResourceType(), entity.getResourceName(), entity.getDescription(), entity.getTableName(), entity.isSupportsAttachments(), entity.isActive());
    }

    private PolicyResponse toPolicyResponse(AuthPolicyEntity entity) {
        return new PolicyResponse(
                entity.getId(),
                entity.getPolicyCode(),
                entity.getPolicyName(),
                entity.getResource().getResourceType(),
                entity.getActionCode(),
                entity.getEffect(),
                entity.getPriority(),
                entity.isActive(),
                entity.getFieldTargets().stream().map(target -> target.getResourceField().getFieldName()).collect(Collectors.toList()),
                entity.getFileTargets().stream().map(target -> target.getResourceFileType().getFileTypeCode()).collect(Collectors.toList()),
                entity.getConditions().stream().map(condition -> condition.getSourceType() + "." + condition.getAttributeName() + " " + condition.getOperator() + " " + (condition.getExpectedValue() != null ? condition.getExpectedValue() : condition.getComparisonSourceType() + "." + condition.getComparisonAttributeName())).collect(Collectors.toList())
        );
    }
}
