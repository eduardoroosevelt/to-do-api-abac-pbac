package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auth_policy")
public class AuthPolicyEntity extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "policy_code")
    private String policyCode;
    @Column(name = "policy_name")
    private String policyName;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private AuthResourceEntity resource;
    @Column(name = "action_code")
    private String actionCode;
    private String effect;
    private Integer priority;
    private boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applies_to_role_id")
    private AuthRoleEntity appliesToRole;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applies_to_permission_id")
    private AuthPermissionEntity appliesToPermission;
    @Column(name = "scope_level")
    private String scopeLevel;
    @Column(name = "condition_logic")
    private String conditionLogic;
    @Column(name = "valid_from")
    private OffsetDateTime validFrom;
    @Column(name = "valid_until")
    private OffsetDateTime validUntil;
    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthPolicyConditionEntity> conditions = new ArrayList<>();
    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthPolicyFieldTargetEntity> fieldTargets = new ArrayList<>();
    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthPolicyFileTargetEntity> fileTargets = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPolicyCode() { return policyCode; }
    public void setPolicyCode(String policyCode) { this.policyCode = policyCode; }
    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AuthResourceEntity getResource() { return resource; }
    public void setResource(AuthResourceEntity resource) { this.resource = resource; }
    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }
    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public AuthRoleEntity getAppliesToRole() { return appliesToRole; }
    public void setAppliesToRole(AuthRoleEntity appliesToRole) { this.appliesToRole = appliesToRole; }
    public AuthPermissionEntity getAppliesToPermission() { return appliesToPermission; }
    public void setAppliesToPermission(AuthPermissionEntity appliesToPermission) { this.appliesToPermission = appliesToPermission; }
    public String getScopeLevel() { return scopeLevel; }
    public void setScopeLevel(String scopeLevel) { this.scopeLevel = scopeLevel; }
    public String getConditionLogic() { return conditionLogic; }
    public void setConditionLogic(String conditionLogic) { this.conditionLogic = conditionLogic; }
    public OffsetDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(OffsetDateTime validFrom) { this.validFrom = validFrom; }
    public OffsetDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(OffsetDateTime validUntil) { this.validUntil = validUntil; }
    public List<AuthPolicyConditionEntity> getConditions() { return conditions; }
    public List<AuthPolicyFieldTargetEntity> getFieldTargets() { return fieldTargets; }
    public List<AuthPolicyFileTargetEntity> getFileTargets() { return fileTargets; }
}
