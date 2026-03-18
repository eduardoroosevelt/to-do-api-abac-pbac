package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_resource_file_type")
public class AuthResourceFileTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private AuthResourceEntity resource;
    @Column(name = "file_type_code")
    private String fileTypeCode;
    @Column(name = "display_name")
    private String displayName;
    private boolean sensitive;
    @Column(name = "default_access_level")
    private String defaultAccessLevel;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuthResourceEntity getResource() { return resource; }
    public void setResource(AuthResourceEntity resource) { this.resource = resource; }
    public String getFileTypeCode() { return fileTypeCode; }
    public void setFileTypeCode(String fileTypeCode) { this.fileTypeCode = fileTypeCode; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }
    public String getDefaultAccessLevel() { return defaultAccessLevel; }
    public void setDefaultAccessLevel(String defaultAccessLevel) { this.defaultAccessLevel = defaultAccessLevel; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
