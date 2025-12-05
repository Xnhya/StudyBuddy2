package com.studybuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "study_groups",
       indexes = {
           @Index(name = "idx_group_subject", columnList = "subject"),
           @Index(name = "idx_group_course_code", columnList = "course_code"),
           @Index(name = "idx_group_creator", columnList = "creator_id"),
           @Index(name = "idx_group_created", columnList = "created_at"),
           @Index(name = "idx_group_public", columnList = "is_public")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(length = 500)
    private String description;
    
    @NotBlank(message = "La materia es obligatoria")
    @Size(max = 100, message = "La materia no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String subject;
    
    @Size(max = 20, message = "El código del curso no puede exceder 20 caracteres")
    @Column(name = "course_code", length = 20)
    private String courseCode;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false, foreignKey = @ForeignKey(name = "fk_group_creator"))
    @NotNull(message = "El creador es obligatorio")
    private User creator;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = {
            @Index(name = "idx_group_members_group", columnList = "group_id"),
            @Index(name = "idx_group_members_user", columnList = "user_id")
        })
    private Set<User> members = new HashSet<>();
    
    @Column(name = "max_members")
    @NotNull(message = "El número máximo de miembros es obligatorio")
    private Integer maxMembers = 10;
    
    @Column(name = "current_members")
    private Integer currentMembers = 1;
    
    @Column(name = "is_public")
    @NotNull(message = "Debe especificar si el grupo es público")
    private Boolean isPublic = true;
    
    @Column(name = "meeting_time")
    private LocalDateTime meetingTime;
    
    @Column(name = "meeting_link", length = 255)
    private String meetingLink;
    
    @Column(name = "meeting_platform", length = 50)
    private String meetingPlatform = "Zoom";
    
    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel = "Intermedio";
    
    @Column(name = "tags", length = 200)
    private String tags;
    
    @Column(name = "location", length = 200)
    private String location;
    
    @Column(name = "schedule", length = 100)
    private String schedule;
    
    @Column(name = "requirements", length = 500)
    private String requirements;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_chat_enabled")
    private Boolean isChatEnabled = false;
    
    @Column(name = "views_count")
    private Integer viewsCount = 0;
    
    @Column(name = "rating_average")
    private Double ratingAverage = 0.0;
    
    @Column(name = "rating_count")
    private Integer ratingCount = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (members == null) {
            members = new HashSet<>();
        }
        if (creator != null && !members.contains(creator)) {
            members.add(creator);
            currentMembers = 1;
        }
        if (maxMembers == null) {
            maxMembers = 10;
        }
        if (isPublic == null) {
            isPublic = true;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (meetingPlatform == null) {
            meetingPlatform = "Zoom";
        }
        if (difficultyLevel == null) {
            difficultyLevel = "Intermedio";
        }
        if (viewsCount == null) {
            viewsCount = 0;
        }
        if (ratingAverage == null) {
            ratingAverage = 0.0;
        }
        if (ratingCount == null) {
            ratingCount = 0;
        }
        if (isChatEnabled == null) {
            isChatEnabled = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (members != null) {
            currentMembers = members.size();
        }
    }
    
    // ========== MÉTODOS HELPER ==========
    
    // Agregar miembro al grupo
    public boolean addMember(User user) {
        if (members.size() < maxMembers && !members.contains(user)) {
            members.add(user);
            currentMembers = members.size();
            return true;
        }
        return false;
    }
    
    // Remover miembro del grupo
    public boolean removeMember(User user) {
        if (members.contains(user) && !user.equals(creator)) {
            members.remove(user);
            currentMembers = members.size();
            return true;
        }
        return false;
    }
    
    // Verificar si hay cupos disponibles
    public boolean hasAvailableSpots() {
        return currentMembers < maxMembers;
    }
    
    // Verificar si un usuario es miembro
    public boolean isMember(User user) {
        return members.contains(user);
    }
    
    // Verificar si un usuario es el creador
    public boolean isCreator(User user) {
        return creator != null && creator.equals(user);
    }
    
    // Obtener tags como lista
    public String[] getTagsList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }
    
    // Agregar un tag
    public void addTag(String tag) {
        if (tags == null || tags.isEmpty()) {
            tags = tag;
        } else {
            tags += "," + tag;
        }
    }
    
    // Obtener porcentaje de ocupación
    public int getOccupancyPercentage() {
        if (maxMembers == null || maxMembers == 0) {
            return 0;
        }
        if (currentMembers == null) {
            return 0;
        }
        return (currentMembers * 100) / maxMembers;
    }
    
    // Incrementar contador de vistas
    public void incrementViews() {
        if (viewsCount == null) {
            viewsCount = 0;
        }
        viewsCount++;
    }
    
    // Agregar una calificación
    public void addRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            return;
        }
        
        if (ratingCount == null) {
            ratingCount = 0;
        }
        if (ratingAverage == null) {
            ratingAverage = 0.0;
        }
        
        double total = ratingAverage * ratingCount + rating;
        ratingCount++;
        ratingAverage = total / ratingCount;
    }
    
    // Obtener próxima reunión (si existe)
    public LocalDateTime getNextMeeting() {
        if (meetingTime != null && meetingTime.isAfter(LocalDateTime.now())) {
            return meetingTime;
        }
        return null;
    }
    
    // Verificar si el grupo está lleno
    public boolean isFull() {
        return !hasAvailableSpots();
    }
    
    // Obtener información de disponibilidad
    public String getAvailabilityInfo() {
        if (isFull()) {
            return "Completo";
        } else {
            return currentMembers + "/" + maxMembers + " miembros";
        }
    }
    
    // Obtener nivel de dificultad con color
    public String getDifficultyLevelWithColor() {
        if (difficultyLevel == null) {
            return "<span class='text-secondary'>No definido</span>";
        }
        
        return switch (difficultyLevel.toLowerCase()) {
            case "básico", "beginner" -> "<span class='text-success'>Básico</span>";
            case "intermedio", "intermediate" -> "<span class='text-warning'>Intermedio</span>";
            case "avanzado", "advanced" -> "<span class='text-danger'>Avanzado</span>";
            default -> "<span class='text-info'>" + difficultyLevel + "</span>";
        };
    }
}