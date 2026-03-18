package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "auth_user_role")
public class AuthUserRoleEntity {
    @EmbeddedId
    private AuthUserRoleId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UsuarioEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private AuthRoleEntity role;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public AuthUserRoleId getId() { return id; }
    public void setId(AuthUserRoleId id) { this.id = id; }
    public UsuarioEntity getUser() { return user; }
    public void setUser(UsuarioEntity user) { this.user = user; }
    public AuthRoleEntity getRole() { return role; }
    public void setRole(AuthRoleEntity role) { this.role = role; }
}
