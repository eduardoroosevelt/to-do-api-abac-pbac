package com.example.todoauth.infrastructure.authorization;

import com.example.todoauth.domain.model.ResourceContext;
import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import com.example.todoauth.infrastructure.persistence.TodoEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ResourceContextFactory {
    public ResourceContext fromTodo(TodoEntity todo, Set<String> fileTypes) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("tenantId", todo.getTenantId());
        attributes.put("escolaId", todo.getEscolaId());
        attributes.put("ownerPessoaId", todo.getOwnerPessoa().getId());
        attributes.put("criadoPorUsuarioId", todo.getCriadoPorUsuario().getId());
        attributes.put("status", todo.getStatus());
        attributes.put("prioridade", todo.getPrioridade());
        attributes.put("sensivel", todo.isSensivel());

        Map<String, Object> fields = new HashMap<>();
        fields.put("id", todo.getId());
        fields.put("titulo", todo.getTitulo());
        fields.put("descricao", todo.getDescricao());
        fields.put("observacaoInterna", todo.getObservacaoInterna());
        fields.put("prioridade", todo.getPrioridade());
        fields.put("status", todo.getStatus());
        fields.put("dataCriacao", todo.getDataCriacao());
        fields.put("dataLimite", todo.getDataLimite());
        fields.put("ownerPessoaId", todo.getOwnerPessoa().getId());
        fields.put("escolaId", todo.getEscolaId());
        fields.put("tenantId", todo.getTenantId());
        fields.put("criadoPorUsuarioId", todo.getCriadoPorUsuario().getId());
        fields.put("sensivel", todo.isSensivel());

        return new ResourceContext("TODO", todo.getId(), attributes, fields, fileTypes, todo.isSensivel());
    }

    public ResourceContext fromAttachment(TodoAttachmentEntity attachment) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("tenantId", attachment.getTenantId());
        attributes.put("fileType", attachment.getTipoArquivo());
        attributes.put("sensivel", attachment.isSensivel());
        attributes.put("todoId", attachment.getTodo().getId());

        Map<String, Object> fields = new HashMap<>();
        fields.put("nomeArquivo", attachment.getNomeArquivo());
        fields.put("tipoArquivo", attachment.getTipoArquivo());
        fields.put("contentType", attachment.getContentType());
        fields.put("caminhoStorage", attachment.getCaminhoStorage());
        return new ResourceContext("TODO", attachment.getId(), attributes, fields, Set.of(attachment.getTipoArquivo()), attachment.isSensivel());
    }
}
