package com.studybuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages",
       indexes = {
           @Index(name = "idx_message_group", columnList = "group_id"),
           @Index(name = "idx_message_sender", columnList = "sender_id"),
           @Index(name = "idx_message_sent_at", columnList = "sent_at"),
           @Index(name = "idx_message_read", columnList = "is_read")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Column(nullable = false, length = 2000)
    private String content;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_sender"))
    @NotNull(message = "El remitente es obligatorio")
    private User sender;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_group"))
    @NotNull(message = "El grupo es obligatorio")
    private StudyGroup studyGroup;
    
    @Column(name = "sent_at")
    private java.time.LocalDateTime sentAt;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private java.time.LocalDateTime readAt;
    
    @Column(name = "message_type", length = 20)
    private String messageType = "TEXT";
    
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
    
    @Column(name = "attachment_type", length = 50)
    private String attachmentType;
    
    @Column(name = "attachment_name", length = 255)
    private String attachmentName;
    
    @Column(name = "is_edited")
    private Boolean isEdited = false;
    
    @Column(name = "edited_at")
    private java.time.LocalDateTime editedAt;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
    
    @Column(name = "replied_to_id")
    private Long repliedToId;
    
    @PrePersist
    protected void onCreate() {
        sentAt = java.time.LocalDateTime.now();
        if (isRead == null) isRead = false;
        if (messageType == null) messageType = "TEXT";
        if (isEdited == null) isEdited = false;
        if (isDeleted == null) isDeleted = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (isRead && readAt == null) {
            readAt = java.time.LocalDateTime.now();
        }
    }
    
    // ========== MÉTODOS HELPER ==========
    
    // Marcar como leído
    public void markAsRead() {
        this.isRead = true;
        this.readAt = java.time.LocalDateTime.now();
    }
    
    // Marcar como editado
    public void markAsEdited() {
        this.isEdited = true;
        this.editedAt = java.time.LocalDateTime.now();
    }
    
    // Marcar como eliminado (soft delete)
    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = java.time.LocalDateTime.now();
    }
    
    // Verificar si tiene adjunto
    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.isEmpty();
    }
    
    // Obtener tipo de mensaje legible
    public String getMessageTypeDisplay() {
        return switch (messageType) {
            case "TEXT" -> "Texto";
            case "IMAGE" -> "Imagen";
            case "FILE" -> "Archivo";
            case "SYSTEM" -> "Sistema";
            case "JOIN" -> "Unión";
            case "LEAVE" -> "Salida";
            default -> messageType;
        };
    }
    
    // Obtener icono según tipo de mensaje
    public String getMessageTypeIcon() {
        return switch (messageType) {
            case "TEXT" -> "fas fa-comment";
            case "IMAGE" -> "fas fa-image";
            case "FILE" -> "fas fa-paperclip";
            case "SYSTEM" -> "fas fa-info-circle";
            case "JOIN" -> "fas fa-user-plus";
            case "LEAVE" -> "fas fa-user-minus";
            default -> "fas fa-comment";
        };
    }
    
    // Obtener contenido truncado para vista previa
    public String getPreviewContent(int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
    
    // Verificar si es un mensaje del sistema
    public boolean isSystemMessage() {
        return "SYSTEM".equals(messageType) || 
               "JOIN".equals(messageType) || 
               "LEAVE".equals(messageType);
    }
    
    // Obtener tiempo transcurrido desde envío
    public String getTimeAgo() {
        if (sentAt == null) return "Recién";
        
        java.time.Duration duration = java.time.Duration.between(sentAt, java.time.LocalDateTime.now());
        
        if (duration.toMinutes() < 1) {
            return "Recién";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + " min";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + " h";
        } else if (duration.toDays() < 30) {
            return duration.toDays() + " d";
        } else {
            return sentAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }
}