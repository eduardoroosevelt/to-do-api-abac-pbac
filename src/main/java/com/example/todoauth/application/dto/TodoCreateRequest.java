package com.example.todoauth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record TodoCreateRequest(
        @NotBlank String titulo,
        String descricao,
        String observacaoInterna,
        @NotBlank String prioridade,
        @NotBlank String status,
        OffsetDateTime dataLimite,
        @NotNull Long ownerPessoaId,
        Long escolaId,
        Boolean sensivel
) {
}
