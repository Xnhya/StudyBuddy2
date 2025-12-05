package com.studybuddy.repository;

import com.studybuddy.model.Message;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Buscar mensajes por grupo (ordenados por fecha de envío)
    List<Message> findByStudyGroupOrderBySentAtDesc(StudyGroup studyGroup);
    
    // Buscar mensajes por grupo con paginación
    Page<Message> findByStudyGroupOrderBySentAtDesc(StudyGroup studyGroup, Pageable pageable);
    
    // Buscar mensajes por remitente
    List<Message> findBySender(User sender);
    
    // Buscar mensajes por grupo y tipo
    List<Message> findByStudyGroupAndMessageTypeOrderBySentAtDesc(StudyGroup studyGroup, String messageType);
    
    // Buscar mensajes no leídos por usuario en un grupo
    @Query("SELECT m FROM Message m WHERE " +
           "m.studyGroup = :group AND " +
           "m.sender != :user AND " +
           "m.isRead = false AND " +
           "m.isDeleted = false")
    List<Message> findUnreadMessages(@Param("group") StudyGroup group, @Param("user") User user);
    
    // Contar mensajes no leídos por usuario en un grupo
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "m.studyGroup = :group AND " +
           "m.sender != :user AND " +
           "m.isRead = false AND " +
           "m.isDeleted = false")
    Long countUnreadMessages(@Param("group") StudyGroup group, @Param("user") User user);
    
    // Buscar mensajes después de una fecha específica
    List<Message> findByStudyGroupAndSentAtAfter(StudyGroup studyGroup, LocalDateTime dateTime);
    
    // Buscar mensajes entre fechas
    @Query("SELECT m FROM Message m WHERE " +
           "m.studyGroup = :group AND " +
           "m.sentAt BETWEEN :startDate AND :endDate AND " +
           "m.isDeleted = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> findMessagesBetweenDates(@Param("group") StudyGroup group,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    // Contar mensajes por grupo
    Long countByStudyGroup(StudyGroup studyGroup);
    
    // Contar mensajes por remitente
    Long countBySender(User sender);
    
    // Buscar últimos N mensajes de un grupo
    @Query("SELECT m FROM Message m WHERE " +
           "m.studyGroup = :group AND " +
           "m.isDeleted = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> findLatestMessages(@Param("group") StudyGroup group, Pageable pageable);
    
    // Buscar mensajes que contengan texto específico
    @Query("SELECT m FROM Message m WHERE " +
           "m.studyGroup = :group AND " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :searchText, '%')) AND " +
           "m.isDeleted = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> searchMessages(@Param("group") StudyGroup group,
                                @Param("searchText") String searchText);
    
    // Marcar mensajes como leídos
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readAt " +
           "WHERE m.studyGroup = :group AND m.sender != :user AND m.isRead = false")
    void markMessagesAsRead(@Param("group") StudyGroup group,
                           @Param("user") User user,
                           @Param("readAt") LocalDateTime readAt);
    
    // Buscar mensajes con archivos adjuntos
    @Query("SELECT m FROM Message m WHERE " +
           "m.attachmentUrl IS NOT NULL AND " +
           "m.studyGroup = :group AND " +
           "m.isDeleted = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> findMessagesWithAttachments(@Param("group") StudyGroup group);
    
    // Buscar mensajes del sistema
    @Query("SELECT m FROM Message m WHERE " +
           "m.messageType IN ('SYSTEM', 'JOIN', 'LEAVE') AND " +
           "m.studyGroup = :group AND " +
           "m.isDeleted = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> findSystemMessages(@Param("group") StudyGroup group);
    
    // Buscar mensajes editados
    List<Message> findByIsEditedTrue();
    
    // Buscar mensajes eliminados (soft delete)
    List<Message> findByIsDeletedTrue();
    
    // Buscar mensajes por tipo
    List<Message> findByMessageType(String messageType);
    
    // Estadísticas de mensajes por usuario en un grupo
    @Query("SELECT m.sender.id, COUNT(m) as messageCount, " +
           "MAX(m.sentAt) as lastMessageTime " +
           "FROM Message m " +
           "WHERE m.studyGroup = :group AND " +
           "m.isDeleted = false " +
           "GROUP BY m.sender.id " +
           "ORDER BY messageCount DESC")
    List<Object[]> getMessageStatisticsByUser(@Param("group") StudyGroup group);
    
    // Buscar mensajes recientes de todos los grupos del usuario
    @Query("SELECT m FROM Message m WHERE " +
           "m.studyGroup IN (SELECT g FROM StudyGroup g JOIN g.members mem WHERE mem.id = :userId) AND " +
           "m.isDeleted = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> findRecentMessagesForUser(@Param("userId") Long userId, Pageable pageable);
    
    // Buscar mensajes a los que se ha respondido
    @Query("SELECT m FROM Message m WHERE " +
           "m.repliedToId IS NOT NULL AND " +
           "m.studyGroup = :group AND " +
           "m.isDeleted = false")
    List<Message> findRepliedMessages(@Param("group") StudyGroup group);
    
    // Soft delete de mensajes antiguos (para limpieza)
    @Modifying
    @Query("UPDATE Message m SET m.isDeleted = true, m.deletedAt = :deletedAt " +
           "WHERE m.sentAt < :cutoffDate AND m.isDeleted = false")
    void deleteOldMessages(@Param("cutoffDate") LocalDateTime cutoffDate,
                          @Param("deletedAt") LocalDateTime deletedAt);
}