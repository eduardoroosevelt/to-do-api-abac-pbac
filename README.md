# todoauth

Backend de demonstração para autorização genérica combinando **RBAC + ABAC/PBAC** com **Java 17**, **Spring Boot 3.x**, **Spring Security**, **Spring Data JPA**, **Flyway** e **PostgreSQL**.

## 1. Visão geral da solução

A aplicação implementa um motor central de autorização que separa:

- **RBAC** para permissões macro, como `TODO_READ`, `TODO_LIST`, `TODO_ATTACHMENT_DOWNLOAD`;
- **ABAC/PBAC** para decisão fina por recurso, registro, campo e tipo de arquivo;
- **enforcement backend** para que o payload já saia filtrado, mascarado e consistente com as políticas.

O domínio **ToDo** existe apenas como caso funcional de validação. O desenho foi feito para permitir evolução para recursos como `ESCOLA`, `ALUNO`, `CONTRATO`, `PESSOA` e `DEPENDENTE` com o mínimo possível de código novo.

## 2. Por que usar RBAC + ABAC/PBAC neste caso

Somente RBAC não resolve cenários como:

- o usuário pode ler ToDo, mas não ver `observacaoInterna`;
- o usuário pode ver `descricao`, mas mascarada quando `sensivel = true`;
- o usuário pode ver anexo `RG`, mas não `CPF`;
- o usuário pode ver apenas metadados do anexo, sem download;
- o usuário pode ver apenas ToDos da mesma escola, do mesmo tenant ou apenas próprios.

Por isso a solução usa:

- **RBAC** como gate macro;
- **ABAC/PBAC** para políticas configuráveis orientadas por metadados e atributos de `subject`, `resource` e `context`.

## 3. Arquitetura em camadas

```text
interfaces/rest
  -> controllers REST + exception handler

application
  -> use cases, DTOs, gateway de subject atual, orchestration

domain
  -> modelos de autorização, value objects, contratos do motor

infrastructure
  -> JPA, repositories, security, policy engine, resolvers, mapeadores
```

### Responsabilidades

- **controllers**: recebem request e delegam para use cases;
- **application**: coordena fluxo, autorização, persistência e serialização de resposta;
- **domain**: modela `SubjectContext`, `ResourceContext`, `Action`, `PolicyEffect`, operadores e contrato do `PolicyEngine`;
- **infrastructure**: implementa segurança, JPA, resolvers e avaliação de policies.

## 4. Estrutura do projeto

```text
src/main/java/com/example/todoauth
├── application
│   ├── dto
│   ├── exception
│   ├── gateway
│   ├── presenter
│   └── usecase
├── domain
│   ├── model
│   ├── policy
│   ├── service
│   └── valueobject
├── infrastructure
│   ├── authorization
│   ├── config
│   ├── mapper
│   ├── persistence
│   ├── repository
│   ├── security
│   └── storage
└── interfaces
    └── rest
```

## 5. Como subir localmente

### 5.1 Pré-requisitos

- Java 17
- Maven 3.9+
- PostgreSQL 14+

### 5.2 Banco local

Exemplo rápido:

```sql
CREATE DATABASE todoauth;
CREATE USER todoauth WITH PASSWORD 'todoauth';
GRANT ALL PRIVILEGES ON DATABASE todoauth TO todoauth;
```

### 5.3 Configuração

Ajuste `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/todoauth
    username: todoauth
    password: todoauth
```

### 5.4 Subir a aplicação

```bash
mvn spring-boot:run
```

A autenticação de demonstração usa o header `X-User-Id`.
Se o header não for enviado, o filtro usa o usuário `1` por padrão.

## 6. Como configurar banco e Flyway

As migrations ficam em:

```text
src/main/resources/db/migration
```

Arquivos principais:

- `V1__create_authorization_core_tables.sql`
- `V2__create_todo_domain_tables.sql`
- `V3__seed_resources_roles_permissions.sql`
- `V4__seed_policies_and_demo_data.sql`

Para rodar migrations ao subir a aplicação, mantenha:

```yaml
spring:
  flyway:
    enabled: true
```

Para executar somente o ciclo de build:

```bash
mvn clean verify
```

## 7. Como executar testes

### Todos os testes

```bash
mvn test
```

### Apenas compilação

```bash
mvn -q -DskipTests compile
```

## 8. Como funciona o motor de autorização

### 8.1 SubjectContext

Contém:

