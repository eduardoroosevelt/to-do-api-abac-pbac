package com.example.todoauth.domain.model;

import java.util.Map;
import java.util.Set;

public record ResourceContext(
        String resourceType,
        Long resourceId,
        Map<String, Object> attributes,
        Map<String, Object> fields,
        Set<String> fileTypes,
        boolean sensitive
) {
    public Object getAttribute(String name) {
        if ("resourceType".equals(name)) {
            return resourceType;
        }
        if ("resourceId".equals(name)) {
            return resourceId;
        }
        if ("sensivel".equals(name) || "sensitive".equals(name)) {
            return sensitive;
        }
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return fields.get(name);
    }
}
