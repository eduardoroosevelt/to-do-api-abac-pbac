package com.example.todoauth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TodoAttachmentCreateRequest(
        @NotBlank String nomeArquivo,
        @NotBlank String tipoArquivo,
        @NotBlank String contentType,
        @NotBlank String caminhoStorage,
        Boolean sensivel
) {
}
