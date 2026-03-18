package com.example.todoauth.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.todoauth.application.dto.TodoAttachmentResponse;
import com.example.todoauth.domain.service.PolicyEngine;
import com.example.todoauth.domain.valueobject.FileAccessMode;
import com.example.todoauth.infrastructure.authorization.FileAccessResolver;
import com.example.todoauth.infrastructure.authorization.ResourceContextFactory;
import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import com.example.todoauth.infrastructure.persistence.TodoEntity;
import com.example.todoauth.unit.util.TestAttachmentFactory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileAccessResolverTest {

    @Mock
    private PolicyEngine policyEngine;

    private FileAccessResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new FileAccessResolver(policyEngine, new ResourceContextFactory());
    }

    @Test
    void shouldExposeRgAndHideCpfAndReturnMetadataOnlyForContrato() {
        when(policyEngine.evaluateFile(any())).thenAnswer(invocation -> {
            var request = invocation.getArgument(0, com.example.todoauth.domain.policy.PolicyEvaluationRequest.class);
            if ("CPF".equals(request.fileType())) {
                return new com.example.todoauth.domain.model.FileDecision("CPF", FileAccessMode.HIDE, "DENY_CPF");
            }
            if ("CONTRATO".equals(request.fileType()) && request.action() == com.example.todoauth.domain.valueobject.Action.VIEW_FILE_METADATA) {
                return new com.example.todoauth.domain.model.FileDecision("CONTRATO", FileAccessMode.METADATA_ONLY, "META_ONLY");
            }
            if ("CONTRATO".equals(request.fileType()) && request.action() == com.example.todoauth.domain.valueobject.Action.DOWNLOAD_FILE) {
                return new com.example.todoauth.domain.model.FileDecision("CONTRATO", FileAccessMode.HIDE, "NO_DOWNLOAD");
            }
            return new com.example.todoauth.domain.model.FileDecision(request.fileType(), FileAccessMode.DOWNLOAD, "ALLOW");
        });

        List<TodoAttachmentResponse> result = resolver.resolveList(
                com.example.todoauth.support.TestContexts.subject(4L, 4L, 1L, 10L, java.util.Set.of("VISUALIZADOR_RESTRITO"), java.util.Set.of("TODO_ATTACHMENT_LIST")),
                List.of(
                        TestAttachmentFactory.attachment(1L, "RG"),
                        TestAttachmentFactory.attachment(2L, "CPF"),
                        TestAttachmentFactory.attachment(3L, "CONTRATO")));

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(TodoAttachmentResponse::tipoArquivo)).containsExactly("RG", "CONTRATO");
        assertThat(result.get(1).metadataOnly()).isTrue();
        assertThat(result.get(1).downloadable()).isFalse();
    }
}
