package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pessoa")
public class PessoaEntity extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String documento;
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "escola_id")
    private Long escolaId;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getEscolaId() { return escolaId; }
    public void setEscolaId(Long escolaId) { this.escolaId = escolaId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
