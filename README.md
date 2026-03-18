# to-do-api-abac-pbac

## Visão arquitetural

Este repositório documenta uma proposta de backend genérico de autorização combinando **RBAC + ABAC/PBAC** para aplicações Java 17 com Spring Boot 3.x, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Maven e Clean Architecture.

O objetivo da solução é separar claramente:

- **autenticação**;
- **permissões macro** via RBAC;
- **decisão fina de acesso** via políticas declarativas;
- **filtragem pós-autorização** de campos, anexos e binários.

A entidade **ToDo** é apenas o domínio de validação. O motor deve ser reutilizável para futuros recursos como `ESCOLA`, `ALUNO`, `CONTRATO`, `PESSOA` e `DEPENDENTE` com o mínimo possível de código novo.

### Princípios da solução

- **Policy engine genérico**: o motor avalia `SubjectContext`, `ResourceContext`, `Action` e políticas configuradas.
- **Metadata-driven**: recursos, campos, tipos de arquivo, ações, sensibilidade e relacionamentos são cadastráveis.
- **RBAC como porta de entrada**: permissões macro limitam o universo do que o usuário pode tentar fazer.
- **ABAC/PBAC como refinamento**: políticas avaliam atributos do sujeito, recurso e contexto.
- **Autorização em múltiplas camadas**:
  - nível de recurso;
  - nível de registro/instância;
  - nível de campo;
  - nível de anexo/tipo de arquivo;
  - nível de download do binário.
- **Pós-processamento seguro da resposta**: campos podem ser omitidos, nulificados ou mascarados; anexos podem ser ocultados ou expostos apenas como metadados.
- **Determinismo**: conflitos entre políticas são resolvidos por precedência estável e auditável.
- **Adaptadores mínimos por feature**: no máximo registro de metadados e pequenos resolvers técnicos quando houver necessidade de enriquecer contexto.

---

## 1. Visão arquitetural completa

A solução é composta por dois eixos complementares.

### Eixo 1: Autorização macro (RBAC)

Responsável por responder perguntas como:

- o usuário pode `READ` ToDo?
- o usuário pode `LIST` ToDo?
- o usuário pode `DOWNLOAD_FILE` de anexos de ToDo?

Esse eixo pode usar:

- roles do usuário;
- permissões agregadas por role;
- permissões diretas do usuário, se desejado.

O RBAC não decide sozinho **quais instâncias**, **quais campos** e **quais anexos** serão acessíveis. Ele apenas valida a capacidade macro.

### Eixo 2: Autorização fina (ABAC/PBAC)

Responsável por responder perguntas como:

- esse ToDo específico pode ser lido por este usuário?
- o campo `observacaoInterna` deve ser omitido?
- o campo `descricao` deve ser mascarado porque o item é sensível?
- o anexo do tipo `CPF` pode aparecer?
- o arquivo pode aparecer apenas como metadado, sem download?

Esse eixo utiliza:

- atributos do sujeito (`tenantId`, `escolaId`, `pessoaId`, vínculos, papéis, atributos dinâmicos);
- atributos do recurso (`ownerPessoaId`, `tenantId`, `escolaId`, `sensivel`, tipo do recurso, classificação);
- atributos contextuais (`requestOrigin`, horário, canal, escopo funcional, parâmetros de listagem).

### Estratégia de alto nível

1. O request chega autenticado.
2. Um `SubjectContextFactory` materializa o contexto do usuário.
3. O caso de uso executa a operação desejada.
4. Antes de retornar dados, um `AuthorizationFacade` chama o motor.
5. O motor avalia políticas do recurso/ação.
6. O resultado da decisão alimenta:
   - filtro de instâncias;
   - filtro/máscara de campos;
   - filtro de anexos;
   - bloqueio ou degradação de download para `METADATA_ONLY`.
7. A resposta final já sai segura e compatível com as políticas vigentes.

### Separação entre decisão e execução

A arquitetura separa:

- **Decision layer**: decide `ALLOW`, `DENY`, `MASK`, `METADATA_ONLY`;
- **Enforcement layer**: aplica a decisão em DTOs, queries e serviços de arquivo.

Isso evita espalhar `if` de segurança pelo domínio de negócio.

---

## 2. Diagrama textual dos módulos/camadas

