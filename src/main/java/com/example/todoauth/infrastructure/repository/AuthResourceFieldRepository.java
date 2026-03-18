package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthResourceFieldEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthResourceFieldRepository extends JpaRepository<AuthResourceFieldEntity, Long> {
    List<AuthResourceFieldEntity> findByResource_ResourceTypeAndActiveTrueOrderByIdAsc(String resourceType);
}
