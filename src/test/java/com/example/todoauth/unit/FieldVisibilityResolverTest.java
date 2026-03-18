package com.example.todoauth.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.todoauth.domain.model.FieldDecision;
import com.example.todoauth.domain.service.PolicyEngine;
import com.example.todoauth.domain.valueobject.FieldAccessMode;
import com.example.todoauth.infrastructure.authorization.DataMaskingService;
import com.example.todoauth.infrastructure.authorization.FieldVisibilityResolver;
import com.example.todoauth.support.TestContexts;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FieldVisibilityResolverTest {

    @Mock
    private PolicyEngine policyEngine;

    private FieldVisibilityResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new FieldVisibilityResolver(policyEngine, new DataMaskingService());
    }

    @Test
    void shouldHideObservacaoInternaAndMaskDescricao() {
        when(policyEngine.evaluateField(any()))
                .thenAnswer(invocation -> {
                    var request = invocation.getArgument(0, com.example.todoauth.domain.policy.PolicyEvaluationRequest.class);
                    return switch (request.fieldName()) {
                        case "observacaoInterna" -> new FieldDecision("observacaoInterna", FieldAccessMode.DENY, null, "DENY_INTERNAL");
                        case "descricao" -> new FieldDecision("descricao", FieldAccessMode.MASK, "PARTIAL_TEXT", "MASK_DESCRIPTION");
                        default -> new FieldDecision(request.fieldName(), FieldAccessMode.ALLOW, null, "ALLOW_DEFAULT");
                    };
                });

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("titulo", "Meu título");
        fields.put("descricao", "Descrição muito sensível");
        fields.put("observacaoInterna", "Segredo");

        Map<String, Object> result = resolver.resolve(
                TestContexts.subject(4L, 4L, 1L, 10L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_READ")),
                TestContexts.todo(1L, 1L, 10L, 3L, true),
                fields);

        assertThat(result).containsKey("titulo");
        assertThat(result).containsKey("descricao");
        assertThat(result.get("descricao")).isNotEqualTo("Descrição muito sensível");
        assertThat(result).doesNotContainKey("observacaoInterna");
    }
}
