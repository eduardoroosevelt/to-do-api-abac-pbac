package com.example.todoauth.domain.model;

import com.example.todoauth.domain.valueobject.FieldAccessMode;

public record FieldDecision(String fieldName, FieldAccessMode mode, String maskStrategy, String reason) {
}
