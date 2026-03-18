package com.example.todoauth.infrastructure.authorization;

import org.springframework.stereotype.Service;

@Service
public class DataMaskingService {
    public Object mask(Object value, String strategy) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        String effective = strategy == null ? "FIXED_MASK" : strategy;
        return switch (effective) {
            case "NULLIFY" -> null;
            case "PARTIAL_TEXT" -> text.length() <= 4 ? "****" : text.substring(0, 2) + "***" + text.substring(text.length() - 2);
            case "LAST_N_CHARS" -> text.length() <= 3 ? text : "***" + text.substring(text.length() - 3);
            case "HASH" -> Integer.toHexString(text.hashCode());
            default -> "***";
        };
    }
}
