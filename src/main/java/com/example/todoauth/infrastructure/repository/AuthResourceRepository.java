package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthResourceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthResourceRepository extends JpaRepository<AuthResourceEntity, Long> {
    Optional<AuthResourceEntity> findByResourceType(String resourceType);
    List<AuthResourceEntity> findByActiveTrueOrderByResourceTypeAsc();
}
