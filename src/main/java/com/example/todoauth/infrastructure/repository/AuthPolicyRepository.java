package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.AuthPolicyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthPolicyRepository extends JpaRepository<AuthPolicyEntity, Long> {
    @EntityGraph(attributePaths = {"resource", "appliesToRole", "appliesToPermission", "conditions", "fieldTargets.resourceField", "fileTargets.resourceFileType"})
    List<AuthPolicyEntity> findByResource_ResourceTypeAndActionCodeAndActiveTrueOrderByPriorityDescIdAsc(String resourceType, String actionCode);

    @EntityGraph(attributePaths = {"resource", "conditions", "fieldTargets.resourceField", "fileTargets.resourceFileType"})
    List<AuthPolicyEntity> findByActiveTrueOrderByPriorityDescIdAsc();
}