```text
[ Interface Adapters / Entry Points ]
  ├── REST Controllers
  ├── Exception Handlers
  ├── Request -> UseCase Mappers
  └── Security Filter Chain / JWT Authentication
                |
                v
[ Application Layer ]
  ├── Use Cases
  │    ├── GetTodoByIdUseCase
  │    ├── ListTodosUseCase
  │    ├── DownloadTodoAttachmentUseCase
  │    └── Generic Authorization-aware Query Use Cases
  ├── AuthorizationFacade
  ├── SubjectContextFactory
  ├── ResourceContextAssembler
  ├── FieldVisibilityResolver
  ├── FileAccessResolver
  ├── DataMaskingService
  └── PolicyEvaluationOrchestrator
                |
                v
[ Domain Layer ]
  ├── Entities / Aggregates
  │    ├── ToDo
  │    ├── Attachment
  │    ├── AuthorizationPolicy
  │    ├── PolicyCondition
  │    ├── ResourceMetadata
  │    ├── ResourceFieldMetadata
  │    ├── ResourceFileTypeMetadata
  │    └── SubjectContext / ResourceContext / AuthorizationDecision
  ├── Value Objects
  │    ├── Action
  │    ├── PolicyEffect
  │    ├── AttributeSource
  │    ├── Operator
  │    └── SensitivityLevel
  ├── Domain Services
  │    ├── PolicyResolver
  │    ├── ConditionEvaluator
  │    ├── PolicyConflictResolver
  │    └── AuthorizationDomainService
  └── Repository Ports
       ├── PolicyRepository
       ├── ResourceMetadataRepository
       ├── SubjectAttributeRepository
       ├── TodoRepository
       └── AttachmentRepository
                |
                v
[ Infrastructure Layer ]
  ├── JPA Repositories / Specifications / QueryDSL Criteria
  ├── Flyway migrations
  ├── PostgreSQL schema
  ├── Spring Security adapters
  ├── Policy cache
  ├── Storage adapters (S3, MinIO, filesystem)
  ├── Dynamic relationship resolvers
  └── Audit / observability / tracing
```

---

## 3. Estrutura de pacotes sugerida

```text
com.example.todoauth
├── domain
│   ├── authorization
│   │   ├── model
│   │   │   ├── SubjectContext.java
│   │   │   ├── ResourceContext.java
│   │   │   ├── AuthorizationDecision.java
│   │   │   ├── AuthorizationPolicy.java
│   │   │   ├── PolicyCondition.java
│   │   │   ├── ResourceMetadata.java
│   │   │   ├── ResourceFieldMetadata.java
│   │   │   ├── ResourceFileTypeMetadata.java
│   │   │   ├── RelationshipMetadata.java
│   │   │   └── enums...
│   │   ├── service
│   │   │   ├── AuthorizationDomainService.java
│   │   │   ├── PolicyResolver.java
│   │   │   ├── ConditionEvaluator.java
│   │   │   └── PolicyConflictResolver.java
│   │   └── port
│   │       ├── PolicyRepository.java
│   │       ├── ResourceMetadataRepository.java
│   │       ├── RelationshipResolverPort.java
│   │       └── AuthorizationAuditPort.java
│   └── todo
│       ├── model
│       │   ├── Todo.java
│       │   └── TodoAttachment.java
│       └── port
│           ├── TodoRepository.java
│           └── TodoAttachmentRepository.java
├── application
│   ├── authorization
│   │   ├── facade
│   │   │   └── AuthorizationFacade.java
│   │   ├── context
│   │   │   ├── SubjectContextFactory.java
│   │   │   ├── ResourceContextAssembler.java
│   │   │   └── GenericResourceContextAssembler.java
│   │   ├── resolver
│   │   │   ├── FieldVisibilityResolver.java
│   │   │   ├── FileAccessResolver.java
│   │   │   └── AuthorizationQueryFilterService.java
│   │   ├── masking
│   │   │   ├── DataMaskingService.java
│   │   │   └── strategies...
│   │   └── dto
│   │       ├── AuthorizationRequest.java
│   │       ├── AuthorizationResult.java
│   │       └── FieldAccessDecision.java
│   └── todo
│       ├── usecase
│       │   ├── GetTodoByIdUseCase.java
│       │   ├── ListTodosUseCase.java
│       │   └── DownloadTodoAttachmentUseCase.java
│       └── dto
│           ├── TodoResponse.java
│           └── TodoAttachmentResponse.java
├── adapter
│   ├── in
│   │   ├── web
│   │   │   ├── TodoController.java
│   │   │   └── AttachmentController.java
│   │   └── security
│   │       └── JwtAuthenticationAdapter.java
│   └── out
│       ├── persistence
│       │   ├── jpa
│       │   ├── entity
│       │   └── mapper
│       ├── storage
│       ├── cache
│       └── audit
└── config
    ├── SecurityConfig.java
    ├── AuthorizationModuleConfig.java
    └── JacksonConfig.java
```

---

## 4. Responsabilidades de cada camada segundo Clean Architecture

