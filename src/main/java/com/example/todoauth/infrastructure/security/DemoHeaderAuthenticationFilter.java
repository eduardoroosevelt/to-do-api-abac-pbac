package com.example.todoauth.infrastructure.security;

import com.example.todoauth.application.exception.NotFoundException;
import com.example.todoauth.infrastructure.persistence.AuthRolePermissionEntity;
import com.example.todoauth.infrastructure.persistence.AuthUserRoleEntity;
import com.example.todoauth.infrastructure.persistence.UsuarioEntity;
import com.example.todoauth.infrastructure.repository.AuthRolePermissionRepository;
import com.example.todoauth.infrastructure.repository.AuthUserRoleRepository;
import com.example.todoauth.infrastructure.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DemoHeaderAuthenticationFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarioRepository;
    private final AuthUserRoleRepository authUserRoleRepository;
    private final AuthRolePermissionRepository authRolePermissionRepository;
    private final String headerName;

    public DemoHeaderAuthenticationFilter(
            UsuarioRepository usuarioRepository,
            AuthUserRoleRepository authUserRoleRepository,
            AuthRolePermissionRepository authRolePermissionRepository,
            @Value("${app.security.demo-header-name:X-User-Id}") String headerName) {
        this.usuarioRepository = usuarioRepository;
        this.authUserRoleRepository = authUserRoleRepository;
        this.authRolePermissionRepository = authRolePermissionRepository;
        this.headerName = headerName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String rawUserId = request.getHeader(headerName);
            Long userId = rawUserId == null || rawUserId.isBlank() ? 1L : Long.valueOf(rawUserId);
            UsuarioEntity user = usuarioRepository.findById(userId).orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado"));
            var userRoles = authUserRoleRepository.findByUser_Id(userId);
            Set<String> roles = userRoles.stream().map(AuthUserRoleEntity::getRole).map(r -> r.getRoleCode()).collect(Collectors.toSet());
            Set<Long> roleIds = userRoles.stream().map(r -> r.getRole().getId()).collect(Collectors.toSet());
            Set<String> permissions = authRolePermissionRepository.findByRole_IdIn(roleIds).stream()
                    .map(AuthRolePermissionEntity::getPermission)
                    .map(p -> p.getPermissionCode())
                    .collect(Collectors.toSet());
            AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
                    user.getId(),
                    user.getPessoa().getId(),
                    user.getTenantId(),
                    user.getEscolaId(),
                    roles,
                    permissions,
                    user.getUsername());
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
