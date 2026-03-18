package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AuthRolePermissionId implements Serializable {
    @Column(name = "role_id")
    private Long roleId;
    @Column(name = "permission_id")
    private Long permissionId;
    public AuthRolePermissionId() {}
    public AuthRolePermissionId(Long roleId, Long permissionId) { this.roleId = roleId; this.permissionId = permissionId; }
    public Long getRoleId() { return roleId; }
    public Long getPermissionId() { return permissionId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof AuthRolePermissionId that)) return false; return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId); }
    @Override public int hashCode() { return Objects.hash(roleId, permissionId); }
}