### 4.1 Domain

Camada central e independente de framework.

**Responsabilidades:**

- modelar conceitos de autorização;
- definir entidades, value objects e invariantes;
- definir contratos (ports) para obtenção de políticas, metadados e relacionamentos;
- implementar a lógica pura de resolução de políticas e conflitos.

**Não deve depender de:**

- Spring;
- JPA;
- HTTP;
- PostgreSQL;
- detalhes de serialização.

### 4.2 Application

Orquestra casos de uso e integra o domínio com adaptadores.

**Responsabilidades:**

- montar `SubjectContext` e `ResourceContext`;
- chamar o motor de autorização;
- aplicar resolvers de campo e arquivo;
- coordenar leitura de dados e composição de resposta;
- decidir em que momentos a autorização será aplicada.

**Não deve conter:**

- regras de persistência acopladas ao banco;
- detalhes de transporte HTTP;
- lógica de política hardcoded em controller.

### 4.3 Interface Adapters

Traduz o mundo externo para os casos de uso.

**Responsabilidades:**

- controllers REST;
- mapeamento request/response;
- adaptação do principal autenticado para o `SubjectContextFactory`;
- serialização final do DTO já filtrado.

### 4.4 Infrastructure

Implementa detalhes técnicos.

**Responsabilidades:**

- JPA repositories;
- queries e specifications;
- Flyway;
- storage de arquivos;
- cache de políticas/metadados;
- integrações externas para vínculos e atributos dinâmicos;
- auditoria e observabilidade.

---

## 5. Modelo conceitual das entidades de autorização

## 5.1 SubjectContext

Representa o sujeito autenticado com dados estruturados e extensíveis.

**Atributos mínimos:**

- `userId`
- `pessoaId`
- `roles`
- `permissions`
- `tenantId`
- `escolaId`
- `orgaoId`
- `attributes: Map<String, Object>`

**Observações de desenho:**

- `roles` suportam RBAC macro;
- `permissions` permitem granularidade maior sem inflar roles;
- `attributes` suportam expansão futura sem alterar o motor;
- atributos derivados como `linkedPessoaIds`, `regionalIds`, `scopes` podem ser inseridos dinamicamente.

## 5.2 ResourceContext

Representa qualquer recurso autorizável de maneira genérica.

**Atributos mínimos:**

- `resourceType`
- `resourceId`
- `attributes: Map<String, Object>`
- `fields: Map<String, Object>`
- `fileTypes: Set<String>`
- `sensitivityLevel` ou `sensivel`

**No caso do ToDo:**

- `resourceType = TODO`
- `attributes` incluirão `tenantId`, `escolaId`, `ownerPessoaId`, `criadoPorUsuarioId`, `status`, `prioridade`, `sensivel`
- `fields` conterão valores brutos do recurso para posterior filtragem
- `fileTypes` refletirão anexos existentes e/ou tipos suportados

### Separação importante

- `attributes` = dados para decisão de policy;
- `fields` = dados passíveis de exposição na API.

Nem todo atributo precisa ser serializável.

## 5.3 Action

As ações devem ser genéricas e registráveis por recurso.

**Conjunto base sugerido:**

- `CREATE`
- `READ`
- `UPDATE`
- `DELETE`
- `LIST`
- `VIEW_FIELD`
- `LIST_FILE`
- `VIEW_FILE_METADATA`
- `DOWNLOAD_FILE`

**Observação:**

`VIEW_FIELD` e `DOWNLOAD_FILE` não substituem `READ`; eles refinam o acesso.

## 5.4 Policy

Representa a regra declarativa cadastrada.

**Campos conceituais:**

- `id`
- `name`
- `description`
- `effect: ALLOW | DENY | MASK | METADATA_ONLY`
- `resourceType`
- `action`
- `priority`
- `active`
- `targetField` (opcional)
- `targetFileType` (opcional)
- `appliesToRole` / `appliesToPermission` (opcionais)
- `conditions`
- `validFrom`, `validUntil` (opcional)

**Interpretação:**

- `ALLOW`/`DENY`: recurso, instância, campo ou arquivo;
- `MASK`: normalmente aplicável a campo;
- `METADATA_ONLY`: normalmente aplicável a arquivo/anexo.

## 5.5 PolicyCondition

Representa cada cláusula simples de avaliação.

**Campos mínimos:**

- `attributeSource: SUBJECT | RESOURCE | CONTEXT`
- `attributeName`
- `operator`
- `expectedValue`

**Operadores mínimos suportados:**

