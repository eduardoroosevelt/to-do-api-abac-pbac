package com.example.todoauth.application.gateway;

import com.example.todoauth.domain.model.SubjectContext;

public interface CurrentSubjectGateway {
    SubjectContext getCurrentSubject();
}
