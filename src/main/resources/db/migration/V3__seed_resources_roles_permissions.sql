INSERT INTO auth_role (id, role_code, role_name, description)
VALUES
    (1, 'ADMIN', 'Administrador', 'Acesso administrativo completo'),
    (2, 'GESTOR_ESCOLA', 'Gestor de Escola', 'Gerencia dados da própria escola'),
    (3, 'OPERADOR', 'Operador', 'Opera fluxos usuais do domínio ToDo'),
    (4, 'VISUALIZADOR_RESTRITO', 'Visualizador Restrito', 'Visualização limitada com restrições finas');

INSERT INTO auth_permission (id, permission_code, permission_name, description)
VALUES
    (1, 'TODO_CREATE', 'Criar ToDo', 'Permite criar ToDo'),
    (2, 'TODO_READ', 'Ler ToDo', 'Permite ler um ToDo'),
    (3, 'TODO_UPDATE', 'Atualizar ToDo', 'Permite atualizar ToDo'),
    (4, 'TODO_DELETE', 'Excluir ToDo', 'Permite excluir ToDo'),
    (5, 'TODO_LIST', 'Listar ToDos', 'Permite listar ToDos'),
    (6, 'TODO_ATTACHMENT_LIST', 'Listar anexos de ToDo', 'Permite listar anexos do ToDo'),
    (7, 'TODO_ATTACHMENT_METADATA', 'Ver metadados de anexos', 'Permite ver metadados de anexos'),
    (8, 'TODO_ATTACHMENT_DOWNLOAD', 'Download de anexo', 'Permite baixar anexos'),
    (9, 'ADMIN_RESOURCE_MANAGE', 'Gerenciar recursos', 'Permite gerenciar metadados de recursos'),
    (10, 'ADMIN_POLICY_MANAGE', 'Gerenciar policies', 'Permite gerenciar policies');

INSERT INTO auth_role_permission (role_id, permission_id)
VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
    (2, 1), (2, 2), (2, 3), (2, 5), (2, 6), (2, 7),
    (3, 1), (3, 2), (3, 3), (3, 5), (3, 6), (3, 7), (3, 8),
    (4, 2), (4, 5), (4, 6), (4, 7);

INSERT INTO auth_resource (
    id, resource_type, resource_name, description, table_name, id_field_name,
    tenant_field_name, escola_field_name, owner_field_name, created_by_field_name,
    sensitivity_field_name, supports_attachments
)
VALUES
    (1, 'TODO', 'ToDo', 'Recurso de validação para autorização fina', 'todo', 'id',
     'tenant_id', 'escola_id', 'owner_pessoa_id', 'criado_por_usuario_id', 'sensivel', TRUE);

INSERT INTO auth_resource_action (resource_id, action_code, action_name, description)
VALUES
    (1, 'CREATE', 'Criar', 'Cria um ToDo'),
    (1, 'READ', 'Ler', 'Lê um ToDo específico'),
    (1, 'UPDATE', 'Atualizar', 'Atualiza um ToDo'),
    (1, 'DELETE', 'Excluir', 'Exclui um ToDo'),
    (1, 'LIST', 'Listar', 'Lista ToDos'),
    (1, 'VIEW_FIELD', 'Visualizar campo', 'Visualiza campo do ToDo'),
    (1, 'LIST_FILE', 'Listar anexo', 'Lista anexos do ToDo'),
    (1, 'VIEW_FILE_METADATA', 'Ver metadado de anexo', 'Visualiza metadados do anexo'),
    (1, 'DOWNLOAD_FILE', 'Baixar anexo', 'Baixa conteúdo binário do anexo');