- `EQUALS`
- `NOT_EQUALS`
- `IN`
- `NOT_IN`
- `GREATER_THAN`
- `LESS_THAN`
- `IS_TRUE`
- `IS_FALSE`
- `CONTAINS`

### Observação importante sobre comparações entre subject e resource

Para suportar regras como:

- `subject.escolaId == resource.escolaId`
- `subject.tenantId == resource.tenantId`
- `subject.pessoaId == resource.ownerPessoaId`

é recomendável permitir duas formas de condição:

1. **comparação atributo vs literal**
2. **comparação atributo vs atributo**

Em uma modelagem mínima, isso pode aparecer como:

- `attributeSource`
- `attributeName`
- `operator`
- `expectedValue`
- `comparisonSource` (opcional)
- `comparisonAttributeName` (opcional)

Sem isso, ABAC real fica limitado. Portanto, a recomendação arquitetural é evoluir `expectedValue` para um `ComparisonValue` com:

- `valueType: LITERAL | ATTRIBUTE_REFERENCE`
- `literalValue`
- `referenceSource`
- `referenceAttributeName`

---

## 5.6 Resource metadata

Essas entidades tornam o motor reutilizável.

### ResourceMetadata

Cadastro do recurso genérico.

**Exemplos de campos:**

- `resourceType` (`TODO`, `ALUNO`, `CONTRATO`)
- `displayName`
- `tableName`
- `idField`
- `tenantField`
- `ownerField`
- `sensitivityField`
- `supportsAttachments`
- `active`

### ResourceActionMetadata

- `resourceType`
- `action`
- `enabled`

### ResourceFieldMetadata

- `resourceType`
- `fieldName`
- `dataType`
- `sensitive`
- `maskStrategy`
- `defaultVisibility`
- `queryable`
- `sortable`

### ResourceFileTypeMetadata

- `resourceType`
- `fileType`
- `sensitive`
- `defaultAccessLevel`

### RelationshipMetadata

Permite descrever relações autorizáveis sem hardcode excessivo.

**Exemplos:**

- `PESSOA_VINCULADA`
- `USUARIO_ESCOLA`
- `GESTOR_ESCOLA`
- `USUARIO_TENANT`

**Campos sugeridos:**

- `relationshipType`
- `subjectAttributeName`
- `resourceAttributeName`
- `resolutionStrategy`
- `active`

---

## 6. Estratégia de resolução de políticas

A resolução deve ser **determinística, simples de auditar e estável**.

## 6.1 Pipeline de decisão

1. Selecionar políticas ativas por `resourceType` e `action`.
2. Filtrar por escopo aplicável:
   - tenant da política, se houver;
   - papel/perfil alvo, se houver;
   - alvo de campo ou tipo de arquivo, quando relevante.
3. Ordenar por:
   - maior `priority` primeiro;
   - em empate, ordem de efeito mais restritiva;
   - em novo empate, `id` crescente para estabilidade.
4. Avaliar condições.
5. Produzir decisão consolidada.

## 6.2 Ordem de precedência recomendada

### Para recurso/instância

`DENY > ALLOW`

### Para campo

`DENY > MASK > ALLOW`

Observação: `DENY` para campo pode significar “campo ausente/nulo”; `MASK` pode significar “visível mascarado”.

### Para anexo

`DENY > METADATA_ONLY > ALLOW`

## 6.3 Estratégia consolidada por nível

### Nível recurso

- se alguma política aplicável resultar em `DENY`, negar acesso ao recurso;
- senão, se houver ao menos uma `ALLOW`, permitir;
- senão, negar por padrão (**default deny**).

### Nível campo

Para cada campo solicitado/serializado:

- se houver `DENY` aplicável ao campo, omitir/nulificar;
- senão, se houver `MASK`, retornar mascarado;
- senão, se houver `ALLOW`, retornar valor normal;
- senão, usar política default do metadado do campo:
  - preferencialmente `hidden unless allowed` para campos sensíveis;
  - opcionalmente `visible unless denied` para campos públicos.

### Nível anexo

Para cada anexo/tipo de arquivo:

- `DENY`: não listar ou retornar inacessível;
- `METADATA_ONLY`: listar metadados, bloquear conteúdo;
- `ALLOW`: listar e permitir binário, se demais regras passarem.

## 6.4 Exemplo de resolução

### Exemplo A: ToDo da mesma escola

- Policy 100: `ALLOW READ TODO if subject.permissions CONTAINS TODO_READ`
- Policy 200: `DENY READ TODO if subject.tenantId != resource.tenantId`
- Policy 300: `ALLOW READ TODO if subject.escolaId == resource.escolaId`

Se o tenant divergir, a policy 200 vence.
Se tenant coincide e escola coincide, leitura permitida.
Se tenant coincide mas escola diverge e não houver outra policy de permissão, negar por padrão.

