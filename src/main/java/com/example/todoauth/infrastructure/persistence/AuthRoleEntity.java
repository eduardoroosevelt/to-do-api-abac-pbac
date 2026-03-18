package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_role")
public class AuthRoleEntity extends BaseAuditableEntity {
    @Id
    private Long id;
    @Column(name = "role_code", nullable = false)
    private String roleCode;
    @Column(name = "role_name", nullable = false)
    private String roleName;
    private String description;
    private boolean active;

    public Long getId() { return id; }
    public String getRoleCode() { return roleCode; }
    public String getRoleName() { return roleName; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public void setId(Long id) { this.id = id; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean active) { this.active = active; }
}
