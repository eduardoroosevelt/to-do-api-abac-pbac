package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.TodoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
    @EntityGraph(attributePaths = {"ownerPessoa", "criadoPorUsuario"})
    Optional<TodoEntity> findById(Long id);

    @EntityGraph(attributePaths = {"ownerPessoa", "criadoPorUsuario"})
    List<TodoEntity> findAllByOrderByIdAsc();
}