- `userId`
- `pessoaId`
- `roles`
- `permissions`
- `tenantId`
- `escolaId`
- atributos adicionais

### 8.2 ResourceContext

Contém:

- `resourceType`
- `resourceId`
- atributos dinâmicos do recurso
- `fields`
- `fileTypes`
- sinalização de sensibilidade

### 8.3 Fluxo de decisão

1. o request entra autenticado;
2. `SecurityCurrentSubjectGateway` monta o `SubjectContext`;
3. o caso de uso monta o `ResourceContext`;
4. `AuthorizationService` delega para o `PolicyEngine`;
5. `FieldVisibilityResolver` e `FileAccessResolver` aplicam a decisão;
6. o DTO final sai seguro e consistente.

## 9. Resolução de conflitos

Regras implementadas:

- **recurso/registro**: `DENY > ALLOW`
- **campo**: `DENY > MASK > ALLOW`
- **arquivo**: `DENY > METADATA_ONLY > ALLOW`
- **prioridade**: maior `priority` primeiro
- **fallback**: `default deny`

Exemplo:

- policy `ALLOW READ TODO`
- policy `DENY READ TODO if subject.tenantId != resource.tenantId`

Se o tenant divergir, a policy `DENY` prevalece.

## 10. Como campos são mascarados

O `FieldVisibilityResolver` avalia `VIEW_FIELD` para cada campo do recurso.

Comportamentos possíveis:

- `ALLOW`: o campo é retornado normalmente;
- `DENY`: o campo é removido do payload;
- `MASK`: o valor é transformado pelo `DataMaskingService`.

Estratégias disponíveis no backend:

- `NULLIFY`
- `FIXED_MASK`
- `PARTIAL_TEXT`
- `LAST_N_CHARS`
- `HASH`

Exemplo prático:

- `observacaoInterna` pode ser negado para um visualizador restrito;
- `descricao` pode ser mascarada quando `resource.sensivel = true`.

## 11. Como anexos são filtrados

O `FileAccessResolver` trata três ações separadas:

- `LIST_FILE`
- `VIEW_FILE_METADATA`
- `DOWNLOAD_FILE`

Resultados possíveis:

- `HIDE`: o anexo some da listagem;
- `METADATA_ONLY`: o anexo aparece, mas sem download;
- `DOWNLOAD`: o anexo pode ser baixado.

Isso permite cenários como:

- ver `RG`, mas não `CPF`;
- ver apenas metadados de `CONTRATO`.

## 12. O que é realmente genérico

A parte genérica da solução é o conjunto formado por:

- `auth_resource`
- `auth_resource_field`
- `auth_resource_file_type`
- `auth_policy`
- `auth_policy_condition`
- `auth_policy_field_target`
- `auth_policy_file_target`
- `PolicyEngine`
- `AuthorizationService`
- `FieldVisibilityResolver`
- `FileAccessResolver`

Com isso, novas features podem ser incorporadas principalmente por **cadastro** de:

- `resourceType`
- actions
- fields
- fileTypes
- policies

## 13. O que pode exigir pequeno adaptador técnico

Mesmo sendo metadata-driven, algumas partes podem exigir adaptação pequena e isolada:

- montagem de `ResourceContext` para um agregado novo;
- enriquecimento de `SubjectContext` com atributos vindos de outros sistemas;
- tradução otimizada de filtros para query SQL em listagens grandes;
- integração com storage real de arquivos;
- relacionamento complexo, por exemplo vínculos externos entre pessoas.

## 14. Limitações reais da abordagem

- a autenticação atual é **demonstrativa**, baseada em `X-User-Id`;
- o download de anexo usa um storage em memória/sintético para demo;
- a listagem de ToDos usa filtragem em memória após leitura, não push-down SQL completo;
- o cadastro administrativo ainda é uma API técnica, não uma UI de administração;
- políticas muito sofisticadas podem exigir adaptadores adicionais ou DSL mais rica.

## 15. Endpoints implementados

### ToDo

- `POST /api/todos`
- `GET /api/todos/{id}`
- `GET /api/todos`
- `PUT /api/todos/{id}`
- `DELETE /api/todos/{id}`

### Anexos

- `POST /api/todos/{id}/attachments`
- `GET /api/todos/{id}/attachments`
- `GET /api/todos/{id}/attachments/{attachmentId}/metadata`
- `GET /api/todos/{id}/attachments/{attachmentId}/download`

### Administração

