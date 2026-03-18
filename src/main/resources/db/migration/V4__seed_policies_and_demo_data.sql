INSERT INTO pessoa (id, nome, documento, tenant_id, escola_id)
VALUES
    (1, 'Alice Admin', '11111111111', 1, 10),
    (2, 'Gustavo Gestor', '22222222222', 1, 10),
    (3, 'Olivia Operadora', '33333333333', 1, 10),
    (4, 'Victor Visualizador', '44444444444', 1, 10),
    (5, 'Paula Outra Escola', '55555555555', 1, 20),
    (6, 'Tiago Outro Tenant', '66666666666', 2, 30);

INSERT INTO usuario (id, username, email, senha_hash, pessoa_id, tenant_id, escola_id)
VALUES
    (1, 'admin', 'admin@example.com', '{noop}admin', 1, 1, 10),
    (2, 'gestor.escola', 'gestor@example.com', '{noop}gestor', 2, 1, 10),
    (3, 'operador', 'operador@example.com', '{noop}operador', 3, 1, 10),
    (4, 'visual.restrito', 'visual@example.com', '{noop}visual', 4, 1, 10),
    (5, 'outra.escola', 'outra.escola@example.com', '{noop}outra', 5, 1, 20),
    (6, 'outro.tenant', 'outro.tenant@example.com', '{noop}tenant', 6, 2, 30);

INSERT INTO auth_user_role (user_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 4),
    (6, 4);

INSERT INTO todo (
    id, titulo, descricao, observacao_interna, prioridade, status, data_criacao, data_limite,
    owner_pessoa_id, escola_id, tenant_id, criado_por_usuario_id, sensivel
)
VALUES
    (1, 'Renovar matrícula', 'Validar documentação da matrícula do aluno.', 'Pendência interna da secretaria.', 'ALTA', 'ABERTO', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP + INTERVAL '7 days', 3, 10, 1, 2, FALSE),
    (2, 'Conferir contrato', 'Contrato contém cláusulas sensíveis e dados pessoais.', 'Somente coordenação pode ler integralmente.', 'URGENTE', 'EM_ANDAMENTO', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '5 days', 4, 10, 1, 2, TRUE),
    (3, 'Atualizar cadastro da escola B', 'ToDo da escola 20 no mesmo tenant.', 'Escola diferente.', 'MEDIA', 'ABERTO', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '10 days', 5, 20, 1, 5, FALSE),
    (4, 'Processar integração tenant 2', 'ToDo pertencente a outro tenant.', 'Tenant isolado.', 'ALTA', 'ABERTO', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP + INTERVAL '12 days', 6, 30, 2, 6, FALSE),
    (5, 'Tarefa própria do operador', 'ToDo do próprio operador para regra owner.', 'Observação privada do operador.', 'BAIXA', 'CONCLUIDO', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 3, 10, 1, 3, FALSE);

INSERT INTO todo_attachment (
    id, todo_id, nome_arquivo, tipo_arquivo, content_type, caminho_storage, sensivel, tenant_id)
VALUES
    (1, 1, 'rg-aluno.pdf', 'RG', 'application/pdf', '/storage/todos/1/rg-aluno.pdf', FALSE, 1),
    (2, 1, 'cpf-aluno.pdf', 'CPF', 'application/pdf', '/storage/todos/1/cpf-aluno.pdf', TRUE, 1),
    (3, 2, 'contrato-sensivel.pdf', 'CONTRATO', 'application/pdf', '/storage/todos/2/contrato-sensivel.pdf', TRUE, 1),
    (4, 2, 'foto-local.png', 'FOTO', 'image/png', '/storage/todos/2/foto-local.png', FALSE, 1),
    (5, 3, 'comprovante-endereco.pdf', 'COMPROVANTE', 'application/pdf', '/storage/todos/3/comprovante-endereco.pdf', FALSE, 1),
    (6, 4, 'rg-tenant2.pdf', 'RG', 'application/pdf', '/storage/todos/4/rg-tenant2.pdf', FALSE, 2);

