package com.studybuddy.repository;

import com.studybuddy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Buscar por username
    Optional<User> findByUsername(String username);
    
    // Buscar por email
    Optional<User> findByEmail(String email);
    
    // Buscar por username o email
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    // Verificar existencia
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Buscar usuarios por carrera
    List<User> findByCareer(String career);
    
    // Buscar usuarios por universidad
    List<User> findByUniversity(String university);
    
    // Buscar usuarios por carrera y semestre
    List<User> findByCareerAndSemester(String career, Integer semester);
    
    // CORREGIDO: Cambiado 'IsActive' por 'Enabled'
    List<User> findByEnabledTrue();
    
    // CORREGIDO: Cambiado 'IsActive' por 'Enabled'
    List<User> findByEnabledFalse();
    
    // Buscar usuarios con email verificado
    List<User> findByEmailVerifiedTrue();
    
    // Buscar usuarios por rol
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    // Buscar usuarios por rol con paginación
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
    
    // Buscar usuarios por intereses
    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i WHERE i.name LIKE CONCAT('%', :interestName, '%')")
    List<User> findByInterestName(@Param("interestName") String interestName);
    
    // Buscar usuarios por categoría de interés
    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i WHERE i.category = :category")
    List<User> findByInterestCategory(@Param("category") String category);
    
    // Búsqueda avanzada (Matching) - CORREGIDO u.isActive -> u.enabled
    @Query("SELECT u FROM User u WHERE " +
           "(:career IS NULL OR u.career = :career) AND " +
           "(:university IS NULL OR u.university = :university) AND " +
           "(:semester IS NULL OR u.semester = :semester) AND " +
           "u.enabled = true")
    List<User> searchUsers(@Param("career") String career, 
                           @Param("university") String university,
                           @Param("semester") Integer semester);
    
    // Contar usuarios por rol
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);
    
    // CORREGIDO: Count por Enabled
    Long countByEnabledTrue();
    
    // Buscar usuarios creados después de una fecha
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    // Buscar usuarios por último login
    List<User> findByLastLoginAfter(LocalDateTime date);
    
    // Buscar usuarios inactivos
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date OR u.lastLogin IS NULL")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);
    
    // Buscar por nombre o username
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByNameOrUsername(@Param("query") String query);
    
    // Búsqueda completa con paginación - CORREGIDO u.isActive -> u.enabled
    @Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.career) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.university) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> searchUsersWithPagination(@Param("query") String query, 
                                        @Param("enabled") Boolean enabled, 
                                        Pageable pageable);
    
    // CORREGIDO: u.isActive -> u.enabled
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN u.interests ui " +
           "WHERE u.id != :currentUserId AND u.enabled = true AND (" +
           "u.career = (SELECT u2.career FROM User u2 WHERE u2.id = :currentUserId) OR " +
           "u.university = (SELECT u2.university FROM User u2 WHERE u2.id = :currentUserId) OR " +
           "ui.id IN (SELECT ui2.id FROM User u2 JOIN u2.interests ui2 WHERE u2.id = :currentUserId))")
    List<User> findCompatibleUsers(@Param("currentUserId") Long currentUserId);
    
    // Encontrar usuarios por género
    List<User> findByGender(String gender);
    
    // Buscar usuarios por rango de edad
    @Query("SELECT u FROM User u WHERE " +
           "(:minAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(u.birthDate) >= :minAge) AND " +
           "(:maxAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(u.birthDate) <= :maxAge)")
    List<User> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
    
    // Método auxiliar necesario para UserServiceImpl
    long countByEnabled(boolean enabled);
}