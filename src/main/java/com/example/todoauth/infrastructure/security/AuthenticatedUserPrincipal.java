package com.example.todoauth.infrastructure.security;

import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUserPrincipal implements UserDetails {
    private final Long userId;
    private final Long pessoaId;
    private final Long tenantId;
    private final Long escolaId;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final String username;

    public AuthenticatedUserPrincipal(Long userId, Long pessoaId, Long tenantId, Long escolaId, Set<String> roles, Set<String> permissions, String username) {
        this.userId = userId;
        this.pessoaId = pessoaId;
        this.tenantId = tenantId;
        this.escolaId = escolaId;
        this.roles = roles;
        this.permissions = permissions;
        this.username = username;
    }

    public Long getUserId() { return userId; }
    public Long getPessoaId() { return pessoaId; }
    public Long getTenantId() { return tenantId; }
    public Long getEscolaId() { return escolaId; }
    public Set<String> getRoles() { return roles; }
    public Set<String> getPermissions() { return permissions; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }

    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
