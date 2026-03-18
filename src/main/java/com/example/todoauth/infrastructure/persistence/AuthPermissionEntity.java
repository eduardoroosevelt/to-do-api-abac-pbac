package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_permission")
public class AuthPermissionEntity extends BaseAuditableEntity {
    @Id
    private Long id;
    @Column(name = "permission_code", nullable = false)
    private String permissionCode;
    @Column(name = "permission_name", nullable = false)
    private String permissionName;
    private String description;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
