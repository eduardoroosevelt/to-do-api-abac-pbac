package com.example.todoauth.application.usecase;

import com.example.todoauth.application.dto.*;
import java.util.List;

public interface TodoUseCase {
    TodoResponse create(TodoCreateRequest request);
    TodoResponse getById(Long id);
    List<TodoResponse> list();
    TodoResponse update(Long id, TodoUpdateRequest request);
    void delete(Long id);
    TodoAttachmentResponse addAttachment(Long todoId, TodoAttachmentCreateRequest request);
    List<TodoAttachmentResponse> listAttachments(Long todoId);
    TodoAttachmentResponse getAttachmentMetadata(Long todoId, Long attachmentId);
    org.springframework.core.io.ByteArrayResource downloadAttachment(Long todoId, Long attachmentId);
}
