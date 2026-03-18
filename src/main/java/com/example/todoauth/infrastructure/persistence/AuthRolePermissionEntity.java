package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "auth_role_permission")
public class AuthRolePermissionEntity {
    @EmbeddedId
    private AuthRolePermissionId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private AuthRoleEntity role;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private AuthPermissionEntity permission;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public AuthRolePermissionId getId() { return id; }
    public void setId(AuthRolePermissionId id) { this.id = id; }
    public AuthRoleEntity getRole() { return role; }
    public void setRole(AuthRoleEntity role) { this.role = role; }
    public AuthPermissionEntity getPermission() { return permission; }
    public void setPermission(AuthPermissionEntity permission) { this.permission = permission; }
}
