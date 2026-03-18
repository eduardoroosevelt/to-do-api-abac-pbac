package com.example.todoauth.infrastructure.authorization;

import com.example.todoauth.application.gateway.CurrentSubjectGateway;
import com.example.todoauth.domain.model.SubjectContext;
import com.example.todoauth.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.HashMap;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentSubjectGateway implements CurrentSubjectGateway {
    @Override
    public SubjectContext getCurrentSubject() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (AuthenticatedUserPrincipal) authentication.getPrincipal();
        var attributes = new HashMap<String, Object>();
        attributes.put("linkedPessoaIds", java.util.List.of(principal.getPessoaId()));
        return new SubjectContext(
                principal.getUserId(),
                principal.getPessoaId(),
                principal.getRoles(),
                principal.getPermissions(),
                principal.getTenantId(),
                principal.getEscolaId(),
                null,
                attributes);
    }
}
