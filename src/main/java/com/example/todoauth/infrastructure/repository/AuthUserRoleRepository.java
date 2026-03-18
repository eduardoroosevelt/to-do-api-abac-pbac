package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthUserRoleEntity;
import com.example.todoauth.infrastructure.persistence.AuthUserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRoleRepository extends JpaRepository<AuthUserRoleEntity, AuthUserRoleId> {
    @EntityGraph(attributePaths = "role")
    List<AuthUserRoleEntity> findByUser_Id(Long userId);
}
