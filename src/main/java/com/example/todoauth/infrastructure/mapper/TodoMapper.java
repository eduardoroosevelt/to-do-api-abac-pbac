package com.example.todoauth.infrastructure.mapper;

import com.example.todoauth.application.dto.TodoCreateRequest;
import com.example.todoauth.application.dto.TodoUpdateRequest;
import com.example.todoauth.infrastructure.persistence.PessoaEntity;
import com.example.todoauth.infrastructure.persistence.TodoEntity;
import com.example.todoauth.infrastructure.persistence.UsuarioEntity;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class TodoMapper {
    public TodoEntity toEntity(TodoCreateRequest request, PessoaEntity ownerPessoa, UsuarioEntity criadoPorUsuario, Long tenantId) {
        TodoEntity entity = new TodoEntity();
        apply(entity, request.titulo(), request.descricao(), request.observacaoInterna(), request.prioridade(), request.status(), request.dataLimite(), ownerPessoa, request.escolaId(), request.sensivel(), criadoPorUsuario, tenantId);
        entity.setDataCriacao(OffsetDateTime.now());
        return entity;
    }

    public void updateEntity(TodoEntity entity, TodoUpdateRequest request, PessoaEntity ownerPessoa, Long tenantId) {
        apply(entity, request.titulo(), request.descricao(), request.observacaoInterna(), request.prioridade(), request.status(), request.dataLimite(), ownerPessoa, request.escolaId(), request.sensivel(), entity.getCriadoPorUsuario(), tenantId);
    }

    private void apply(TodoEntity entity, String titulo, String descricao, String observacaoInterna, String prioridade, String status, java.time.OffsetDateTime dataLimite, PessoaEntity ownerPessoa, Long escolaId, Boolean sensivel, UsuarioEntity criadoPorUsuario, Long tenantId) {
        entity.setTitulo(titulo);
        entity.setDescricao(descricao);
        entity.setObservacaoInterna(observacaoInterna);
        entity.setPrioridade(prioridade);
        entity.setStatus(status);
        entity.setDataLimite(dataLimite);
        entity.setOwnerPessoa(ownerPessoa);
        entity.setEscolaId(escolaId);
        entity.setSensivel(Boolean.TRUE.equals(sensivel));
        entity.setCriadoPorUsuario(criadoPorUsuario);
        entity.setTenantId(tenantId);
    }
}
