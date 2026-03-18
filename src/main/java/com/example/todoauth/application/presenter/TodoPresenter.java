package com.example.todoauth.application.presenter;

import com.example.todoauth.application.dto.TodoResponse;
import com.example.todoauth.infrastructure.persistence.TodoEntity;
import java.util.List;

public interface TodoPresenter {
    TodoResponse present(TodoEntity entity, List<?> attachments);
}
