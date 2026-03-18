package com.example.todoauth.infrastructure.storage;

import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class InMemoryAttachmentContentService {
    public ByteArrayResource download(TodoAttachmentEntity attachment) {
        String content = "DEMO_ATTACHMENT_CONTENT::" + attachment.getCaminhoStorage();
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }
}
