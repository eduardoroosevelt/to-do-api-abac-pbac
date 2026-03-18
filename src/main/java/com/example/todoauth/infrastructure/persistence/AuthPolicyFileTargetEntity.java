package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_policy_file_target")
public class AuthPolicyFileTargetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private AuthPolicyEntity policy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_file_type_id", nullable = false)
    private AuthResourceFileTypeEntity resourceFileType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuthPolicyEntity getPolicy() { return policy; }
    public void setPolicy(AuthPolicyEntity policy) { this.policy = policy; }
    public AuthResourceFileTypeEntity getResourceFileType() { return resourceFileType; }
    public void setResourceFileType(AuthResourceFileTypeEntity resourceFileType) { this.resourceFileType = resourceFileType; }
}
