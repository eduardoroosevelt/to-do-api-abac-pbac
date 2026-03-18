package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_resource_field")
public class AuthResourceFieldEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private AuthResourceEntity resource;
    @Column(name = "field_name")
    private String fieldName;
    @Column(name = "display_name")
    private String displayName;
    @Column(name = "data_type")
    private String dataType;
    private boolean sensitive;
    @Column(name = "mask_strategy")
    private String maskStrategy;
    @Column(name = "default_visibility")
    private String defaultVisibility;
    private boolean queryable;
    private boolean sortable;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuthResourceEntity getResource() { return resource; }
    public void setResource(AuthResourceEntity resource) { this.resource = resource; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }
    public String getMaskStrategy() { return maskStrategy; }
    public void setMaskStrategy(String maskStrategy) { this.maskStrategy = maskStrategy; }
    public String getDefaultVisibility() { return defaultVisibility; }
    public void setDefaultVisibility(String defaultVisibility) { this.defaultVisibility = defaultVisibility; }
    public boolean isQueryable() { return queryable; }
    public void setQueryable(boolean queryable) { this.queryable = queryable; }
    public boolean isSortable() { return sortable; }
    public void setSortable(boolean sortable) { this.sortable = sortable; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
