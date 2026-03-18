package com.example.todoauth.unit.util;

import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import com.example.todoauth.infrastructure.persistence.TodoEntity;

public final class TestAttachmentFactory {
    private TestAttachmentFactory() {
    }

    public static TodoAttachmentEntity attachment(Long id, String tipoArquivo) {
        TodoAttachmentEntity entity = new TodoAttachmentEntity();
        entity.setId(id);
        entity.setTipoArquivo(tipoArquivo);
        entity.setNomeArquivo(tipoArquivo.toLowerCase() + ".pdf");
        entity.setContentType("application/pdf");
        entity.setCaminhoStorage("/tmp/" + tipoArquivo.toLowerCase() + ".pdf");
        entity.setTenantId(1L);
        TodoEntity todo = new TodoEntity();
        todo.setId(1L);
        entity.setTodo(todo);
        return entity;
    }
}
