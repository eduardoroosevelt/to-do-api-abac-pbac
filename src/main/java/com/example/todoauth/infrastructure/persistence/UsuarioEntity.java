package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class UsuarioEntity extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    @Column(name = "senha_hash")
    private String senhaHash;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private PessoaEntity pessoa;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "escola_id")
    private Long escolaId;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public PessoaEntity getPessoa() { return pessoa; }
    public void setPessoa(PessoaEntity pessoa) { this.pessoa = pessoa; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getEscolaId() { return escolaId; }
    public void setEscolaId(Long escolaId) { this.escolaId = escolaId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