### Exemplo B: Campo descrição sensível mascarado

- Policy 400: `ALLOW VIEW_FIELD TODO.descricao`
- Policy 500: `MASK VIEW_FIELD TODO.descricao if resource.sensivel IS_TRUE`

Se `sensivel = true`, retorna valor mascarado.
Se `sensivel = false`, retorna normalmente.

### Exemplo C: Arquivo CPF visível só como metadado

- Policy 600: `METADATA_ONLY VIEW_FILE_METADATA TODO fileType=CPF`
- Policy 610: `DENY DOWNLOAD_FILE TODO fileType=CPF`
- Policy 620: `ALLOW LIST_FILE TODO fileType=CPF`

Resultado:

- aparece na listagem de anexos;
- mostra nome/tipo/contentType se permitido;
- download negado.

---

## 7. Estratégia para filtrar campos e anexos

## 7.1 FieldVisibilityResolver

Componente da aplicação que recebe:

- `SubjectContext`
- `ResourceContext`
- DTO ou mapa serializável
- conjunto de campos candidatos

E devolve uma estrutura como:

- `visibleFields`
- `maskedFields`
- `hiddenFields`
- `maskingStrategyByField`

### Processo recomendado

1. Identificar os campos do recurso via `ResourceFieldMetadata`.
2. Para cada campo, avaliar `Action.VIEW_FIELD`.
3. Resolver decisão final do campo.
4. Aplicar transformação:
   - **hidden**: remover do DTO ou setar `null` segundo contrato da API;
   - **masked**: aplicar estratégia de máscara;
   - **allow**: retornar integralmente.

### Estratégias de máscara sugeridas

- `NULLIFY`
- `FIXED_MASK` → `***`
- `PARTIAL_TEXT`
- `HASH`
- `LAST_N_CHARS`
- `CUSTOM_BEAN`

### Exemplo prático

Para `observacaoInterna`:

- metadado marca como sensível;
- ausência de ALLOW explícito já pode ocultar;
- ou uma policy específica de `DENY`/`MASK` controla o comportamento.

---

## 7.2 FileAccessResolver

Componente que trata anexos em três dimensões separadas:

1. **listar existência do anexo**
2. **ver metadados do anexo**
3. **baixar binário do anexo**

### Ações recomendadas por etapa

- `LIST_FILE`
- `VIEW_FILE_METADATA`
- `DOWNLOAD_FILE`

### Processo

Para cada anexo:

1. montar `ResourceContext` derivado ou subcontexto do arquivo;
2. avaliar por `fileType`, `sensivel`, `tenantId` e vínculo com o ToDo;
3. produzir decisão consolidada:
   - `HIDDEN`
   - `METADATA_ONLY`
   - `FULL_ACCESS`

### Regra importante

A permissão para ver o ToDo **não implica** permissão para ver todo anexo. O anexo é um alvo subordinado com políticas próprias.

---

## 7.3 DataMaskingService

Serviço dedicado para evitar que lógica de máscara contamine controllers ou casos de uso.

**Responsabilidades:**

- aplicar estratégias por campo;
- aplicar máscaras contextuais (ex.: se `sensivel=true`);
- suportar evolução para compliance/LGPD.

---

## 8. Fluxo de requisição

## 8.1 Fluxo: `GET /todos/{id}`

### Objetivo

Retornar um ToDo específico com campos e anexos filtrados.

### Passo a passo

1. Controller recebe `GET /todos/{id}`.
2. Caso de uso `GetTodoByIdUseCase` é acionado.
3. `SubjectContextFactory` monta o contexto do usuário autenticado.
4. Repositório busca o ToDo por id e tenant básico, idealmente já restringindo o universo mínimo.
5. `ResourceContextAssembler` monta `ResourceContext` do ToDo.
6. `AuthorizationFacade` avalia `READ` do recurso.
7. Se negar, retornar `403` ou `404` conforme estratégia de ocultação.
8. Se permitir, montar DTO inicial.
9. `FieldVisibilityResolver` avalia cada campo com `VIEW_FIELD`.
10. `DataMaskingService` aplica máscara onde necessário.
11. `FileAccessResolver` avalia anexos:
    - quais listar;
    - quais só metadados;
    - quais ocultar.
12. Retornar payload final seguro.

### Resultado possível

- `titulo`: visível
- `descricao`: mascarada
- `observacaoInterna`: nula/ausente
- anexos RG: visíveis
- anexos CPF: ausentes ou apenas metadados

---

## 8.2 Fluxo: `GET /todos`

### Desafio principal

