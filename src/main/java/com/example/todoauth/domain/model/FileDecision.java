package com.example.todoauth.domain.model;

import com.example.todoauth.domain.valueobject.FileAccessMode;

public record FileDecision(String fileType, FileAccessMode mode, String reason) {
}
