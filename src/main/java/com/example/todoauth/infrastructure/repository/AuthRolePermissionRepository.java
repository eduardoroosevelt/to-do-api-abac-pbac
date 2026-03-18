package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthRolePermissionEntity;
import com.example.todoauth.infrastructure.persistence.AuthRolePermissionId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRolePermissionRepository extends JpaRepository<AuthRolePermissionEntity, AuthRolePermissionId> {
    @EntityGraph(attributePaths = {"permission", "role"})
    List<AuthRolePermissionEntity> findByRole_IdIn(Collection<Long> roleIds);
}
