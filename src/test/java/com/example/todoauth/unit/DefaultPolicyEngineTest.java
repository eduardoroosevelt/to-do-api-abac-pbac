package com.example.todoauth.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.todoauth.domain.policy.PolicyEvaluationRequest;
import com.example.todoauth.domain.valueobject.Action;
import com.example.todoauth.domain.valueobject.FieldAccessMode;
import com.example.todoauth.domain.valueobject.FileAccessMode;
import com.example.todoauth.infrastructure.authorization.DefaultPolicyEngine;
import com.example.todoauth.infrastructure.persistence.AuthResourceFieldEntity;
import com.example.todoauth.infrastructure.repository.AuthPolicyRepository;
import com.example.todoauth.infrastructure.repository.AuthResourceFieldRepository;
import com.example.todoauth.support.TestContexts;
import com.example.todoauth.support.TestPolicyFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultPolicyEngineTest {

    @Mock
    private AuthPolicyRepository authPolicyRepository;
    @Mock
    private AuthResourceFieldRepository authResourceFieldRepository;

    private DefaultPolicyEngine policyEngine;

    @BeforeEach
    void setUp() {
        policyEngine = new DefaultPolicyEngine(authPolicyRepository, authResourceFieldRepository);
    }

    @Test
    void shouldDenyWhenMacroRoleExistsButFinePolicyIsMissing() {
        when(authPolicyRepository.findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc("TODO", "READ"))
                .thenReturn(List.of());

        var decision = policyEngine.evaluate(new PolicyEvaluationRequest(
                TestContexts.subject(4L, 4L, 1L, 10L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_READ")),
                TestContexts.todo(1L, 1L, 10L, 3L, false),
                Action.READ,
                Map.of(),
                null,
                null));

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reason()).isEqualTo("Default deny");
    }

    @Test
    void shouldApplyDenyOverAllowWhenConflictExists() {
        var allow = TestPolicyFactory.policy("ALLOW_READ", "READ", "ALLOW", 100);
        var deny = TestPolicyFactory.policy("DENY_OTHER_TENANT", "READ", "DENY", 900);
        TestPolicyFactory.attachConditions(deny,
                TestPolicyFactory.attributeReferenceCondition("SUBJECT", "tenantId", "NOT_EQUALS", "RESOURCE", "tenantId"));
        when(authPolicyRepository.findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc("TODO", "READ"))
                .thenReturn(List.of(deny, allow));

        var decision = policyEngine.evaluate(new PolicyEvaluationRequest(
                TestContexts.subject(6L, 6L, 2L, 30L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_READ")),
                TestContexts.todo(1L, 1L, 10L, 3L, false),
                Action.READ,
                Map.of(),
                null,
                null));

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reason()).contains("DENY_OTHER_TENANT");
    }

    @Test
    void shouldResolveByPriorityForReadPolicies() {
        var lowPriority = TestPolicyFactory.policy("ALLOW_LOW", "READ", "ALLOW", 100);
        var highPriority = TestPolicyFactory.policy("ALLOW_HIGH", "READ", "ALLOW", 500);
        when(authPolicyRepository.findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc("TODO", "READ"))
                .thenReturn(List.of(highPriority, lowPriority));

        var decision = policyEngine.evaluate(new PolicyEvaluationRequest(
                TestContexts.subject(2L, 2L, 1L, 10L, java.util.Set.of("GESTOR_ESCOLA"), java.util.Set.of("TODO_READ")),
                TestContexts.todo(1L, 1L, 10L, 3L, false),
                Action.READ,
                Map.of(),
                null,
                null));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.reason()).contains("ALLOW_HIGH");
    }

    @Test
    void shouldMaskSensitiveFieldWhenMaskPolicyMatches() {
        var maskPolicy = TestPolicyFactory.policy("MASK_DESCRIPTION", "VIEW_FIELD", "MASK", 850);
        TestPolicyFactory.attachConditions(maskPolicy, TestPolicyFactory.literalCondition("RESOURCE", "sensivel", "IS_TRUE", null));
        TestPolicyFactory.attachFieldTargets(maskPolicy, TestPolicyFactory.fieldTarget("descricao", "PARTIAL_TEXT"));
        when(authPolicyRepository.findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc("TODO", "VIEW_FIELD"))
                .thenReturn(List.of(maskPolicy));

        var decision = policyEngine.evaluateField(new PolicyEvaluationRequest(
                TestContexts.subject(4L, 4L, 1L, 10L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_READ")),
                TestContexts.todo(1L, 1L, 10L, 3L, true),
                Action.VIEW_FIELD,
                Map.of(),
                "descricao",
                null));

        assertThat(decision.mode()).isEqualTo(FieldAccessMode.MASK);
        assertThat(decision.maskStrategy()).isEqualTo("PARTIAL_TEXT");
    }

    @Test
    void shouldHideFieldWhenMetadataDefaultsToHidden() {
        AuthResourceFieldEntity metadata = new AuthResourceFieldEntity();
        metadata.setFieldName("observacaoInterna");
        metadata.setDefaultVisibility("HIDDEN");
        metadata.setMaskStrategy("NULLIFY");
        metadata.setActive(true);
        when(authPolicyRepository.findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc("TODO", "VIEW_FIELD"))
                .thenReturn(List.of());
        when(authResourceFieldRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc("TODO"))
                .thenReturn(List.of(metadata));

        var decision = policyEngine.evaluateField(new PolicyEvaluationRequest(
                TestContexts.subject(4L, 4L, 1L, 10L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_READ")),
                TestContexts.todo(1L, 1L, 10L, 3L, false),
                Action.VIEW_FIELD,
                Map.of(),
                "observacaoInterna",
                null));

        assertThat(decision.mode()).isEqualTo(FieldAccessMode.DENY);
    }

    @Test
    void shouldAllowRgAndHideCpfForFiles() {
        var allowRg = TestPolicyFactory.policy("ALLOW_RG", "DOWNLOAD_FILE", "ALLOW", 300);
        TestPolicyFactory.attachFileTargets(allowRg, TestPolicyFactory.fileTarget("RG"));
        var denyCpf = TestPolicyFactory.policy("DENY_CPF", "DOWNLOAD_FILE", "DENY", 900);
        TestPolicyFactory.attachConditions(denyCpf, TestPolicyFactory.literalCondition("RESOURCE", "fileType", "EQUALS", "CPF"));
        TestPolicyFactory.attachFileTargets(denyCpf, TestPolicyFactory.fileTarget("CPF"));
        when(authPolicyRepository.findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc("TODO", "DOWNLOAD_FILE"))
                .thenReturn(List.of(denyCpf, allowRg));

        var rgDecision = policyEngine.evaluateFile(new PolicyEvaluationRequest(
                TestContexts.subject(4L, 4L, 1L, 10L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_ATTACHMENT_DOWNLOAD")),
                TestContexts.todo(1L, 1L, 10L, 3L, false),
                Action.DOWNLOAD_FILE,
                Map.of(),
                null,
                "RG"));
        var cpfDecision = policyEngine.evaluateFile(new PolicyEvaluationRequest(
                TestContexts.subject(4L, 4L, 1L, 10L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_ATTACHMENT_DOWNLOAD")),
                TestContexts.todo(1L, 1L, 10L, 3L, false),
                Action.DOWNLOAD_FILE,
                Map.of(),
                null,
                "CPF"));

        assertThat(rgDecision.mode()).isEqualTo(FileAccessMode.DOWNLOAD);
        assertThat(cpfDecision.mode()).isEqualTo(FileAccessMode.HIDE);
    }
}
