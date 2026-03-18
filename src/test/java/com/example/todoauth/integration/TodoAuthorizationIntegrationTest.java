package com.example.todoauth.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.todoauth.infrastructure.persistence.*;
import com.example.todoauth.infrastructure.repository.*;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class TodoAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AuthRoleRepository authRoleRepository;
    @Autowired private AuthPermissionRepository authPermissionRepository;
    @Autowired private AuthUserRoleRepository authUserRoleRepository;
    @Autowired private AuthRolePermissionRepository authRolePermissionRepository;
    @Autowired private AuthResourceRepository authResourceRepository;
    @Autowired private AuthResourceFieldRepository authResourceFieldRepository;
    @Autowired private AuthResourceFileTypeRepository authResourceFileTypeRepository;
    @Autowired private AuthPolicyRepository authPolicyRepository;
    @Autowired private TodoRepository todoRepository;
    @Autowired private TodoAttachmentRepository todoAttachmentRepository;

    private Long visualizadorUserId;
    private Long gestorUserId;
    private Long operadorUserId;
    private Long adminUserId;
    private Long todoMesmoEscopoId;
    private Long todoOutraEscolaId;
    private Long todoOutroTenantId;
    private Long todoSensivelOperadorId;
    private Long cpfAttachmentId;
    private Long contratoAttachmentId;

    @BeforeEach
    void setUp() {
        cleanup();
        seedAuthorizationBase();
        seedDomain();
        seedPolicies();
    }

    @Test
    void getTodoByIdShouldHideObservacaoInternaAndMaskDescricao() throws Exception {
        mockMvc.perform(get("/api/todos/{id}", todoSensivelOperadorId)
                        .header("X-User-Id", visualizadorUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fields.titulo", is("Contrato sensível")))
                .andExpect(jsonPath("$.fields.descricao", not("Descrição altamente sensível do contrato")))
                .andExpect(jsonPath("$.fields.observacaoInterna").doesNotExist());
    }

    @Test
    void listAttachmentsShouldShowRgButNotCpf() throws Exception {
        mockMvc.perform(get("/api/todos/{id}/attachments", todoMesmoEscopoId)
                        .header("X-User-Id", visualizadorUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipoArquivo", contains("RG")))
                .andExpect(jsonPath("$[*].tipoArquivo", not(hasItem("CPF"))));
    }

    @Test
    void listAttachmentsShouldReturnMetadataOnlyForContrato() throws Exception {
        mockMvc.perform(get("/api/todos/{id}/attachments", todoSensivelOperadorId)
                        .header("X-User-Id", operadorUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoArquivo", is("CONTRATO")))
                .andExpect(jsonPath("$[0].metadataOnly", is(true)))
                .andExpect(jsonPath("$[0].downloadable", is(false)))
                .andExpect(jsonPath("$[0].caminhoStorage").doesNotExist());
    }

    @Test
    void listTodosShouldFilterByEscolaForGestor() throws Exception {
        mockMvc.perform(get("/api/todos")
                        .header("X-User-Id", gestorUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(todoMesmoEscopoId.intValue())))
                .andExpect(jsonPath("$[*].id", hasItem(todoSensivelOperadorId.intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(todoOutraEscolaId.intValue()))))
                .andExpect(jsonPath("$[*].id", not(hasItem(todoOutroTenantId.intValue()))));
    }

    @Test
    void listTodosShouldFilterToOwnTodosForOperador() throws Exception {
        mockMvc.perform(get("/api/todos")
                        .header("X-User-Id", operadorUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", everyItem(anyOf(is(todoMesmoEscopoId.intValue()), is(todoSensivelOperadorId.intValue())))));
    }

    @Test
    void downloadAttachmentShouldBeBlockedByPolicy() throws Exception {
        mockMvc.perform(get("/api/todos/{todoId}/attachments/{attachmentId}/download", todoMesmoEscopoId, cpfAttachmentId)
                        .header("X-User-Id", visualizadorUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    void evaluateEndpointShouldExplainTenantDenial() throws Exception {
        String payload = """
                {
                  "userId": %d,
                  "resourceType": "TODO",
                  "resourceId": %d,
                  "action": "READ"
                }
                """.formatted(visualizadorUserId, todoOutroTenantId);

        mockMvc.perform(post("/api/admin/authorization/evaluate")
                        .header("X-User-Id", adminUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed", is(false)))
                .andExpect(jsonPath("$.reason", containsString("DENY_OTHER_TENANT_READ")));
    }

    private void cleanup() {
        authUserRoleRepository.deleteAll();
        authRolePermissionRepository.deleteAll();
        todoAttachmentRepository.deleteAll();
        todoRepository.deleteAll();
        authPolicyRepository.deleteAll();
        authResourceFileTypeRepository.deleteAll();
        authResourceFieldRepository.deleteAll();
        authResourceRepository.deleteAll();
        usuarioRepository.deleteAll();
        pessoaRepository.deleteAll();
        authPermissionRepository.deleteAll();
        authRoleRepository.deleteAll();
    }

    private void seedAuthorizationBase() {
        AuthRoleEntity admin = role(1L, "ADMIN");
        AuthRoleEntity gestor = role(2L, "GESTOR_ESCOLA");
        AuthRoleEntity operador = role(3L, "OPERADOR");
        AuthRoleEntity visualizador = role(4L, "VISUALIZADOR_RESTRITO");
        authRoleRepository.saveAll(List.of(admin, gestor, operador, visualizador));

        AuthPermissionEntity read = permission(1L, "TODO_READ");
        AuthPermissionEntity list = permission(2L, "TODO_LIST");
        AuthPermissionEntity metadata = permission(3L, "TODO_ATTACHMENT_METADATA");
        AuthPermissionEntity download = permission(4L, "TODO_ATTACHMENT_DOWNLOAD");
        AuthPermissionEntity attachmentList = permission(5L, "TODO_ATTACHMENT_LIST");
        authPermissionRepository.saveAll(List.of(read, list, metadata, download, attachmentList));

        saveRolePermission(admin, read); saveRolePermission(admin, list); saveRolePermission(admin, metadata); saveRolePermission(admin, download); saveRolePermission(admin, attachmentList);
        saveRolePermission(gestor, read); saveRolePermission(gestor, list);
        saveRolePermission(operador, read); saveRolePermission(operador, list); saveRolePermission(operador, metadata); saveRolePermission(operador, attachmentList);
        saveRolePermission(visualizador, read); saveRolePermission(visualizador, list); saveRolePermission(visualizador, metadata); saveRolePermission(visualizador, download); saveRolePermission(visualizador, attachmentList);

        AuthResourceEntity resource = new AuthResourceEntity();
        resource.setResourceType("TODO");
        resource.setResourceName("ToDo");
        resource.setTableName("todo");
        resource.setIdFieldName("id");
        resource.setTenantFieldName("tenant_id");
        resource.setEscolaFieldName("escola_id");
        resource.setOwnerFieldName("owner_pessoa_id");
        resource.setSensitivityFieldName("sensivel");
        resource.setSupportsAttachments(true);
        resource.setActive(true);
        resource = authResourceRepository.save(resource);

        authResourceFieldRepository.save(field(resource, "titulo", "VISIBLE", null));
        authResourceFieldRepository.save(field(resource, "descricao", "VISIBLE", "PARTIAL_TEXT"));
        authResourceFieldRepository.save(field(resource, "observacaoInterna", "HIDDEN", "NULLIFY"));

        authResourceFileTypeRepository.save(fileType(resource, "RG"));
        authResourceFileTypeRepository.save(fileType(resource, "CPF"));
        authResourceFileTypeRepository.save(fileType(resource, "CONTRATO"));
    }

    private void seedDomain() {
        PessoaEntity adminPessoa = pessoa("Admin", 1L, 10L);
        PessoaEntity gestorPessoa = pessoa("Gestor", 1L, 10L);
        PessoaEntity operadorPessoa = pessoa("Operador", 1L, 10L);
        PessoaEntity visualPessoa = pessoa("Visual", 1L, 10L);
        PessoaEntity outraEscolaPessoa = pessoa("Outra Escola", 1L, 20L);
        PessoaEntity outroTenantPessoa = pessoa("Outro Tenant", 2L, 30L);
        pessoaRepository.saveAll(List.of(adminPessoa, gestorPessoa, operadorPessoa, visualPessoa, outraEscolaPessoa, outroTenantPessoa));

        UsuarioEntity admin = user("admin", adminPessoa, 1L, 10L);
        UsuarioEntity gestor = user("gestor", gestorPessoa, 1L, 10L);
        UsuarioEntity operador = user("operador", operadorPessoa, 1L, 10L);
        UsuarioEntity visual = user("visual", visualPessoa, 1L, 10L);
        UsuarioEntity escola20 = user("escola20", outraEscolaPessoa, 1L, 20L);
        UsuarioEntity tenant2 = user("tenant2", outroTenantPessoa, 2L, 30L);
        usuarioRepository.saveAll(List.of(admin, gestor, operador, visual, escola20, tenant2));

        adminUserId = admin.getId();
        gestorUserId = gestor.getId();
        operadorUserId = operador.getId();
        visualizadorUserId = visual.getId();

        saveUserRole(admin, authRoleRepository.findById(1L).orElseThrow());
        saveUserRole(gestor, authRoleRepository.findById(2L).orElseThrow());
        saveUserRole(operador, authRoleRepository.findById(3L).orElseThrow());
        saveUserRole(visual, authRoleRepository.findById(4L).orElseThrow());
        saveUserRole(escola20, authRoleRepository.findById(4L).orElseThrow());
        saveUserRole(tenant2, authRoleRepository.findById(4L).orElseThrow());

        TodoEntity todoMesmoEscopo = todo("ToDo da escola 10", "Descrição comum", "Obs interna", operadorPessoa, gestor, 1L, 10L, false);
        TodoEntity todoSensivel = todo("Contrato sensível", "Descrição altamente sensível do contrato", "Obs muito interna", operadorPessoa, gestor, 1L, 10L, true);
        TodoEntity todoOutraEscola = todo("ToDo da escola 20", "Descrição escola 20", "Obs escola 20", outraEscolaPessoa, escola20, 1L, 20L, false);
        TodoEntity todoOutroTenant = todo("ToDo tenant 2", "Descrição tenant 2", "Obs tenant 2", outroTenantPessoa, tenant2, 2L, 30L, false);
        todoRepository.saveAll(List.of(todoMesmoEscopo, todoSensivel, todoOutraEscola, todoOutroTenant));

        todoMesmoEscopoId = todoMesmoEscopo.getId();
        todoSensivelOperadorId = todoSensivel.getId();
        todoOutraEscolaId = todoOutraEscola.getId();
        todoOutroTenantId = todoOutroTenant.getId();

        TodoAttachmentEntity rg = attachment(todoMesmoEscopo, "RG", "/storage/rg.pdf");
        TodoAttachmentEntity cpf = attachment(todoMesmoEscopo, "CPF", "/storage/cpf.pdf");
        TodoAttachmentEntity contrato = attachment(todoSensivel, "CONTRATO", "/storage/contrato.pdf");
        todoAttachmentRepository.saveAll(List.of(rg, cpf, contrato));
        cpfAttachmentId = cpf.getId();
        contratoAttachmentId = contrato.getId();
    }

    private void seedPolicies() {
        AuthResourceEntity resource = authResourceRepository.findByResourceType("TODO").orElseThrow();
        AuthRoleEntity admin = authRoleRepository.findById(1L).orElseThrow();
        AuthRoleEntity gestor = authRoleRepository.findById(2L).orElseThrow();
        AuthRoleEntity operador = authRoleRepository.findById(3L).orElseThrow();
        AuthRoleEntity visualizador = authRoleRepository.findById(4L).orElseThrow();
        AuthPermissionEntity read = authPermissionRepository.findById(1L).orElseThrow();
        AuthPermissionEntity list = authPermissionRepository.findById(2L).orElseThrow();
        AuthPermissionEntity metadata = authPermissionRepository.findById(3L).orElseThrow();
        AuthPermissionEntity download = authPermissionRepository.findById(4L).orElseThrow();
        AuthPermissionEntity attachmentList = authPermissionRepository.findById(5L).orElseThrow();
        AuthResourceFieldEntity descricaoField = authResourceFieldRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc("TODO").stream().filter(f -> f.getFieldName().equals("descricao")).findFirst().orElseThrow();
        AuthResourceFieldEntity observacaoField = authResourceFieldRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc("TODO").stream().filter(f -> f.getFieldName().equals("observacaoInterna")).findFirst().orElseThrow();
        AuthResourceFileTypeEntity rgType = authResourceFileTypeRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc("TODO").stream().filter(f -> f.getFileTypeCode().equals("RG")).findFirst().orElseThrow();
        AuthResourceFileTypeEntity cpfType = authResourceFileTypeRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc("TODO").stream().filter(f -> f.getFileTypeCode().equals("CPF")).findFirst().orElseThrow();
        AuthResourceFileTypeEntity contratoType = authResourceFileTypeRepository.findByResource_ResourceTypeAndActiveTrueOrderByIdAsc("TODO").stream().filter(f -> f.getFileTypeCode().equals("CONTRATO")).findFirst().orElseThrow();

        authPolicyRepository.save(policy(resource, admin, null, "ADMIN_READ", "READ", "ALLOW", 1000, List.of(), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, admin, null, "ADMIN_LIST", "LIST", "ALLOW", 1000, List.of(), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, admin, null, "ADMIN_VIEW_FIELD", "VIEW_FIELD", "ALLOW", 1000, List.of(), List.of(descricaoField, observacaoField), List.of()));
        authPolicyRepository.save(policy(resource, admin, null, "ADMIN_LIST_FILE", "LIST_FILE", "ALLOW", 1000, List.of(), List.of(), List.of(rgType, cpfType, contratoType)));
        authPolicyRepository.save(policy(resource, admin, null, "ADMIN_META_FILE", "VIEW_FILE_METADATA", "ALLOW", 1000, List.of(), List.of(), List.of(rgType, cpfType, contratoType)));
        authPolicyRepository.save(policy(resource, admin, null, "ADMIN_DOWNLOAD_FILE", "DOWNLOAD_FILE", "ALLOW", 1000, List.of(), List.of(), List.of(rgType, cpfType, contratoType)));

        authPolicyRepository.save(policy(resource, visualizador, read, "VISUAL_READ", "READ", "ALLOW", 100, List.of(), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, visualizador, list, "VISUAL_LIST", "LIST", "ALLOW", 100, List.of(), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, null, null, "DENY_OTHER_TENANT_READ", "READ", "DENY", 950,
                List.of(condition("SUBJECT", "tenantId", "NOT_EQUALS", null, "RESOURCE", "tenantId")), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, null, null, "DENY_OTHER_TENANT_LIST", "LIST", "DENY", 950,
                List.of(condition("SUBJECT", "tenantId", "NOT_EQUALS", null, "RESOURCE", "tenantId")), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, visualizador, null, "DENY_INTERNAL_NOTE", "VIEW_FIELD", "DENY", 800,
                List.of(), List.of(observacaoField), List.of()));
        authPolicyRepository.save(policy(resource, visualizador, null, "ALLOW_DESCRIPTION", "VIEW_FIELD", "ALLOW", 200,
                List.of(), List.of(descricaoField), List.of()));
        authPolicyRepository.save(policy(resource, visualizador, null, "MASK_DESCRIPTION", "VIEW_FIELD", "MASK", 850,
                List.of(condition("RESOURCE", "sensivel", "IS_TRUE", null, null, null)), List.of(descricaoField), List.of()));
        authPolicyRepository.save(policy(resource, visualizador, attachmentList, "ALLOW_RG_LIST", "LIST_FILE", "ALLOW", 300,
                List.of(), List.of(), List.of(rgType)));
        authPolicyRepository.save(policy(resource, visualizador, metadata, "ALLOW_RG_METADATA", "VIEW_FILE_METADATA", "ALLOW", 300,
                List.of(), List.of(), List.of(rgType)));
        authPolicyRepository.save(policy(resource, visualizador, download, "ALLOW_RG_DOWNLOAD", "DOWNLOAD_FILE", "ALLOW", 300,
                List.of(), List.of(), List.of(rgType)));
        authPolicyRepository.save(policy(resource, visualizador, null, "DENY_CPF_LIST", "LIST_FILE", "DENY", 850,
                List.of(condition("RESOURCE", "fileType", "EQUALS", "CPF", null, null)), List.of(), List.of(cpfType)));
        authPolicyRepository.save(policy(resource, visualizador, null, "DENY_CPF_DOWNLOAD", "DOWNLOAD_FILE", "DENY", 900,
                List.of(condition("RESOURCE", "fileType", "EQUALS", "CPF", null, null)), List.of(), List.of(cpfType)));

        authPolicyRepository.save(policy(resource, gestor, read, "GESTOR_READ", "READ", "ALLOW", 300,
                List.of(condition("SUBJECT", "escolaId", "EQUALS", null, "RESOURCE", "escolaId")), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, gestor, list, "GESTOR_LIST", "LIST", "ALLOW", 300,
                List.of(condition("SUBJECT", "escolaId", "EQUALS", null, "RESOURCE", "escolaId")), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, gestor, null, "GESTOR_DENY_OTHER_ESCOLA_READ", "READ", "DENY", 900,
                List.of(condition("SUBJECT", "escolaId", "NOT_EQUALS", null, "RESOURCE", "escolaId")), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, gestor, null, "GESTOR_DENY_OTHER_ESCOLA_LIST", "LIST", "DENY", 900,
                List.of(condition("SUBJECT", "escolaId", "NOT_EQUALS", null, "RESOURCE", "escolaId")), List.of(), List.of()));

        authPolicyRepository.save(policy(resource, operador, read, "OPERADOR_OWN_READ", "READ", "ALLOW", 350,
                List.of(condition("SUBJECT", "pessoaId", "EQUALS", null, "RESOURCE", "ownerPessoaId")), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, operador, list, "OPERADOR_OWN_LIST", "LIST", "ALLOW", 350,
                List.of(condition("SUBJECT", "pessoaId", "EQUALS", null, "RESOURCE", "ownerPessoaId")), List.of(), List.of()));
        authPolicyRepository.save(policy(resource, operador, metadata, "OPERADOR_CONTRATO_META_ONLY", "VIEW_FILE_METADATA", "METADATA_ONLY", 700,
                List.of(condition("RESOURCE", "fileType", "EQUALS", "CONTRATO", null, null)), List.of(), List.of(contratoType)));
        authPolicyRepository.save(policy(resource, operador, null, "OPERADOR_DENY_CONTRATO_DOWNLOAD", "DOWNLOAD_FILE", "DENY", 800,
                List.of(condition("RESOURCE", "fileType", "EQUALS", "CONTRATO", null, null)), List.of(), List.of(contratoType)));
        authPolicyRepository.save(policy(resource, operador, attachmentList, "OPERADOR_ALLOW_CONTRATO_LIST", "LIST_FILE", "ALLOW", 300,
                List.of(), List.of(), List.of(contratoType)));
    }

    private AuthRoleEntity role(Long id, String code) {
        AuthRoleEntity role = new AuthRoleEntity();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(code);
        role.setActive(true);
        return role;
    }

    private AuthPermissionEntity permission(Long id, String code) {
        AuthPermissionEntity permission = new AuthPermissionEntity();
        permission.setId(id);
        permission.setPermissionCode(code);
        permission.setPermissionName(code);
        permission.setActive(true);
        return permission;
    }

    private void saveRolePermission(AuthRoleEntity role, AuthPermissionEntity permission) {
        AuthRolePermissionEntity entity = new AuthRolePermissionEntity();
        entity.setId(new AuthRolePermissionId(role.getId(), permission.getId()));
        entity.setRole(role);
        entity.setPermission(permission);
        authRolePermissionRepository.save(entity);
    }

    private void saveUserRole(UsuarioEntity user, AuthRoleEntity role) {
        AuthUserRoleEntity entity = new AuthUserRoleEntity();
        entity.setId(new AuthUserRoleId(user.getId(), role.getId()));
        entity.setUser(user);
        entity.setRole(role);
        authUserRoleRepository.save(entity);
    }

    private AuthResourceFieldEntity field(AuthResourceEntity resource, String name, String visibility, String maskStrategy) {
        AuthResourceFieldEntity field = new AuthResourceFieldEntity();
        field.setResource(resource);
        field.setFieldName(name);
        field.setDisplayName(name);
        field.setDataType("STRING");
        field.setSensitive(true);
        field.setMaskStrategy(maskStrategy);
        field.setDefaultVisibility(visibility);
        field.setQueryable(true);
        field.setSortable(false);
        field.setActive(true);
        return field;
    }

    private AuthResourceFileTypeEntity fileType(AuthResourceEntity resource, String code) {
        AuthResourceFileTypeEntity fileType = new AuthResourceFileTypeEntity();
        fileType.setResource(resource);
        fileType.setFileTypeCode(code);
        fileType.setDisplayName(code);
        fileType.setSensitive(true);
        fileType.setDefaultAccessLevel("DENY");
        fileType.setActive(true);
        return fileType;
    }

    private PessoaEntity pessoa(String nome, Long tenantId, Long escolaId) {
        PessoaEntity entity = new PessoaEntity();
        entity.setNome(nome);
        entity.setDocumento(nome + "-DOC");
        entity.setTenantId(tenantId);
        entity.setEscolaId(escolaId);
        entity.setActive(true);
        return entity;
    }

    private UsuarioEntity user(String username, PessoaEntity pessoa, Long tenantId, Long escolaId) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setUsername(username);
        entity.setEmail(username + "@example.com");
        entity.setSenhaHash("noop");
        entity.setPessoa(pessoa);
        entity.setTenantId(tenantId);
        entity.setEscolaId(escolaId);
        entity.setActive(true);
        return entity;
    }

    private TodoEntity todo(String titulo, String descricao, String observacao, PessoaEntity owner, UsuarioEntity criador, Long tenantId, Long escolaId, boolean sensivel) {
        TodoEntity entity = new TodoEntity();
        entity.setTitulo(titulo);
        entity.setDescricao(descricao);
        entity.setObservacaoInterna(observacao);
        entity.setPrioridade("ALTA");
        entity.setStatus("ABERTO");
        entity.setDataCriacao(OffsetDateTime.now());
        entity.setOwnerPessoa(owner);
        entity.setCriadoPorUsuario(criador);
        entity.setTenantId(tenantId);
        entity.setEscolaId(escolaId);
        entity.setSensivel(sensivel);
        return entity;
    }

    private TodoAttachmentEntity attachment(TodoEntity todo, String tipoArquivo, String caminho) {
        TodoAttachmentEntity entity = new TodoAttachmentEntity();
        entity.setTodo(todo);
        entity.setNomeArquivo(tipoArquivo.toLowerCase() + ".pdf");
        entity.setTipoArquivo(tipoArquivo);
        entity.setContentType("application/pdf");
        entity.setCaminhoStorage(caminho);
        entity.setTenantId(todo.getTenantId());
        entity.setSensivel(true);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }

    private AuthPolicyEntity policy(AuthResourceEntity resource, AuthRoleEntity role, AuthPermissionEntity permission, String code, String action, String effect, int priority,
                                    List<AuthPolicyConditionEntity> conditions, List<AuthResourceFieldEntity> fields, List<AuthResourceFileTypeEntity> fileTypes) {
        AuthPolicyEntity policy = new AuthPolicyEntity();
        policy.setPolicyCode(code);
        policy.setPolicyName(code);
        policy.setResource(resource);
        policy.setActionCode(action);
        policy.setEffect(effect);
        policy.setPriority(priority);
        policy.setActive(true);
        policy.setScopeLevel(fields.isEmpty() ? (fileTypes.isEmpty() ? "RESOURCE" : "FILE") : "FIELD");
        policy.setConditionLogic("AND");
        policy.setAppliesToRole(role);
        policy.setAppliesToPermission(permission);
        conditions.forEach(condition -> {
            condition.setPolicy(policy);
            condition.setActive(true);
            policy.getConditions().add(condition);
        });
        fields.forEach(field -> {
            AuthPolicyFieldTargetEntity target = new AuthPolicyFieldTargetEntity();
            target.setPolicy(policy);
            target.setResourceField(field);
            policy.getFieldTargets().add(target);
        });
        fileTypes.forEach(fileType -> {
            AuthPolicyFileTargetEntity target = new AuthPolicyFileTargetEntity();
            target.setPolicy(policy);
            target.setResourceFileType(fileType);
            policy.getFileTargets().add(target);
        });
        return policy;
    }

    private AuthPolicyConditionEntity condition(String source, String attribute, String operator, String expectedValue, String comparisonSource, String comparisonAttribute) {
        AuthPolicyConditionEntity condition = new AuthPolicyConditionEntity();
        condition.setConditionOrder(1);
        condition.setSourceType(source);
        condition.setAttributeName(attribute);
        condition.setOperator(operator);
        condition.setExpectedValue(expectedValue);
        condition.setComparisonType(comparisonSource == null ? "LITERAL" : "ATTRIBUTE_REFERENCE");
        condition.setComparisonSourceType(comparisonSource);
        condition.setComparisonAttributeName(comparisonAttribute);
        return condition;
    }
}