INSERT INTO auth_policy (
    id, policy_code, policy_name, description, resource_id, action_code, effect, priority, active,
    applies_to_role_id, applies_to_permission_id, scope_level, condition_logic
)
VALUES
    (1, 'TODO_ADMIN_FULL_ACCESS_READ', 'Admin full access read', 'Administrador com acesso irrestrito de leitura ao recurso ToDo', 1, 'READ', 'ALLOW', 1000, TRUE, 1, NULL, 'RESOURCE', 'AND'),
    (2, 'TODO_ADMIN_FULL_ACCESS_LIST', 'Admin full access list', 'Administrador lista todos os ToDos', 1, 'LIST', 'ALLOW', 1000, TRUE, 1, NULL, 'RESOURCE', 'AND'),
    (3, 'TODO_ADMIN_FIELD_ACCESS', 'Admin field access', 'Administrador pode visualizar todos os campos', 1, 'VIEW_FIELD', 'ALLOW', 1000, TRUE, 1, NULL, 'FIELD', 'AND'),
    (4, 'TODO_ADMIN_FILE_LIST', 'Admin file list', 'Administrador pode listar anexos', 1, 'LIST_FILE', 'ALLOW', 1000, TRUE, 1, NULL, 'FILE', 'AND'),
    (5, 'TODO_ADMIN_FILE_METADATA', 'Admin file metadata', 'Administrador pode ver metadados de anexos', 1, 'VIEW_FILE_METADATA', 'ALLOW', 1000, TRUE, 1, NULL, 'FILE', 'AND'),
    (6, 'TODO_ADMIN_FILE_DOWNLOAD', 'Admin file download', 'Administrador pode baixar anexos', 1, 'DOWNLOAD_FILE', 'ALLOW', 1000, TRUE, 1, NULL, 'FILE', 'AND'),
    (7, 'TODO_VISUAL_BASE_READ', 'Visualizador leitura base', 'Visualizador restrito pode ler ToDos', 1, 'READ', 'ALLOW', 100, TRUE, 4, 2, 'RESOURCE', 'AND'),
    (8, 'TODO_VISUAL_BASE_LIST', 'Visualizador lista base', 'Visualizador restrito pode listar ToDos', 1, 'LIST', 'ALLOW', 100, TRUE, 4, 5, 'RESOURCE', 'AND'),
    (9, 'TODO_GESTOR_SAME_ESCOLA_READ', 'Gestor lê mesma escola', 'Gestor lê ToDos apenas da mesma escola', 1, 'READ', 'ALLOW', 300, TRUE, 2, 2, 'RECORD', 'AND'),
    (10, 'TODO_GESTOR_SAME_ESCOLA_LIST', 'Gestor lista mesma escola', 'Gestor lista ToDos apenas da mesma escola', 1, 'LIST', 'ALLOW', 300, TRUE, 2, 5, 'RECORD', 'AND'),
    (11, 'TODO_GESTOR_DENY_OTHER_ESCOLA_READ', 'Gestor nega outra escola no READ', 'Nega leitura do gestor quando a escola diverge', 1, 'READ', 'DENY', 900, TRUE, 2, NULL, 'RECORD', 'AND'),
    (12, 'TODO_GESTOR_DENY_OTHER_ESCOLA_LIST', 'Gestor nega outra escola no LIST', 'Nega listagem do gestor quando a escola diverge', 1, 'LIST', 'DENY', 900, TRUE, 2, NULL, 'RECORD', 'AND'),
    (13, 'TODO_DENY_OTHER_TENANT_READ', 'Negar outro tenant no READ', 'Nega leitura quando tenant do sujeito diverge do recurso', 1, 'READ', 'DENY', 950, TRUE, NULL, NULL, 'RECORD', 'AND'),
    (14, 'TODO_DENY_OTHER_TENANT_LIST', 'Negar outro tenant no LIST', 'Nega listagem quando tenant do sujeito diverge do recurso', 1, 'LIST', 'DENY', 950, TRUE, NULL, NULL, 'RECORD', 'AND'),
    (15, 'TODO_DENY_INTERNAL_NOTE', 'Negar observação interna', 'Visualizador restrito não visualiza observação interna', 1, 'VIEW_FIELD', 'DENY', 800, TRUE, 4, NULL, 'FIELD', 'AND'),
    (16, 'TODO_ALLOW_TITLE', 'Permitir título', 'Visualizador restrito visualiza título', 1, 'VIEW_FIELD', 'ALLOW', 200, TRUE, 4, NULL, 'FIELD', 'AND'),
    (17, 'TODO_ALLOW_DESCRIPTION', 'Permitir descrição', 'Visualizador restrito visualiza descrição', 1, 'VIEW_FIELD', 'ALLOW', 200, TRUE, 4, NULL, 'FIELD', 'AND'),
    (18, 'TODO_MASK_DESCRIPTION_SENSITIVE', 'Mascarar descrição sensível', 'Descrição vem mascarada quando o ToDo é sensível', 1, 'VIEW_FIELD', 'MASK', 850, TRUE, 4, NULL, 'FIELD', 'AND'),
    (19, 'TODO_ALLOW_RG_LIST', 'Permitir RG listado', 'Visualizador restrito pode listar anexo RG', 1, 'LIST_FILE', 'ALLOW', 300, TRUE, 4, 6, 'FILE', 'AND'),
    (20, 'TODO_ALLOW_RG_METADATA', 'Permitir metadata RG', 'Visualizador restrito pode ver metadados de RG', 1, 'VIEW_FILE_METADATA', 'ALLOW', 300, TRUE, 4, 7, 'FILE', 'AND'),
    (21, 'TODO_ALLOW_RG_DOWNLOAD', 'Permitir download RG', 'Visualizador restrito pode baixar RG', 1, 'DOWNLOAD_FILE', 'ALLOW', 300, TRUE, 4, 8, 'FILE', 'AND'),
    (22, 'TODO_DENY_CPF_LIST', 'Negar CPF listado', 'Visualizador restrito não pode listar CPF', 1, 'LIST_FILE', 'DENY', 850, TRUE, 4, NULL, 'FILE', 'AND'),
    (23, 'TODO_DENY_CPF_DOWNLOAD', 'Negar CPF download', 'Visualizador restrito não pode baixar CPF', 1, 'DOWNLOAD_FILE', 'DENY', 900, TRUE, 4, NULL, 'FILE', 'AND'),
    (24, 'TODO_CPF_METADATA_ONLY', 'CPF somente metadata', 'Visualizador restrito vê somente metadados de CPF', 1, 'VIEW_FILE_METADATA', 'METADATA_ONLY', 850, TRUE, 4, 7, 'FILE', 'AND'),
    (25, 'TODO_OPERADOR_OWN_READ', 'Operador lê próprios ToDos', 'Operador lê apenas os próprios ToDos', 1, 'READ', 'ALLOW', 350, TRUE, 3, 2, 'RECORD', 'AND'),
    (26, 'TODO_OPERADOR_OWN_LIST', 'Operador lista próprios ToDos', 'Operador lista apenas os próprios ToDos', 1, 'LIST', 'ALLOW', 350, TRUE, 3, 5, 'RECORD', 'AND'),
    (27, 'TODO_OPERADOR_FILE_METADATA_ONLY_CONTRATO', 'Contrato só metadata', 'Operador visualiza apenas metadado de contrato', 1, 'VIEW_FILE_METADATA', 'METADATA_ONLY', 700, TRUE, 3, 7, 'FILE', 'AND'),
    (28, 'TODO_OPERADOR_DENY_CONTRATO_DOWNLOAD', 'Negar download contrato', 'Operador não baixa contrato', 1, 'DOWNLOAD_FILE', 'DENY', 800, TRUE, 3, NULL, 'FILE', 'AND');

