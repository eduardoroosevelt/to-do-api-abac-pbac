package com.example.todoauth.application.usecase;

import com.example.todoauth.application.dto.*;
import com.example.todoauth.application.exception.ForbiddenException;
import com.example.todoauth.application.exception.NotFoundException;
import com.example.todoauth.application.gateway.CurrentSubjectGateway;
import com.example.todoauth.domain.model.ResourceContext;
import com.example.todoauth.domain.model.SubjectContext;
import com.example.todoauth.domain.valueobject.Action;
import com.example.todoauth.domain.valueobject.FileAccessMode;
import com.example.todoauth.infrastructure.authorization.FieldVisibilityResolver;
import com.example.todoauth.infrastructure.authorization.FileAccessResolver;
import com.example.todoauth.infrastructure.authorization.ResourceContextFactory;
import com.example.todoauth.infrastructure.mapper.TodoAttachmentMapper;
import com.example.todoauth.infrastructure.mapper.TodoMapper;
import com.example.todoauth.infrastructure.persistence.PessoaEntity;
import com.example.todoauth.infrastructure.persistence.TodoAttachmentEntity;
import com.example.todoauth.infrastructure.persistence.TodoEntity;
import com.example.todoauth.infrastructure.persistence.UsuarioEntity;
import com.example.todoauth.infrastructure.repository.PessoaRepository;
import com.example.todoauth.infrastructure.repository.TodoAttachmentRepository;
import com.example.todoauth.infrastructure.repository.TodoRepository;
import com.example.todoauth.infrastructure.repository.UsuarioRepository;
import com.example.todoauth.infrastructure.storage.InMemoryAttachmentContentService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoApplicationService implements TodoUseCase {
    private final TodoRepository todoRepository;
    private final TodoAttachmentRepository todoAttachmentRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TodoMapper todoMapper;
    private final TodoAttachmentMapper todoAttachmentMapper;
    private final CurrentSubjectGateway currentSubjectGateway;
    private final AuthorizationService authorizationService;
    private final ResourceContextFactory resourceContextFactory;
    private final FieldVisibilityResolver fieldVisibilityResolver;
    private final FileAccessResolver fileAccessResolver;
    private final InMemoryAttachmentContentService attachmentContentService;

    public TodoApplicationService(TodoRepository todoRepository, TodoAttachmentRepository todoAttachmentRepository, PessoaRepository pessoaRepository, UsuarioRepository usuarioRepository, TodoMapper todoMapper, TodoAttachmentMapper todoAttachmentMapper, CurrentSubjectGateway currentSubjectGateway, AuthorizationService authorizationService, ResourceContextFactory resourceContextFactory, FieldVisibilityResolver fieldVisibilityResolver, FileAccessResolver fileAccessResolver, InMemoryAttachmentContentService attachmentContentService) {
        this.todoRepository = todoRepository;
        this.todoAttachmentRepository = todoAttachmentRepository;
        this.pessoaRepository = pessoaRepository;
        this.usuarioRepository = usuarioRepository;
        this.todoMapper = todoMapper;
        this.todoAttachmentMapper = todoAttachmentMapper;
        this.currentSubjectGateway = currentSubjectGateway;
        this.authorizationService = authorizationService;
        this.resourceContextFactory = resourceContextFactory;
        this.fieldVisibilityResolver = fieldVisibilityResolver;
        this.fileAccessResolver = fileAccessResolver;
        this.attachmentContentService = attachmentContentService;
    }

    @Override
    public TodoResponse create(TodoCreateRequest request) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        ResourceContext resource = new ResourceContext("TODO", null, Map.of("tenantId", subject.tenantId(), "escolaId", request.escolaId(), "ownerPessoaId", request.ownerPessoaId(), "sensivel", Boolean.TRUE.equals(request.sensivel())), Map.of(), Set.of(), Boolean.TRUE.equals(request.sensivel()));
        authorizationService.checkAllowed(subject, resource, Action.CREATE);
        PessoaEntity owner = pessoaRepository.findById(request.ownerPessoaId()).orElseThrow(() -> new NotFoundException("Pessoa não encontrada"));
        UsuarioEntity user = usuarioRepository.findById(subject.userId()).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        TodoEntity saved = todoRepository.save(todoMapper.toEntity(request, owner, user, subject.tenantId()));
        return toResponse(subject, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponse getById(Long id) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        TodoEntity todo = loadTodo(id);
        ResourceContext resource = buildTodoContext(todo);
        authorizationService.checkAllowed(subject, resource, Action.READ);
        return toResponse(subject, todo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> list() {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        return todoRepository.findAllByOrderByIdAsc().stream()
                .filter(todo -> authorizationService.authorize(subject, buildTodoContext(todo), Action.LIST).allowed())
                .map(todo -> toResponse(subject, todo))
                .toList();
    }

    @Override
    public TodoResponse update(Long id, TodoUpdateRequest request) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        TodoEntity todo = loadTodo(id);
        authorizationService.checkAllowed(subject, buildTodoContext(todo), Action.UPDATE);
        PessoaEntity owner = pessoaRepository.findById(request.ownerPessoaId()).orElseThrow(() -> new NotFoundException("Pessoa não encontrada"));
        todoMapper.updateEntity(todo, request, owner, subject.tenantId());
        return toResponse(subject, todoRepository.save(todo));
    }

    @Override
    public void delete(Long id) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        TodoEntity todo = loadTodo(id);
        authorizationService.checkAllowed(subject, buildTodoContext(todo), Action.DELETE);
        todoRepository.delete(todo);
    }

    @Override
    public TodoAttachmentResponse addAttachment(Long todoId, TodoAttachmentCreateRequest request) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        TodoEntity todo = loadTodo(todoId);
        authorizationService.checkAllowed(subject, buildTodoContext(todo), Action.UPDATE);
        TodoAttachmentEntity saved = todoAttachmentRepository.save(todoAttachmentMapper.toEntity(request, todo));
        return new TodoAttachmentResponse(saved.getId(), saved.getNomeArquivo(), saved.getTipoArquivo(), saved.getContentType(), saved.getCaminhoStorage(), true, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoAttachmentResponse> listAttachments(Long todoId) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        TodoEntity todo = loadTodo(todoId);
        authorizationService.checkAllowed(subject, buildTodoContext(todo), Action.READ);
        return fileAccessResolver.resolveList(subject, todoAttachmentRepository.findByTodo_IdOrderByIdAsc(todoId));
    }

    @Override
    @Transactional(readOnly = true)
    public TodoAttachmentResponse getAttachmentMetadata(Long todoId, Long attachmentId) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        TodoAttachmentEntity attachment = loadAttachment(todoId, attachmentId);
        FileAccessMode mode = fileAccessResolver.resolveMetadata(subject, attachment);
        if (mode == FileAccessMode.HIDE) {
            throw new ForbiddenException("Metadado do anexo não autorizado");
        }
        boolean metadataOnly = mode == FileAccessMode.METADATA_ONLY || fileAccessResolver.resolveDownload(subject, attachment) != FileAccessMode.DOWNLOAD;
        return new TodoAttachmentResponse(attachment.getId(), attachment.getNomeArquivo(), attachment.getTipoArquivo(), attachment.getContentType(), metadataOnly ? null : attachment.getCaminhoStorage(), !metadataOnly, metadataOnly);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayResource downloadAttachment(Long todoId, Long attachmentId) {
        SubjectContext subject = currentSubjectGateway.getCurrentSubject();
        TodoAttachmentEntity attachment = loadAttachment(todoId, attachmentId);
        if (fileAccessResolver.resolveDownload(subject, attachment) != FileAccessMode.DOWNLOAD) {
            throw new ForbiddenException("Download do anexo não autorizado");
        }
        return attachmentContentService.download(attachment);
    }

    private TodoEntity loadTodo(Long id) {
        return todoRepository.findById(id).orElseThrow(() -> new NotFoundException("ToDo não encontrado"));
    }

    private TodoAttachmentEntity loadAttachment(Long todoId, Long attachmentId) {
        return todoAttachmentRepository.findByIdAndTodo_Id(attachmentId, todoId).orElseThrow(() -> new NotFoundException("Anexo não encontrado"));
    }

    private ResourceContext buildTodoContext(TodoEntity todo) {
        List<TodoAttachmentEntity> attachments = todoAttachmentRepository.findByTodo_IdOrderByIdAsc(todo.getId());
        Set<String> fileTypes = attachments.stream().map(TodoAttachmentEntity::getTipoArquivo).collect(java.util.stream.Collectors.toSet());
        return resourceContextFactory.fromTodo(todo, fileTypes);
    }

    private TodoResponse toResponse(SubjectContext subject, TodoEntity todo) {
        ResourceContext resource = buildTodoContext(todo);
        Map<String, Object> resolvedFields = fieldVisibilityResolver.resolve(subject, resource, new LinkedHashMap<>(resource.fields()));
        List<TodoAttachmentResponse> attachments = fileAccessResolver.resolveList(subject, todoAttachmentRepository.findByTodo_IdOrderByIdAsc(todo.getId()));
        return new TodoResponse(todo.getId(), resolvedFields, attachments, todo.getDataCriacao(), todo.getDataLimite());
    }
}
