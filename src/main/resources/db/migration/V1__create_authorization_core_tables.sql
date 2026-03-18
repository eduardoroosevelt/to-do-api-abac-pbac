CREATE TABLE auth_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(100) NOT NULL,
    role_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_auth_role_code UNIQUE (role_code)
);

CREATE TABLE auth_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_code VARCHAR(120) NOT NULL,
    permission_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_auth_permission_code UNIQUE (permission_code)
);

CREATE TABLE auth_resource (
    id BIGSERIAL PRIMARY KEY,
    resource_type VARCHAR(100) NOT NULL,
    resource_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    table_name VARCHAR(150),
    id_field_name VARCHAR(100) NOT NULL DEFAULT 'id',
    tenant_field_name VARCHAR(100),
    escola_field_name VARCHAR(100),
    owner_field_name VARCHAR(100),
    created_by_field_name VARCHAR(100),
    sensitivity_field_name VARCHAR(100),
    supports_attachments BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_auth_resource_type UNIQUE (resource_type)
);

CREATE TABLE auth_resource_action (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    action_code VARCHAR(100) NOT NULL,
    action_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_resource_action_resource
        FOREIGN KEY (resource_id) REFERENCES auth_resource (id),
    CONSTRAINT uk_auth_resource_action UNIQUE (resource_id, action_code)
);

CREATE TABLE auth_resource_field (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    mask_strategy VARCHAR(50),
    default_visibility VARCHAR(30) NOT NULL DEFAULT 'VISIBLE',
    queryable BOOLEAN NOT NULL DEFAULT TRUE,
    sortable BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_resource_field_resource
        FOREIGN KEY (resource_id) REFERENCES auth_resource (id),
    CONSTRAINT uk_auth_resource_field UNIQUE (resource_id, field_name),
    CONSTRAINT ck_auth_resource_field_visibility
        CHECK (default_visibility IN ('VISIBLE', 'HIDDEN', 'MASKED'))
);

CREATE TABLE auth_resource_file_type (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    file_type_code VARCHAR(100) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    default_access_level VARCHAR(30) NOT NULL DEFAULT 'DENY',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_resource_file_type_resource
        FOREIGN KEY (resource_id) REFERENCES auth_resource (id),
    CONSTRAINT uk_auth_resource_file_type UNIQUE (resource_id, file_type_code),
    CONSTRAINT ck_auth_resource_file_type_access
        CHECK (default_access_level IN ('ALLOW', 'DENY', 'METADATA_ONLY'))
);

CREATE TABLE auth_resource_relationship (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    relationship_code VARCHAR(100) NOT NULL,
    relationship_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    subject_attribute_name VARCHAR(100) NOT NULL,
    resource_attribute_name VARCHAR(100) NOT NULL,
    resolution_strategy VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_resource_relationship_resource
        FOREIGN KEY (resource_id) REFERENCES auth_resource (id),
    CONSTRAINT uk_auth_resource_relationship UNIQUE (resource_id, relationship_code)
);

CREATE TABLE auth_policy (
    id BIGSERIAL PRIMARY KEY,
    policy_code VARCHAR(120) NOT NULL,
    policy_name VARCHAR(180) NOT NULL,
    description VARCHAR(1000),
    resource_id BIGINT NOT NULL,
    action_code VARCHAR(100) NOT NULL,
    effect VARCHAR(30) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    applies_to_role_id BIGINT,
    applies_to_permission_id BIGINT,
    scope_level VARCHAR(30) NOT NULL DEFAULT 'RESOURCE',
    condition_logic VARCHAR(10) NOT NULL DEFAULT 'AND',
    valid_from TIMESTAMPTZ,
    valid_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_auth_policy_code UNIQUE (policy_code),
    CONSTRAINT fk_auth_policy_resource
        FOREIGN KEY (resource_id) REFERENCES auth_resource (id),
    CONSTRAINT fk_auth_policy_role
        FOREIGN KEY (applies_to_role_id) REFERENCES auth_role (id),
    CONSTRAINT fk_auth_policy_permission
        FOREIGN KEY (applies_to_permission_id) REFERENCES auth_permission (id),
    CONSTRAINT ck_auth_policy_effect
        CHECK (effect IN ('ALLOW', 'DENY', 'MASK', 'METADATA_ONLY')),
    CONSTRAINT ck_auth_policy_scope_level
        CHECK (scope_level IN ('RESOURCE', 'RECORD', 'FIELD', 'FILE')),
    CONSTRAINT ck_auth_policy_condition_logic
        CHECK (condition_logic IN ('AND', 'OR'))
);

