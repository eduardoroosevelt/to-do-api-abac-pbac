package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRoleRepository extends JpaRepository<AuthRoleEntity, Long> {
}