INSERT INTO auth_resource_field (
    id, resource_id, field_name, display_name, data_type, sensitive, mask_strategy, default_visibility, queryable, sortable
)
VALUES
    (1, 1, 'id', 'Identificador', 'LONG', FALSE, NULL, 'VISIBLE', FALSE, TRUE),
    (2, 1, 'titulo', 'Título', 'STRING', FALSE, NULL, 'VISIBLE', TRUE, TRUE),
    (3, 1, 'descricao', 'Descrição', 'TEXT', TRUE, 'PARTIAL_TEXT', 'VISIBLE', TRUE, FALSE),
    (4, 1, 'observacaoInterna', 'Observação Interna', 'TEXT', TRUE, 'NULLIFY', 'HIDDEN', FALSE, FALSE),
    (5, 1, 'prioridade', 'Prioridade', 'STRING', FALSE, NULL, 'VISIBLE', TRUE, TRUE),
    (6, 1, 'status', 'Status', 'STRING', FALSE, NULL, 'VISIBLE', TRUE, TRUE),
    (7, 1, 'dataCriacao', 'Data de Criação', 'DATETIME', FALSE, NULL, 'VISIBLE', TRUE, TRUE),
    (8, 1, 'dataLimite', 'Data Limite', 'DATETIME', FALSE, NULL, 'VISIBLE', TRUE, TRUE),
    (9, 1, 'ownerPessoaId', 'Pessoa Proprietária', 'LONG', FALSE, NULL, 'VISIBLE', TRUE, FALSE),
    (10, 1, 'escolaId', 'Escola', 'LONG', FALSE, NULL, 'VISIBLE', TRUE, FALSE),
    (11, 1, 'tenantId', 'Tenant', 'LONG', FALSE, NULL, 'VISIBLE', TRUE, FALSE),
    (12, 1, 'criadoPorUsuarioId', 'Criado por Usuário', 'LONG', FALSE, NULL, 'VISIBLE', TRUE, FALSE),
    (13, 1, 'sensivel', 'Sensível', 'BOOLEAN', TRUE, NULL, 'HIDDEN', TRUE, FALSE);

INSERT INTO auth_resource_file_type (
    id, resource_id, file_type_code, display_name, sensitive, default_access_level
)
VALUES
    (1, 1, 'CPF', 'CPF', TRUE, 'DENY'),
    (2, 1, 'RG', 'RG', TRUE, 'DENY'),
    (3, 1, 'CONTRATO', 'Contrato', TRUE, 'DENY'),
    (4, 1, 'FOTO', 'Foto', FALSE, 'DENY'),
    (5, 1, 'COMPROVANTE', 'Comprovante', FALSE, 'DENY'),
    (6, 1, 'OUTRO', 'Outro', FALSE, 'DENY');

INSERT INTO auth_resource_relationship (
    resource_id, relationship_code, relationship_name, description,
    subject_attribute_name, resource_attribute_name, resolution_strategy
)
VALUES
    (1, 'OWNER_MATCH', 'Mesmo proprietário', 'Compara pessoa do sujeito com owner do recurso', 'pessoaId', 'ownerPessoaId', 'ATTRIBUTE_EQUALS'),
    (1, 'SAME_ESCOLA', 'Mesma escola', 'Compara escola do sujeito com escola do recurso', 'escolaId', 'escolaId', 'ATTRIBUTE_EQUALS'),
    (1, 'SAME_TENANT', 'Mesmo tenant', 'Compara tenant do sujeito com tenant do recurso', 'tenantId', 'tenantId', 'ATTRIBUTE_EQUALS'),
    (1, 'LINKED_PESSOA', 'Pessoa vinculada', 'Valida se owner do recurso está na lista de pessoas vinculadas do sujeito', 'linkedPessoaIds', 'ownerPessoaId', 'COLLECTION_CONTAINS');


SELECT setval(pg_get_serial_sequence('auth_role', 'id'), COALESCE((SELECT MAX(id) FROM auth_role), 1), TRUE);
SELECT setval(pg_get_serial_sequence('auth_permission', 'id'), COALESCE((SELECT MAX(id) FROM auth_permission), 1), TRUE);
SELECT setval(pg_get_serial_sequence('auth_resource', 'id'), COALESCE((SELECT MAX(id) FROM auth_resource), 1), TRUE);
SELECT setval(pg_get_serial_sequence('auth_resource_field', 'id'), COALESCE((SELECT MAX(id) FROM auth_resource_field), 1), TRUE);
SELECT setval(pg_get_serial_sequence('auth_resource_file_type', 'id'), COALESCE((SELECT MAX(id) FROM auth_resource_file_type), 1), TRUE);
