package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "todo_attachment")
public class TodoAttachmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private TodoEntity todo;
    @Column(name = "nome_arquivo")
    private String nomeArquivo;
    @Column(name = "tipo_arquivo")
    private String tipoArquivo;
    @Column(name = "content_type")
    private String contentType;
    @Column(name = "caminho_storage")
    private String caminhoStorage;
    private boolean sensivel;
    @Column(name = "tenant_id")
    private Long tenantId;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TodoEntity getTodo() { return todo; }
    public void setTodo(TodoEntity todo) { this.todo = todo; }
    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
    public String getTipoArquivo() { return tipoArquivo; }
    public void setTipoArquivo(String tipoArquivo) { this.tipoArquivo = tipoArquivo; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getCaminhoStorage() { return caminhoStorage; }
    public void setCaminhoStorage(String caminhoStorage) { this.caminhoStorage = caminhoStorage; }
    public boolean isSensivel() { return sensivel; }
    public void setSensivel(boolean sensivel) { this.sensivel = sensivel; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