Listagem exige eficiência. Não é aceitável carregar todos os registros e filtrar em memória em cenários grandes.

### Estratégia recomendada em duas fases

#### Fase A: pré-filtro em query

Aplicar o máximo possível das regras convertíveis para SQL/JPA Specification:

- tenant igual;
- escola igual;
- owner igual ao `pessoaId`;
- owner em lista de pessoas vinculadas;
- status, sensibilidade e outros atributos simples.

Isso é feito por um `AuthorizationQueryFilterService` que traduz políticas elegíveis para critérios de consulta.

#### Fase B: pós-filtro fino

Depois da consulta paginada:

- revalidar item a item quando houver políticas complexas;
- filtrar campos por registro;
- filtrar anexos apenas se a listagem os incluir.

### Passo a passo

1. Controller recebe filtros e paginação.
2. Use case monta `SubjectContext`.
3. `AuthorizationFacade` valida ação macro `LIST` para `TODO`.
4. `AuthorizationQueryFilterService` gera restrições estruturais a partir de políticas traduzíveis.
5. Repositório executa query paginada.
6. Para cada item retornado:
   - montar `ResourceContext` resumido;
   - validar `READ/LIST` por instância se necessário;
   - aplicar `FieldVisibilityResolver` para colunas da listagem.
7. Retornar página filtrada.

### Observação de arquitetura

Nem toda policy ABAC é traduzível para SQL genérico. Por isso, o motor deve distinguir:

- **políticas push-down**: podem virar cláusulas SQL;
- **políticas post-filter**: exigem avaliação em memória.

---

## 8.3 Fluxo: download de anexo

### Exemplo

`GET /todos/{todoId}/attachments/{attachmentId}/download`

### Passo a passo

1. Controller recebe request.
2. Use case carrega metadata do anexo e valida vínculo com o ToDo.
3. `SubjectContextFactory` monta sujeito.
4. `ResourceContextAssembler` monta contexto do arquivo/anexo.
5. `AuthorizationFacade` avalia:
   - `READ` do ToDo pai, se aplicável;
   - `LIST_FILE`/`VIEW_FILE_METADATA` do anexo;
   - `DOWNLOAD_FILE` do anexo.
6. Se a decisão final for:
   - `DENY`: retornar `403/404`;
   - `METADATA_ONLY`: negar download e opcionalmente orientar consumo do endpoint de metadata;
   - `ALLOW`: `StoragePort` entrega stream/binário.

### Regra de segurança

Sempre avaliar acesso ao binário separadamente do acesso ao metadado. Isso evita vazamento indireto.

---

## 9. Estratégia para suportar novas features com mínimo de código

O objetivo é que adicionar um novo recurso siga majoritariamente um roteiro configurável.

## 9.1 O que cadastrar para uma nova entidade

Exemplo: `CONTRATO`.

1. Registrar `ResourceMetadata` com:
   - `resourceType=CONTRATO`
   - nome da tabela
   - campo id
   - campo tenant
   - campo owner
   - campo de sensibilidade
2. Registrar ações habilitadas.
3. Registrar campos e flags de sensibilidade.
4. Registrar tipos de arquivo permitidos, se houver anexos.
5. Registrar relacionamentos relevantes.
6. Cadastrar políticas por perfil/cenário.

## 9.2 O que pode exigir pequeno código novo

- entidade de domínio e repositório do recurso;
- caso de uso da feature;
- mapper para `ResourceContext` se os nomes dos atributos não puderem ser inferidos automaticamente;
- eventual `RelationshipResolver` especializado se a relação depender de join ou serviço externo.

## 9.3 Padrão recomendado para reduzir código

### Registry de mapeamento do recurso

Criar um `ResourceTypeRegistry` configurável que informe, para cada recurso:

- como extrair atributos base do agregado;
- como localizar anexos;
- quais assemblers usar;
- quais resolvers opcionais se aplicam.

### Assemblers baseados em metadado

Um `GenericResourceContextAssembler` pode usar reflection controlada, MapStruct ou descriptor explícito para extrair:

- campos simples;
- atributos de tenancy;
- owner;
- flags de sensibilidade.

Assim, só recursos fora do padrão exigem adaptador específico.

---

## 10. Quais partes são 100% configuráveis e quais podem exigir pequeno adaptador técnico

## 10.1 Partes 100% configuráveis

Essas devem residir em banco/metadados administrativos.

- cadastro de `resourceType`;
- cadastro de ações por recurso;
- cadastro de campos por recurso;
- indicação de campos sensíveis;
- estratégia padrão de máscara por campo;
- cadastro de tipos de arquivo por recurso;
- políticas (`ALLOW`, `DENY`, `MASK`, `METADATA_ONLY`);
- condições de política;
- prioridade e vigência das políticas;
- relação entre roles/permissões macro;
- relacionamento lógico parametrizado quando resolvível por atributos/joins conhecidos.