- `POST /api/admin/resources`
- `GET /api/admin/resources`
- `POST /api/admin/resources/{resourceType}/fields`
- `POST /api/admin/resources/{resourceType}/file-types`
- `POST /api/admin/policies`
- `GET /api/admin/policies`
- `POST /api/admin/authorization/evaluate`

## 16. Exemplos de requests e responses

### 16.1 POST /api/todos

#### Request

```http
POST /api/todos
X-User-Id: 1
Content-Type: application/json
```

```json
{
  "titulo": "Conferir documentação",
  "descricao": "Validar documentação entregue pela família.",
  "observacaoInterna": "Não expor para visualizador restrito.",
  "prioridade": "ALTA",
  "status": "ABERTO",
  "dataLimite": "2026-03-30T18:00:00Z",
  "ownerPessoaId": 3,
  "escolaId": 10,
  "sensivel": true
}
```

#### Response

```json
{
  "id": 6,
  "fields": {
    "titulo": "Conferir documentação",
    "descricao": "Va***ia",
    "prioridade": "ALTA",
    "status": "ABERTO",
    "ownerPessoaId": 3,
    "escolaId": 10,
    "tenantId": 1,
    "criadoPorUsuarioId": 1
  },
  "attachments": [],
  "dataCriacao": "2026-03-18T12:00:00Z",
  "dataLimite": "2026-03-30T18:00:00Z"
}
```

### 16.2 GET /api/todos/{id}

#### Request

```http
GET /api/todos/2
X-User-Id: 4
```

#### Response

```json
{
  "id": 2,
  "fields": {
    "titulo": "Conferir contrato",
    "descricao": "Co***to",
    "prioridade": "URGENTE",
    "status": "EM_ANDAMENTO",
    "dataCriacao": "2026-03-16T10:00:00Z",
    "dataLimite": "2026-03-23T10:00:00Z",
    "ownerPessoaId": 4,
    "escolaId": 10,
    "tenantId": 1,
    "criadoPorUsuarioId": 2
  },
  "attachments": [
    {
      "id": 3,
      "nomeArquivo": "contrato-sensivel.pdf",
      "tipoArquivo": "CONTRATO",
      "contentType": "application/pdf",
      "downloadable": false,
      "metadataOnly": true
    }
  ],
  "dataCriacao": "2026-03-16T10:00:00Z",
  "dataLimite": "2026-03-23T10:00:00Z"
}
```

### 16.3 GET /api/todos

#### Request

```http
GET /api/todos
X-User-Id: 2
```

#### Response

```json
[
  {
    "id": 1,
    "fields": {
      "titulo": "Renovar matrícula",
      "descricao": "Validar documentação da matrícula do aluno.",
      "prioridade": "ALTA",
      "status": "ABERTO",
      "escolaId": 10,
      "tenantId": 1
    },
    "attachments": []
  },
  {
    "id": 2,
    "fields": {
      "titulo": "Conferir contrato",
      "descricao": "Co***to",
      "prioridade": "URGENTE",
      "status": "EM_ANDAMENTO",
      "escolaId": 10,
      "tenantId": 1
    },
    "attachments": []
  }
]
```

### 16.4 POST /api/todos/{id}/attachments

#### Request

```http
POST /api/todos/2/attachments
X-User-Id: 1
Content-Type: application/json
```

```json
{
  "nomeArquivo": "rg-responsavel.pdf",
  "tipoArquivo": "RG",
  "contentType": "application/pdf",
  "caminhoStorage": "/storage/todos/2/rg-responsavel.pdf",
  "sensivel": false
}
```

#### Response

```json
{
  "id": 10,
  "nomeArquivo": "rg-responsavel.pdf",
  "tipoArquivo": "RG",
  "contentType": "application/pdf",
  "caminhoStorage": "/storage/todos/2/rg-responsavel.pdf",
  "downloadable": true,
  "metadataOnly": false
}
```

### 16.5 GET /api/todos/{id}/attachments

#### Request

```http
GET /api/todos/1/attachments
X-User-Id: 4
```

#### Response

```json
[
  {
    "id": 1,
    "nomeArquivo": "rg-aluno.pdf",
    "tipoArquivo": "RG",
    "contentType": "application/pdf",
    "caminhoStorage": "/storage/todos/1/rg-aluno.pdf",
    "downloadable": true,
    "metadataOnly": false
  }
]
```

### 16.6 GET /api/todos/{id}/attachments/{attachmentId}/metadata

#### Request

