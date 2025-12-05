package com.studybuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 50)
    private String icon;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    // Constructor para creación simplificada
    public Interest(String name, String category) {
        this.name = name;
        this.category = category;
        this.description = "";
        this.icon = getDefaultIcon(category);
        this.isActive = true;
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    public Interest(String name, String category, String description) {
        this(name, category);
        this.description = description;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
        if (icon == null) {
            icon = getDefaultIcon(category);
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
    
    // Método para obtener icono por defecto según categoría
    private String getDefaultIcon(String category) {
        if (category == null) return "fas fa-graduation-cap";
        
        return switch (category.toLowerCase()) {
            case "ciencias", "science" -> "fas fa-flask";
            case "tecnología", "technology", "programación" -> "fas fa-laptop-code";
            case "matemáticas", "mathematics" -> "fas fa-calculator";
            case "humanidades", "humanities" -> "fas fa-book-open";
            case "salud", "health" -> "fas fa-heartbeat";
            case "negocios", "business" -> "fas fa-chart-line";
            case "arte", "art" -> "fas fa-palette";
            case "lenguas", "languages" -> "fas fa-language";
            case "deportes", "sports" -> "fas fa-running";
            case "música", "music" -> "fas fa-music";
            case "ingeniería", "engineering" -> "fas fa-cogs";
            case "medicina", "medicine" -> "fas fa-stethoscope";
            case "derecho", "law" -> "fas fa-gavel";
            case "psicología", "psychology" -> "fas fa-brain";
            case "arquitectura", "architecture" -> "fas fa-building";
            case "contabilidad", "accounting" -> "fas fa-calculator";
            default -> "fas fa-graduation-cap";
        };
    }
    
    // Método para obtener categoría en español (si está en inglés)
    public String getCategoryInSpanish() {
        if (category == null) return "General";
        
        return switch (category.toLowerCase()) {
            case "science" -> "Ciencias";
            case "technology" -> "Tecnología";
            case "mathematics" -> "Matemáticas";
            case "humanities" -> "Humanidades";
            case "health" -> "Salud";
            case "business" -> "Negocios";
            case "art" -> "Arte";
            case "languages" -> "Lenguas";
            case "sports" -> "Deportes";
            case "music" -> "Música";
            case "engineering" -> "Ingeniería";
            case "medicine" -> "Medicina";
            case "law" -> "Derecho";
            case "psychology" -> "Psicología";
            case "architecture" -> "Arquitectura";
            case "accounting" -> "Contabilidad";
            default -> category;
        };
    }
}