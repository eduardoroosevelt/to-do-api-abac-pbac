package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthPermissionRepository extends JpaRepository<AuthPermissionEntity, Long> {
}
