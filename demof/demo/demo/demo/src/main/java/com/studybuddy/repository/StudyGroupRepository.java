package com.studybuddy.repository;

import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    
    // Buscar grupos por creador
    List<StudyGroup> findByCreator(User creator);
    
    // Buscar grupos por creador con paginación
    Page<StudyGroup> findByCreator(User creator, Pageable pageable);
    
    // Buscar grupos públicos
    List<StudyGroup> findByIsPublicTrue();
    
    // Buscar grupos públicos con paginación
    Page<StudyGroup> findByIsPublicTrue(Pageable pageable);
    
    // Buscar grupos activos
    List<StudyGroup> findByIsActiveTrue();
    
    // Buscar grupos inactivos
    List<StudyGroup> findByIsActiveFalse();
    
    // Buscar grupos por materia (búsqueda parcial, case insensitive)
    List<StudyGroup> findBySubjectContainingIgnoreCase(String subject);
    
    // Buscar grupos por código de curso
    List<StudyGroup> findByCourseCode(String courseCode);
    
    // Buscar grupos por dificultad
    List<StudyGroup> findByDifficultyLevel(String difficultyLevel);
    
    // Buscar grupos por plataforma de reunión
    List<StudyGroup> findByMeetingPlatform(String meetingPlatform);
    
    // Buscar grupos que tienen un miembro específico
    @Query("SELECT g FROM StudyGroup g JOIN g.members m WHERE m.id = :userId")
    List<StudyGroup> findByMemberId(@Param("userId") Long userId);
    
    // Buscar grupos donde el usuario no es miembro (para unirse)
    @Query("SELECT g FROM StudyGroup g WHERE g.id NOT IN " +
           "(SELECT g2.id FROM StudyGroup g2 JOIN g2.members m WHERE m.id = :userId) " +
           "AND g.isPublic = true AND g.isActive = true")
    List<StudyGroup> findAvailableGroupsForUser(@Param("userId") Long userId);
    
    // Buscar grupos con cupos disponibles
    @Query("SELECT g FROM StudyGroup g WHERE g.currentMembers < g.maxMembers")
    List<StudyGroup> findGroupsWithAvailableSpots();
    
    // Buscar grupos llenos
    @Query("SELECT g FROM StudyGroup g WHERE g.currentMembers >= g.maxMembers")
    List<StudyGroup> findFullGroups();
    
    // Buscar grupos por tags (búsqueda parcial)
    @Query("SELECT g FROM StudyGroup g WHERE g.tags LIKE CONCAT('%', :tag, '%')")
    List<StudyGroup> findByTag(@Param("tag") String tag);
    
    // Buscar grupos por múltiples tags
    @Query("SELECT g FROM StudyGroup g WHERE " +
           "(:tags IS NULL OR g.tags LIKE CONCAT('%', :tags, '%'))")
    List<StudyGroup> findByTags(@Param("tags") String tags);
    
    // Buscar grupos que inician después de una fecha
    List<StudyGroup> findByMeetingTimeAfter(LocalDateTime dateTime);
    
    // Buscar próximos grupos (próximas 24 horas)
    @Query("SELECT g FROM StudyGroup g WHERE " +
           "g.meetingTime BETWEEN :startTime AND :endTime " +
           "AND g.isActive = true")
    List<StudyGroup> findUpcomingGroups(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
    
    // Buscar grupos por materia y dificultad
    List<StudyGroup> findBySubjectAndDifficultyLevel(String subject, String difficultyLevel);
    
    // Búsqueda avanzada de grupos
    @Query("SELECT g FROM StudyGroup g WHERE " +
           "(:subject IS NULL OR LOWER(g.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) AND " +
           "(:difficulty IS NULL OR g.difficultyLevel = :difficulty) AND " +
           "(:isPublic IS NULL OR g.isPublic = :isPublic) AND " +
           "(:hasSpots IS NULL OR (:hasSpots = true AND g.currentMembers < g.maxMembers) OR " +
           "(:hasSpots = false AND g.currentMembers >= g.maxMembers)) AND " +
           "g.isActive = true")
    Page<StudyGroup> searchGroups(@Param("subject") String subject,
                                 @Param("difficulty") String difficulty,
                                 @Param("isPublic") Boolean isPublic,
                                 @Param("hasSpots") Boolean hasSpots,
                                 Pageable pageable);
    
    // Contar grupos por creador
    Long countByCreator(User creator);
    
    // Contar grupos activos
    Long countByIsActiveTrue();
    
    // Contar grupos públicos
    Long countByIsPublicTrue();
    
    // Estadísticas de grupos por materia
    @Query("SELECT g.subject, COUNT(g) as groupCount, " +
           "AVG(g.currentMembers) as avgMembers, " +
           "MAX(g.maxMembers) as maxCapacity " +
           "FROM StudyGroup g " +
           "WHERE g.isActive = true " +
           "GROUP BY g.subject " +
           "ORDER BY groupCount DESC")
    List<Object[]> getGroupStatisticsBySubject();
    
    // Buscar grupos populares (más miembros)
    @Query("SELECT g FROM StudyGroup g " +
           "WHERE g.isActive = true " +
           "ORDER BY g.currentMembers DESC, g.viewsCount DESC")
    List<StudyGroup> findPopularGroups(Pageable pageable);
    
    // Buscar grupos recientes
    List<StudyGroup> findByCreatedAtAfter(LocalDateTime date);
    
    // Buscar grupos por ubicación
    @Query("SELECT g FROM StudyGroup g WHERE " +
           "LOWER(g.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<StudyGroup> findByLocation(@Param("location") String location);
    
    // Buscar grupos por horario
    @Query("SELECT g FROM StudyGroup g WHERE " +
           "LOWER(g.schedule) LIKE LOWER(CONCAT('%', :schedule, '%'))")
    List<StudyGroup> findBySchedule(@Param("schedule") String schedule);
    
    // Incrementar contador de vistas
    @Query("UPDATE StudyGroup g SET g.viewsCount = g.viewsCount + 1 WHERE g.id = :groupId")
    void incrementViews(@Param("groupId") Long groupId);
    
    // Buscar grupos similares (para recomendaciones)
    @Query("SELECT g FROM StudyGroup g WHERE " +
           "g.subject = (SELECT g2.subject FROM StudyGroup g2 WHERE g2.id = :groupId) AND " +
           "g.id != :groupId AND " +
           "g.isActive = true")
    List<StudyGroup> findSimilarGroups(@Param("groupId") Long groupId, Pageable pageable);
}