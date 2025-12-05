package com.studybuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private ERole name;
    
    @Column(length = 100)
    private String description;
    
    // Constructor para crear roles fácilmente
    public Role(ERole name) {
        this.name = name;
        this.description = getDefaultDescription(name);
    }
    
    public Role(ERole name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Enum para los tipos de roles
    public enum ERole {
        ROLE_STUDENT,
        ROLE_MODERATOR,
        ROLE_ADMIN
    }
    
    // Método helper para obtener nombre legible
    public String getDisplayName() {
        return name.toString().replace("ROLE_", "");
    }
    
    // Método para descripción por defecto según rol
    private String getDefaultDescription(ERole role) {
        return switch (role) {
            case ROLE_STUDENT -> "Usuario regular del sistema, puede crear y unirse a grupos de estudio";
            case ROLE_MODERATOR -> "Modera contenido, gestiona reportes y ayuda en administración";
            case ROLE_ADMIN -> "Administrador completo del sistema con todos los privilegios";
        };
    }
    
    // Método para verificar si es un rol específico
    public boolean isStudent() {
        return this.name == ERole.ROLE_STUDENT;
    }
    
    public boolean isModerator() {
        return this.name == ERole.ROLE_MODERATOR;
    }
    
    public boolean isAdmin() {
        return this.name == ERole.ROLE_ADMIN;
    }
}