```http
GET /api/todos/2/attachments/3/metadata
X-User-Id: 3
```

#### Response

```json
{
  "id": 3,
  "nomeArquivo": "contrato-sensivel.pdf",
  "tipoArquivo": "CONTRATO",
  "contentType": "application/pdf",
  "downloadable": false,
  "metadataOnly": true
}
```

### 16.7 GET /api/todos/{id}/attachments/{attachmentId}/download

#### Request

```http
GET /api/todos/1/attachments/1/download
X-User-Id: 4
```

#### Response

```text
DEMO_ATTACHMENT_CONTENT::/storage/todos/1/rg-aluno.pdf
```

### 16.8 POST /api/admin/resources

#### Request

```http
POST /api/admin/resources
X-User-Id: 1
Content-Type: application/json
```

```json
{
  "resourceType": "ESCOLA",
  "resourceName": "Escola",
  "description": "Recurso administrativo de escola",
  "tableName": "escola",
  "idFieldName": "id",
  "tenantFieldName": "tenant_id",
  "escolaFieldName": "id",
  "ownerFieldName": null,
  "createdByFieldName": "criado_por_usuario_id",
  "sensitivityFieldName": "sensivel",
  "supportsAttachments": false
}
```

#### Response

```json
{
  "id": 2,
  "resourceType": "ESCOLA",
  "resourceName": "Escola",
  "description": "Recurso administrativo de escola",
  "tableName": "escola",
  "supportsAttachments": false,
  "active": true
}
```

### 16.9 POST /api/admin/resources/{resourceType}/fields

#### Request

```http
POST /api/admin/resources/ESCOLA/fields
X-User-Id: 1
Content-Type: application/json
```

```json
{
  "fieldName": "nome",
  "displayName": "Nome da Escola",
  "dataType": "STRING",
  "sensitive": false,
  "maskStrategy": null,
  "defaultVisibility": "VISIBLE",
  "queryable": true,
  "sortable": true
}
```

#### Response

```json
{
  "id": 2,
  "resourceType": "ESCOLA",
  "resourceName": "Escola",
  "description": "Recurso administrativo de escola",
  "tableName": "escola",
  "supportsAttachments": false,
  "active": true
}
```

### 16.10 POST /api/admin/resources/{resourceType}/file-types

#### Request

```http
POST /api/admin/resources/ESCOLA/file-types
X-User-Id: 1
Content-Type: application/json
```

```json
{
  "fileTypeCode": "COMPROVANTE",
  "displayName": "Comprovante",
  "sensitive": false,
  "defaultAccessLevel": "DENY"
}
```

#### Response

```json
{
  "id": 2,
  "resourceType": "ESCOLA",
  "resourceName": "Escola",
  "description": "Recurso administrativo de escola",
  "tableName": "escola",
  "supportsAttachments": false,
  "active": true
}
```

### 16.11 POST /api/admin/policies

#### Request

```http
POST /api/admin/policies
X-User-Id: 1
Content-Type: application/json
```

```json
{
  "policyCode": "ESCOLA_READ_SAME_TENANT",
  "policyName": "Leitura de escola no mesmo tenant",
  "description": "Permite leitura de escolas do mesmo tenant",
  "resourceType": "ESCOLA",
  "actionCode": "READ",
  "effect": "ALLOW",
  "priority": 300,
  "active": true,
  "appliesToRoleId": 2,
  "scopeLevel": "RECORD",
  "conditionLogic": "AND",
  "fieldNames": [],
  "fileTypeCodes": [],
  "conditions": [
    {
      "conditionOrder": 1,
      "sourceType": "SUBJECT",
      "attributeName": "tenantId",
      "operator": "EQUALS",
      "comparisonType": "ATTRIBUTE_REFERENCE",
      "comparisonSourceType": "RESOURCE",
      "comparisonAttributeName": "tenantId"
    }
  ]
}
```

#### Response

```json
{
  "id": 30,
  "policyCode": "ESCOLA_READ_SAME_TENANT",
  "policyName": "Leitura de escola no mesmo tenant",
  "resourceType": "ESCOLA",
  "actionCode": "READ",
  "effect": "ALLOW",
  "priority": 300,
  "active": true,
  "fieldTargets": [],
  "fileTargets": [],
  "conditions": [
    "SUBJECT.tenantId EQUALS RESOURCE.tenantId"
  ]
}
```

### 16.12 POST /api/admin/authorization/evaluate

#### Request

```http
POST /api/admin/authorization/evaluate
X-User-Id: 1
Content-Type: application/json
```

