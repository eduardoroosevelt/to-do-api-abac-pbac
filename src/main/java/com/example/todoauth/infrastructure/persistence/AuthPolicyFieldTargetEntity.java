package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_policy_field_target")
public class AuthPolicyFieldTargetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private AuthPolicyEntity policy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_field_id", nullable = false)
    private AuthResourceFieldEntity resourceField;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuthPolicyEntity getPolicy() { return policy; }
    public void setPolicy(AuthPolicyEntity policy) { this.policy = policy; }
    public AuthResourceFieldEntity getResourceField() { return resourceField; }
    public void setResourceField(AuthResourceFieldEntity resourceField) { this.resourceField = resourceField; }
}
