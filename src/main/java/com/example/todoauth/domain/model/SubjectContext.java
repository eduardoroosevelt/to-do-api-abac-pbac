package com.example.todoauth.domain.model;

import java.util.Map;
import java.util.Set;

public record SubjectContext(
        Long userId,
        Long pessoaId,
        Set<String> roles,
        Set<String> permissions,
        Long tenantId,
        Long escolaId,
        Long orgaoId,
        Map<String, Object> attributes
) {
    public Object getAttribute(String name) {
        return switch (name) {
            case "userId" -> userId;
            case "pessoaId" -> pessoaId;
            case "tenantId" -> tenantId;
            case "escolaId" -> escolaId;
            case "orgaoId" -> orgaoId;
            case "roles" -> roles;
            case "permissions" -> permissions;
            default -> attributes.get(name);
        };
    }
}
