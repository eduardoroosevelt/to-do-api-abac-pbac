package com.example.todoauth.interfaces.rest;

import com.example.todoauth.application.dto.*;
import com.example.todoauth.application.usecase.TodoUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoUseCase todoUseCase;

    public TodoController(TodoUseCase todoUseCase) {
        this.todoUseCase = todoUseCase;
    }

    @PostMapping
    public TodoResponse create(@Valid @RequestBody TodoCreateRequest request) {
        return todoUseCase.create(request);
    }

    @GetMapping("/{id}")
    public TodoResponse getById(@PathVariable Long id) {
        return todoUseCase.getById(id);
    }

    @GetMapping
    public List<TodoResponse> list() {
        return todoUseCase.list();
    }

    @PutMapping("/{id}")
    public TodoResponse update(@PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request) {
        return todoUseCase.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/attachments")
    public TodoAttachmentResponse addAttachment(@PathVariable Long id, @Valid @RequestBody TodoAttachmentCreateRequest request) {
        return todoUseCase.addAttachment(id, request);
    }

    @GetMapping("/{id}/attachments")
    public List<TodoAttachmentResponse> listAttachments(@PathVariable Long id) {
        return todoUseCase.listAttachments(id);
    }

    @GetMapping("/{id}/attachments/{attachmentId}/metadata")
    public TodoAttachmentResponse getMetadata(@PathVariable Long id, @PathVariable Long attachmentId) {
        return todoUseCase.getAttachmentMetadata(id, attachmentId);
    }

    @GetMapping("/{id}/attachments/{attachmentId}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id, @PathVariable Long attachmentId) {
        ByteArrayResource content = todoUseCase.downloadAttachment(id, attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=demo-attachment.bin")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }
}