```json
{
  "userId": 4,
  "resourceType": "TODO",
  "resourceId": 4,
  "action": "READ"
}
```

#### Response

```json
{
  "allowed": false,
  "effect": "DENY",
  "reason": "Denied by policy DENY_OTHER_TENANT_READ",
  "matchedPolicies": [
    "DENY_OTHER_TENANT_READ"
  ]
}
```

## 17. Estratégia de testes implementada

A suíte de testes cobre:

### Unitários

- `DefaultPolicyEngineTest`
  - role macro correta, mas sem policy fina suficiente;
  - conflito `ALLOW` vs `DENY`;
  - prioridade de policy;
  - máscara de campo sensível;
  - campo oculto por metadata;
  - RG permitido e CPF negado.

- `FieldVisibilityResolverTest`
  - ocultação de `observacaoInterna`;
  - mascaramento de `descricao`.

- `FileAccessResolverTest`
  - RG visível;
  - CPF escondido;
  - `CONTRATO` com `METADATA_ONLY`.

### Integração

- GET de ToDo com ocultação de campo e máscara;
- listagem de anexos com RG visível e CPF oculto;
- metadata only para contrato;
- listagem de ToDos filtrada por escola;
- listagem de ToDos filtrada por owner;
- download bloqueado por política;
- endpoint `/api/admin/authorization/evaluate` explicando negação por tenant.

## 18. Como adicionar um novo recurso chamado ESCOLA com o mínimo de código

### 18.1 O que é só cadastro/configuração

A maior parte da evolução é cadastro nas tabelas de autorização:

1. inserir `auth_resource` com `resourceType = ESCOLA`;
2. cadastrar actions em `auth_resource_action`;
3. cadastrar fields em `auth_resource_field`;
4. cadastrar tipos de arquivo em `auth_resource_file_type`, se houver;
5. cadastrar policies em `auth_policy`;
6. cadastrar condições em `auth_policy_condition`;
7. cadastrar alvos de campo em `auth_policy_field_target`;
8. cadastrar alvos de arquivo em `auth_policy_file_target`.

### 18.2 O que precisa de adaptador, se houver

Em geral, só será necessário um pequeno adaptador se:

- a montagem do `ResourceContext` de `ESCOLA` não puder ser inferida facilmente;
- houver anexos com regras especiais;
- existir dependência de atributos externos ao banco local.

### 18.3 Como cadastrar actions

Exemplos:

- `CREATE`
- `READ`
- `UPDATE`
- `DELETE`
- `LIST`
- `VIEW_FIELD`
- `LIST_FILE`
- `VIEW_FILE_METADATA`
- `DOWNLOAD_FILE`

### 18.4 Como cadastrar fields

Cadastre cada campo com:

- `fieldName`
- `displayName`
- `dataType`
- `sensitive`
- `maskStrategy`
- `defaultVisibility`
- `queryable`
- `sortable`

### 18.5 Como cadastrar fileTypes

Se `ESCOLA` tiver anexos, cadastre por exemplo:

- `ALVARA`
- `CONTRATO`
- `COMPROVANTE`

Cada um com:

- `fileTypeCode`
- `displayName`
- `sensitive`
- `defaultAccessLevel`

### 18.6 Como cadastrar policies

Crie policies por perfil e ação, por exemplo:

- permitir leitura da mesma unidade;
- negar leitura fora do tenant;
- mascarar um campo sensível;
- permitir apenas metadado de um anexo específico.

### 18.7 Como validar pelo endpoint de evaluation

Use:

```http
POST /api/admin/authorization/evaluate
```

Com payload semelhante a:

```json
{
  "userId": 2,
  "resourceType": "ESCOLA",
  "resourceId": 15,
  "action": "READ"
}
```

### 18.8 Como testar

1. crie testes unitários para policies específicas de `ESCOLA`;
2. crie testes de integração para endpoints do novo recurso;
3. valide cenários positivos e negativos;
4. valide conflito `ALLOW` vs `DENY` e prioridade;
5. valide masking e file access quando aplicável.

## 19. Conclusão

A solução entrega um backend em que:

- a **autorização é responsabilidade do backend**;
- controllers permanecem finos;
- o motor central é reutilizável;
- o domínio ToDo prova acesso por tenant, escola, owner, campo e arquivo;
- a evolução futura tende a ocorrer mais por **cadastro de metadados e policies** do que por reescrita do motor.
