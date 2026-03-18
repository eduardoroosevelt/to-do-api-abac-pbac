package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoAttachmentRepository extends JpaRepository<TodoAttachmentEntity, Long> {
    @EntityGraph(attributePaths = "todo")
    List<TodoAttachmentEntity> findByTodo_IdOrderByIdAsc(Long todoId);

    @EntityGraph(attributePaths = "todo")
    Optional<TodoAttachmentEntity> findByIdAndTodo_Id(Long id, Long todoId);
}
