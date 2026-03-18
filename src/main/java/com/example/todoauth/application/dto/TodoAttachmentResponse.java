package com.example.todoauth.application.dto;

public record TodoAttachmentResponse(
        Long id,
        String nomeArquivo,
        String tipoArquivo,
        String contentType,
        String caminhoStorage,
        Boolean downloadable,
        Boolean metadataOnly
) {
}
