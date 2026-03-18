package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "todo")
public class TodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(name = "observacao_interna", columnDefinition = "TEXT")
    private String observacaoInterna;
    private String prioridade;
    private String status;
    @Column(name = "data_criacao")
    private OffsetDateTime dataCriacao;
    @Column(name = "data_limite")
    private OffsetDateTime dataLimite;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_pessoa_id")
    private PessoaEntity ownerPessoa;
    @Column(name = "escola_id")
    private Long escolaId;
    @Column(name = "tenant_id")
    private Long tenantId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_usuario_id")
    private UsuarioEntity criadoPorUsuario;
    private boolean sensivel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getObservacaoInterna() { return observacaoInterna; }
    public void setObservacaoInterna(String observacaoInterna) { this.observacaoInterna = observacaoInterna; }
    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(OffsetDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public OffsetDateTime getDataLimite() { return dataLimite; }
    public void setDataLimite(OffsetDateTime dataLimite) { this.dataLimite = dataLimite; }
    public PessoaEntity getOwnerPessoa() { return ownerPessoa; }
    public void setOwnerPessoa(PessoaEntity ownerPessoa) { this.ownerPessoa = ownerPessoa; }
    public Long getEscolaId() { return escolaId; }
    public void setEscolaId(Long escolaId) { this.escolaId = escolaId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public UsuarioEntity getCriadoPorUsuario() { return criadoPorUsuario; }
    public void setCriadoPorUsuario(UsuarioEntity criadoPorUsuario) { this.criadoPorUsuario = criadoPorUsuario; }
    public boolean isSensivel() { return sensivel; }
    public void setSensivel(boolean sensivel) { this.sensivel = sensivel; }
}