## 10.2 Partes que podem exigir pequeno adaptador técnico

### 1. Enriquecimento do SubjectContext

Quando atributos vêm de múltiplas fontes:

- hierarquia organizacional;
- serviço externo;
- cache distribuído;
- cálculo de `linkedPessoaIds`.

Nesse caso, um `SubjectAttributeProvider` plugável resolve.

### 2. Montagem do ResourceContext

Se o recurso tem estrutura não padronizada, coleção complexa ou campos derivados, pode ser necessário um `ResourceContextAdapter` específico.

### 3. Tradução de policy para query SQL

Nem toda política é facilmente traduzível. Pode ser necessário um adaptador de query por recurso para alta performance em listagens grandes.

### 4. Resolução de relacionamentos complexos

Exemplo: “usuário pode ver ToDos de pessoas vinculadas por regras de parentesco + matrícula ativa + unidade vigente”.

Isso provavelmente exigirá um `RelationshipResolverPort` implementado na infraestrutura.

---

## 11. Trade-offs de performance, manutenção e complexidade

## 11.1 Performance

### Vantagens

- políticas e metadados podem ser cacheados;
- parte relevante das regras pode ser empurrada para SQL;
- reaproveitamento do motor reduz duplicação e inconsistência.

### Custos

- avaliação por campo e por anexo aumenta custo computacional;
- listagens podem sofrer se tudo for pós-filtrado em memória;
- montagem rica de contexto pode gerar overhead e N+1 se mal desenhada.

### Mitigações

- cache local/distribuído de políticas e metadados;
- contextos resumidos para listagem;
- batch loading de anexos e atributos;
- push-down de políticas simples para query;
- paginação obrigatória;
- auditoria assíncrona quando possível.

## 11.2 Manutenção

### Vantagens

- regras ficam centralizadas e auditáveis;
- novas features reutilizam o mesmo modelo mental;
- menor risco de “if de segurança” espalhado na aplicação.

### Custos

- exige governança forte de metadados e políticas;
- troubleshooting pode ficar mais abstrato;
- equipe precisa dominar o motor e a precedência das decisões.

### Mitigações

- painel administrativo de políticas com simulação;
- logs explicativos de decisão;
- versionamento de policy;
- testes automatizados de cenários críticos.

## 11.3 Complexidade arquitetural

### Vantagens

- flexibilidade alta;
- aderência a cenários multi-tenant e multi-organização;
- suporte natural a compliance e segregação fina.

### Custos

- maior complexidade inicial;
- demanda desenho cuidadoso de metamodelo;
- risco de superengenharia se o domínio for pequeno.

### Recomendação prática

Implementar em camadas evolutivas:

1. **fase 1**: RBAC + ABAC por recurso/instância;
2. **fase 2**: field-level masking;
3. **fase 3**: file-level metadata/download control;
4. **fase 4**: query push-down avançado e relationship resolvers sofisticados.

---

## Aplicação da arquitetura ao domínio ToDo

## Exemplo de regras suportadas

### 1. Pode ver ToDo, mas não `observacaoInterna`

- `ALLOW READ TODO`
- `DENY VIEW_FIELD TODO.observacaoInterna`

### 2. Pode ver `descricao` e `titulo`

- `ALLOW VIEW_FIELD TODO.titulo`
- `ALLOW VIEW_FIELD TODO.descricao`

### 3. Pode ver anexo `RG`

- `ALLOW LIST_FILE TODO fileType=RG`
- `ALLOW VIEW_FILE_METADATA TODO fileType=RG`
- `ALLOW DOWNLOAD_FILE TODO fileType=RG`

### 4. Não pode ver anexo `CPF`

- `DENY LIST_FILE TODO fileType=CPF`
- ou `ALLOW VIEW_FILE_METADATA` + `DENY DOWNLOAD_FILE`, conforme necessidade

### 5. Pode ver ToDo apenas se `subject.escolaId == resource.escolaId`

- policy condicional comparando atributo do sujeito com atributo do recurso

### 6. Pode ver ToDo apenas se `subject.tenantId == resource.tenantId`

- policy condicional de tenant

### 7. Pode ver apenas próprios ToDos

- policy `subject.pessoaId == resource.ownerPessoaId`

### 8. Pode ver ToDos de pessoas vinculadas

- policy `subject.linkedPessoaIds CONTAINS resource.ownerPessoaId`

