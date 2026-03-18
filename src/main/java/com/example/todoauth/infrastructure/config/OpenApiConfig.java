package com.example.todoauth.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI todoAuthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("todoauth API")
                        .description("API de demonstração para autorização genérica com RBAC + ABAC/PBAC no domínio ToDo.")
                        .version("v1")
                        .contact(new Contact().name("OpenAI Codex Demo"))
                        .license(new License().name("Uso interno / demonstração")));
    }
}
