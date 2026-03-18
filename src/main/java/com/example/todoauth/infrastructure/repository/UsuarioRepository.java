package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    @EntityGraph(attributePaths = "pessoa")
    Optional<UsuarioEntity> findById(Long id);
}
