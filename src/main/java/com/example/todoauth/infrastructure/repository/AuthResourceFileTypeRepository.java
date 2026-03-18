package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthResourceFileTypeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthResourceFileTypeRepository extends JpaRepository<AuthResourceFileTypeEntity, Long> {
    List<AuthResourceFileTypeEntity> findByResource_ResourceTypeAndActiveTrueOrderByIdAsc(String resourceType);
}
