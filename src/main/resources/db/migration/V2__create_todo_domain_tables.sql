CREATE TABLE pessoa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    documento VARCHAR(50),
    tenant_id BIGINT NOT NULL,
    escola_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(120) NOT NULL,
    email VARCHAR(200) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    pessoa_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    escola_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id)
);

CREATE TABLE auth_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_auth_role_permission PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_auth_role_permission_role FOREIGN KEY (role_id) REFERENCES auth_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_auth_role_permission_permission FOREIGN KEY (permission_id) REFERENCES auth_permission (id) ON DELETE CASCADE
);

CREATE TABLE auth_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_auth_user_role PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_auth_user_role_user FOREIGN KEY (user_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_auth_user_role_role FOREIGN KEY (role_id) REFERENCES auth_role (id) ON DELETE CASCADE
);

CREATE TABLE todo (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    observacao_interna TEXT,
    prioridade VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_limite TIMESTAMPTZ,
    owner_pessoa_id BIGINT NOT NULL,
    escola_id BIGINT,
    tenant_id BIGINT NOT NULL,
    criado_por_usuario_id BIGINT NOT NULL,
    sensivel BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_todo_owner_pessoa FOREIGN KEY (owner_pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT fk_todo_criado_por_usuario FOREIGN KEY (criado_por_usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_todo_prioridade CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE')),
    CONSTRAINT ck_todo_status CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO'))
);

CREATE TABLE todo_attachment (
    id BIGSERIAL PRIMARY KEY,
    todo_id BIGINT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_arquivo VARCHAR(30) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    caminho_storage VARCHAR(500) NOT NULL,
    sensivel BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_todo_attachment_todo FOREIGN KEY (todo_id) REFERENCES todo (id) ON DELETE CASCADE,
    CONSTRAINT ck_todo_attachment_tipo CHECK (tipo_arquivo IN ('CPF', 'RG', 'CONTRATO', 'FOTO', 'COMPROVANTE', 'OUTRO'))
);

CREATE INDEX idx_pessoa_tenant_escola ON pessoa (tenant_id, escola_id);
CREATE INDEX idx_usuario_tenant_escola ON usuario (tenant_id, escola_id);
CREATE INDEX idx_auth_user_role_role ON auth_user_role (role_id, user_id);
CREATE INDEX idx_todo_lookup_tenant_escola ON todo (tenant_id, escola_id, status);
CREATE INDEX idx_todo_owner_lookup ON todo (owner_pessoa_id, tenant_id, escola_id);
CREATE INDEX idx_todo_creator_lookup ON todo (criado_por_usuario_id, tenant_id);
CREATE INDEX idx_todo_sensitive_lookup ON todo (sensivel, tenant_id);
CREATE INDEX idx_todo_attachment_todo_file_type ON todo_attachment (todo_id, tipo_arquivo);
CREATE INDEX idx_todo_attachment_tenant_file_type ON todo_attachment (tenant_id, tipo_arquivo, sensivel);
