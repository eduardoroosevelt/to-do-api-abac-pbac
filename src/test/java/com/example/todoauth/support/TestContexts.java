package com.example.todoauth.support;

import com.example.todoauth.domain.model.ResourceContext;
import com.example.todoauth.domain.model.SubjectContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class TestContexts {
    private TestContexts() {
    }

    public static SubjectContext subject(Long userId, Long pessoaId, Long tenantId, Long escolaId, Set<String> roles, Set<String> permissions) {
        return new SubjectContext(userId, pessoaId, roles, permissions, tenantId, escolaId, null, Map.of("linkedPessoaIds", java.util.List.of(pessoaId)));
    }

    public static ResourceContext todo(Long id, Long tenantId, Long escolaId, Long ownerPessoaId, boolean sensivel) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("tenantId", tenantId);
        attributes.put("escolaId", escolaId);
        attributes.put("ownerPessoaId", ownerPessoaId);
        attributes.put("sensivel", sensivel);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("titulo", "Título");
        fields.put("descricao", "Descrição sensível");
        fields.put("observacaoInterna", "Observação interna");
        return new ResourceContext("TODO", id, attributes, fields, Set.of("RG", "CPF"), sensivel);
    }
}