CREATE TABLE auth_policy_condition (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    condition_order INTEGER NOT NULL DEFAULT 1,
    source_type VARCHAR(30) NOT NULL,
    attribute_name VARCHAR(120) NOT NULL,
    operator VARCHAR(40) NOT NULL,
    comparison_type VARCHAR(30) NOT NULL DEFAULT 'LITERAL',
    expected_value VARCHAR(1000),
    expected_value_json JSONB,
    comparison_source_type VARCHAR(30),
    comparison_attribute_name VARCHAR(120),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_policy_condition_policy
        FOREIGN KEY (policy_id) REFERENCES auth_policy (id) ON DELETE CASCADE,
    CONSTRAINT ck_auth_policy_condition_source
        CHECK (source_type IN ('SUBJECT', 'RESOURCE', 'CONTEXT')),
    CONSTRAINT ck_auth_policy_condition_operator
        CHECK (operator IN ('EQUALS', 'NOT_EQUALS', 'IN', 'NOT_IN', 'GREATER_THAN', 'LESS_THAN', 'IS_TRUE', 'IS_FALSE', 'CONTAINS')),
    CONSTRAINT ck_auth_policy_condition_comparison_type
        CHECK (comparison_type IN ('LITERAL', 'ATTRIBUTE_REFERENCE')),
    CONSTRAINT ck_auth_policy_condition_comparison_source
        CHECK (comparison_source_type IS NULL OR comparison_source_type IN ('SUBJECT', 'RESOURCE', 'CONTEXT'))
);

CREATE TABLE auth_policy_field_target (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    resource_field_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_policy_field_target_policy
        FOREIGN KEY (policy_id) REFERENCES auth_policy (id) ON DELETE CASCADE,
    CONSTRAINT fk_auth_policy_field_target_field
        FOREIGN KEY (resource_field_id) REFERENCES auth_resource_field (id),
    CONSTRAINT uk_auth_policy_field_target UNIQUE (policy_id, resource_field_id)
);

CREATE TABLE auth_policy_file_target (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    resource_file_type_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_policy_file_target_policy
        FOREIGN KEY (policy_id) REFERENCES auth_policy (id) ON DELETE CASCADE,
    CONSTRAINT fk_auth_policy_file_target_file_type
        FOREIGN KEY (resource_file_type_id) REFERENCES auth_resource_file_type (id),
    CONSTRAINT uk_auth_policy_file_target UNIQUE (policy_id, resource_file_type_id)
);

CREATE INDEX idx_auth_resource_active_type ON auth_resource (active, resource_type);
CREATE INDEX idx_auth_resource_action_lookup ON auth_resource_action (resource_id, action_code, active);
CREATE INDEX idx_auth_resource_field_lookup ON auth_resource_field (resource_id, field_name, sensitive, active);
CREATE INDEX idx_auth_resource_file_type_lookup ON auth_resource_file_type (resource_id, file_type_code, sensitive, active);
CREATE INDEX idx_auth_policy_lookup ON auth_policy (resource_id, action_code, active, priority DESC);
CREATE INDEX idx_auth_policy_role_lookup ON auth_policy (applies_to_role_id, active);
CREATE INDEX idx_auth_policy_permission_lookup ON auth_policy (applies_to_permission_id, active);
CREATE INDEX idx_auth_policy_condition_lookup ON auth_policy_condition (policy_id, active, condition_order);
