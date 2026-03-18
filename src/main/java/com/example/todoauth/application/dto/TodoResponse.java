package com.example.todoauth.application.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record TodoResponse(
        Long id,
        Map<String, Object> fields,
        List<TodoAttachmentResponse> attachments,
        OffsetDateTime dataCriacao,
        OffsetDateTime dataLimite
) {
}
