package com.studybuddy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI studyBuddyOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Servidor de desarrollo local");
        
        Server prodServer = new Server()
                .url("https://api.studybuddy.com")
                .description("Servidor de producción");
        
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Ingresa el token JWT. Obtenlo mediante /api/auth/login");
        
        Contact contact = new Contact()
                .name("Equipo Study Buddy")
                .email("support@studybuddy.com")
                .url("https://studybuddy.com");
        
        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");
        
        Info info = new Info()
                .title("Study Buddy Platform API")
                .description("""
                    API REST para la plataforma Study Buddy - Sistema de conexión entre estudiantes
                    
                    ### Características principales:
                    - Autenticación y autorización con JWT
                    - Gestión de usuarios con roles (STUDENT, MODERATOR, ADMIN)
                    - Sistema de grupos de estudio
                    - Chat en tiempo real
                    - APIs externas (cambio de moneda, consulta DNI)
                    - Sistema de matching entre estudiantes
                    
                    ### Tecnologías utilizadas:
                    - Spring Boot 3.1.5
                    - Spring Security
                    - Spring Data JPA
                    - MySQL 8.0
                    - Thymeleaf + Bootstrap
                    
                    ### Autenticación:
                    Para usar las APIs protegidas, primero obtén un token JWT mediante el endpoint `/api/auth/login`
                    """)
                .version("1.0.0")
                .contact(contact)
                .license(license);
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme));
    }
}