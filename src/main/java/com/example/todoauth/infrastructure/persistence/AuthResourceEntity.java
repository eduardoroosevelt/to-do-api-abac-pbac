package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_resource")
public class AuthResourceEntity extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resource_type")
    private String resourceType;
    @Column(name = "resource_name")
    private String resourceName;
    private String description;
    @Column(name = "table_name")
    private String tableName;
    @Column(name = "id_field_name")
    private String idFieldName;
    @Column(name = "tenant_field_name")
    private String tenantFieldName;
    @Column(name = "escola_field_name")
    private String escolaFieldName;
    @Column(name = "owner_field_name")
    private String ownerFieldName;
    @Column(name = "created_by_field_name")
    private String createdByFieldName;
    @Column(name = "sensitivity_field_name")
    private String sensitivityFieldName;
    @Column(name = "supports_attachments")
    private boolean supportsAttachments;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getIdFieldName() { return idFieldName; }
    public void setIdFieldName(String idFieldName) { this.idFieldName = idFieldName; }
    public String getTenantFieldName() { return tenantFieldName; }
    public void setTenantFieldName(String tenantFieldName) { this.tenantFieldName = tenantFieldName; }
    public String getEscolaFieldName() { return escolaFieldName; }
    public void setEscolaFieldName(String escolaFieldName) { this.escolaFieldName = escolaFieldName; }
    public String getOwnerFieldName() { return ownerFieldName; }
    public void setOwnerFieldName(String ownerFieldName) { this.ownerFieldName = ownerFieldName; }
    public String getCreatedByFieldName() { return createdByFieldName; }
    public void setCreatedByFieldName(String createdByFieldName) { this.createdByFieldName = createdByFieldName; }
    public String getSensitivityFieldName() { return sensitivityFieldName; }
    public void setSensitivityFieldName(String sensitivityFieldName) { this.sensitivityFieldName = sensitivityFieldName; }
    public boolean isSupportsAttachments() { return supportsAttachments; }
    public void setSupportsAttachments(boolean supportsAttachments) { this.supportsAttachments = supportsAttachments; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