INSERT INTO auth_policy_condition (
    policy_id, condition_order, source_type, attribute_name, operator, comparison_type, expected_value,
    comparison_source_type, comparison_attribute_name
)
VALUES
    (9, 1, 'SUBJECT', 'escolaId', 'EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'escolaId'),
    (10, 1, 'SUBJECT', 'escolaId', 'EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'escolaId'),
    (11, 1, 'SUBJECT', 'escolaId', 'NOT_EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'escolaId'),
    (12, 1, 'SUBJECT', 'escolaId', 'NOT_EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'escolaId'),
    (13, 1, 'SUBJECT', 'tenantId', 'NOT_EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'tenantId'),
    (14, 1, 'SUBJECT', 'tenantId', 'NOT_EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'tenantId'),
    (18, 1, 'RESOURCE', 'sensivel', 'IS_TRUE', 'LITERAL', NULL, NULL, NULL),
    (22, 1, 'RESOURCE', 'fileType', 'EQUALS', 'LITERAL', 'CPF', NULL, NULL),
    (23, 1, 'RESOURCE', 'fileType', 'EQUALS', 'LITERAL', 'CPF', NULL, NULL),
    (24, 1, 'RESOURCE', 'fileType', 'EQUALS', 'LITERAL', 'CPF', NULL, NULL),
    (25, 1, 'SUBJECT', 'pessoaId', 'EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'ownerPessoaId'),
    (26, 1, 'SUBJECT', 'pessoaId', 'EQUALS', 'ATTRIBUTE_REFERENCE', NULL, 'RESOURCE', 'ownerPessoaId'),
    (27, 1, 'RESOURCE', 'fileType', 'EQUALS', 'LITERAL', 'CONTRATO', NULL, NULL),
    (28, 1, 'RESOURCE', 'fileType', 'EQUALS', 'LITERAL', 'CONTRATO', NULL, NULL);

INSERT INTO auth_policy_field_target (policy_id, resource_field_id)
VALUES
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13),
    (15, 4),
    (16, 2),
    (17, 3),
    (18, 3);

INSERT INTO auth_policy_file_target (policy_id, resource_file_type_id)
VALUES
    (19, 2),
    (20, 2),
    (21, 2),
    (22, 1),
    (23, 1),
    (24, 1),
    (27, 3),
    (28, 3);


SELECT setval(pg_get_serial_sequence('pessoa', 'id'), COALESCE((SELECT MAX(id) FROM pessoa), 1), TRUE);
SELECT setval(pg_get_serial_sequence('usuario', 'id'), COALESCE((SELECT MAX(id) FROM usuario), 1), TRUE);
SELECT setval(pg_get_serial_sequence('todo', 'id'), COALESCE((SELECT MAX(id) FROM todo), 1), TRUE);
SELECT setval(pg_get_serial_sequence('todo_attachment', 'id'), COALESCE((SELECT MAX(id) FROM todo_attachment), 1), TRUE);
SELECT setval(pg_get_serial_sequence('auth_policy', 'id'), COALESCE((SELECT MAX(id) FROM auth_policy), 1), TRUE);
