package com.example.todoauth.infrastructure.authorization;

import com.example.todoauth.application.dto.TodoAttachmentResponse;
import com.example.todoauth.domain.model.SubjectContext;
import com.example.todoauth.domain.policy.PolicyEvaluationRequest;
import com.example.todoauth.domain.service.PolicyEngine;
import com.example.todoauth.domain.valueobject.Action;
import com.example.todoauth.domain.valueobject.FileAccessMode;
import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FileAccessResolver {
    private final PolicyEngine policyEngine;
    private final ResourceContextFactory resourceContextFactory;

    public FileAccessResolver(PolicyEngine policyEngine, ResourceContextFactory resourceContextFactory) {
        this.policyEngine = policyEngine;
        this.resourceContextFactory = resourceContextFactory;
    }

    public List<TodoAttachmentResponse> resolveList(SubjectContext subject, List<TodoAttachmentEntity> attachments) {
        List<TodoAttachmentResponse> result = new ArrayList<>();
        for (TodoAttachmentEntity attachment : attachments) {
            var context = resourceContextFactory.fromAttachment(attachment);
            var listDecision = policyEngine.evaluateFile(new PolicyEvaluationRequest(subject, context, Action.LIST_FILE, java.util.Map.of(), null, attachment.getTipoArquivo()));
            if (listDecision.mode() == FileAccessMode.HIDE) {
                continue;
            }
            var metadataDecision = policyEngine.evaluateFile(new PolicyEvaluationRequest(subject, context, Action.VIEW_FILE_METADATA, java.util.Map.of(), null, attachment.getTipoArquivo()));
            var downloadDecision = policyEngine.evaluateFile(new PolicyEvaluationRequest(subject, context, Action.DOWNLOAD_FILE, java.util.Map.of(), null, attachment.getTipoArquivo()));
            boolean metadataOnly = metadataDecision.mode() == FileAccessMode.METADATA_ONLY || downloadDecision.mode() != FileAccessMode.DOWNLOAD;
            result.add(new TodoAttachmentResponse(
                    attachment.getId(),
                    attachment.getNomeArquivo(),
                    attachment.getTipoArquivo(),
                    attachment.getContentType(),
                    metadataOnly ? null : attachment.getCaminhoStorage(),
                    downloadDecision.mode() == FileAccessMode.DOWNLOAD,
                    metadataOnly));
        }
        return result;
    }

    public FileAccessMode resolveDownload(SubjectContext subject, TodoAttachmentEntity attachment) {
        var context = resourceContextFactory.fromAttachment(attachment);
        return policyEngine.evaluateFile(new PolicyEvaluationRequest(subject, context, Action.DOWNLOAD_FILE, java.util.Map.of(), null, attachment.getTipoArquivo())).mode();
    }

    public FileAccessMode resolveMetadata(SubjectContext subject, TodoAttachmentEntity attachment) {
        var context = resourceContextFactory.fromAttachment(attachment);
        return policyEngine.evaluateFile(new PolicyEvaluationRequest(subject, context, Action.VIEW_FILE_METADATA, java.util.Map.of(), null, attachment.getTipoArquivo())).mode();
    }
}
