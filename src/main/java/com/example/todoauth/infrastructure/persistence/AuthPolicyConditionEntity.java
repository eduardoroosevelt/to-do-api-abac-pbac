package com.example.todoauth.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_policy_condition")
public class AuthPolicyConditionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private AuthPolicyEntity policy;
    @Column(name = "condition_order")
    private Integer conditionOrder;
    @Column(name = "source_type")
    private String sourceType;
    @Column(name = "attribute_name")
    private String attributeName;
    private String operator;
    @Column(name = "comparison_type")
    private String comparisonType;
    @Column(name = "expected_value")
    private String expectedValue;
    @Column(name = "comparison_source_type")
    private String comparisonSourceType;
    @Column(name = "comparison_attribute_name")
    private String comparisonAttributeName;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuthPolicyEntity getPolicy() { return policy; }
    public void setPolicy(AuthPolicyEntity policy) { this.policy = policy; }
    public Integer getConditionOrder() { return conditionOrder; }
    public void setConditionOrder(Integer conditionOrder) { this.conditionOrder = conditionOrder; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getComparisonType() { return comparisonType; }
    public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }
    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
    public String getComparisonSourceType() { return comparisonSourceType; }
    public void setComparisonSourceType(String comparisonSourceType) { this.comparisonSourceType = comparisonSourceType; }
    public String getComparisonAttributeName() { return comparisonAttributeName; }
    public void setComparisonAttributeName(String comparisonAttributeName) { this.comparisonAttributeName = comparisonAttributeName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
