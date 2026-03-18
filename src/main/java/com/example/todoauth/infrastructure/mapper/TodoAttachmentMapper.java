package com.example.todoauth.infrastructure.mapper;

import com.example.todoauth.application.dto.TodoAttachmentCreateRequest;
import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import com.example.todoauth.infrastructure.persistence.TodoEntity;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class TodoAttachmentMapper {
    public TodoAttachmentEntity toEntity(TodoAttachmentCreateRequest request, TodoEntity todo) {
        TodoAttachmentEntity entity = new TodoAttachmentEntity();
        entity.setTodo(todo);
        entity.setNomeArquivo(request.nomeArquivo());
        entity.setTipoArquivo(request.tipoArquivo());
        entity.setContentType(request.contentType());
        entity.setCaminhoStorage(request.caminhoStorage());
        entity.setSensivel(Boolean.TRUE.equals(request.sensivel()));
        entity.setTenantId(todo.getTenantId());
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}