### 9. Se o ToDo for sensível, `descricao` vem mascarada

- `MASK VIEW_FIELD TODO.descricao if resource.sensivel IS_TRUE`

---

## Recomendações finais de desenho

### 1. Default deny por padrão

Tudo que não estiver explicitamente permitido deve ser negado, especialmente para campos sensíveis e download de anexos.

### 2. Decisão explicável

O motor deve retornar não só o resultado, mas também:

- policies avaliadas;
- policy vencedora;
- motivo da decisão;
- nível da decisão (recurso, campo, arquivo).

Isso ajuda auditoria e suporte.

### 3. Separar autorização de serialização

A decisão pode ser única, mas a aplicação no payload deve ficar em componentes próprios (`FieldVisibilityResolver`, `FileAccessResolver`, `DataMaskingService`).

### 4. Modelar anexos como sub-recursos

Mesmo quando fisicamente pertencem ao ToDo, conceitualmente anexos devem ser tratados como sub-recursos autorizáveis.

### 5. Prever administração de metadados

No médio prazo, a arquitetura ganha muito valor quando acompanhada de:

- UI administrativa de políticas;
- validação sintática/semântica de políticas;
- ambiente de simulação “what-if”.

---

## Síntese executiva

A solução proposta usa **RBAC para habilitação macro** e **ABAC/PBAC metadata-driven para decisão fina**. O coração da arquitetura é um motor genérico que opera sobre `SubjectContext`, `ResourceContext`, `Action` e `Policy`, com decisões determinísticas e enforcement desacoplado da regra de negócio.

Com isso, o domínio ToDo valida que é possível suportar:

- acesso por tenant, escola, owner e vínculo;
- visibilidade seletiva por campo;
- mascaramento condicional;
- controle por tipo de anexo;
- separação entre metadado e download do arquivo;
- extensão para novos recursos com baixo acoplamento.

Na prática, a maior parte da evolução futura fica concentrada em **cadastro de metadados e políticas**, enquanto o código novo tende a se limitar a **casos de uso do recurso** e, eventualmente, **adaptadores técnicos pequenos** para contexto, relacionamento ou otimização de query.

---

## Modelagem relacional e migrations Flyway

### Diagrama textual das tabelas

```text
RBAC
  auth_role
    └──< auth_role_permission >── auth_permission
    └──< auth_user_role >── usuario

Cadastro genérico de recursos
  auth_resource
    ├──< auth_resource_action
    ├──< auth_resource_field
    ├──< auth_resource_file_type
    └──< auth_resource_relationship

Políticas
  auth_policy >── auth_resource
  auth_policy >── auth_role (opcional)
  auth_policy >── auth_permission (opcional)
  auth_policy
    ├──< auth_policy_condition
    ├──< auth_policy_field_target >── auth_resource_field
    └──< auth_policy_file_target >── auth_resource_file_type

Domínio de validação
  pessoa ──< usuario
  pessoa ──< todo
  usuario ──< todo
  todo ──< todo_attachment
```

### Papel das tabelas

- `auth_role`, `auth_permission`, `auth_role_permission`, `auth_user_role`: implementam RBAC macro desacoplado do domínio específico.
- `auth_resource`, `auth_resource_action`, `auth_resource_field`, `auth_resource_file_type`, `auth_resource_relationship`: registram metadados genéricos para qualquer `resourceType`, permitindo evolução para `ESCOLA`, `ALUNO`, `CONTRATO` e outros sem alterar o schema central.
- `auth_policy`, `auth_policy_condition`, `auth_policy_field_target`, `auth_policy_file_target`: materializam a camada ABAC/PBAC, incluindo efeito, prioridade, ação, alvo opcional de campo/anexo e condições comparando atributos de `SUBJECT`, `RESOURCE` e `CONTEXT`.
- `pessoa`, `usuario`, `todo`, `todo_attachment`: compõem o domínio mínimo de validação e demonstram como o motor genérico pode ser aplicado a um recurso real com campos sensíveis, tenant, escola e anexos tipados.

### Como a modelagem suporta novas features no futuro

A extensibilidade vem do fato de que o schema de autorização é dirigido por cadastro. Para uma nova feature, a evolução normal é:

1. criar apenas as tabelas de domínio da nova feature;
2. inserir um novo `auth_resource` com o `resourceType` correspondente;
3. cadastrar ações, campos, tipos de arquivo e relacionamentos nas tabelas metadata-driven;
4. criar políticas e condições sem alterar as tabelas centrais de autorização.

Em outras palavras, o schema base de autorização não precisa ser reescrito para cada novo recurso; o crescimento tende a ocorrer por dados e não por refatoração estrutural.
