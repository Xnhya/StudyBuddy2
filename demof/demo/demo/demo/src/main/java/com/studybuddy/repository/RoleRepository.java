package com.studybuddy.repository;

import com.studybuddy.model.Role;
import com.studybuddy.model.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Buscar rol por nombre del enum
    Optional<Role> findByName(ERole name);
    
    // Buscar rol por nombre del enum (string)
    @Query("SELECT r FROM Role r WHERE r.name = :roleName")
    Optional<Role> findByName(@Param("roleName") String roleName);
    
    // Verificar si existe por nombre
    boolean existsByName(ERole name);
    
    // Buscar múltiples roles por nombres
    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    List<Role> findByNames(@Param("names") Set<ERole> names);
    
    // Buscar roles por descripción (búsqueda parcial)
    @Query("SELECT r FROM Role r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Role> findByDescriptionContaining(@Param("description") String description);
    
    // CORRECCIÓN: Consulta invertida. Contamos desde User en lugar de desde Role.
    // Antes fallaba porque Role no tenía "r.users".
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersPerRole();
    
    // Buscar roles activos
    @Query("SELECT r FROM Role r WHERE r.name IS NOT NULL")
    List<Role> findAllActive();
    
    // Buscar roles ordenados por nombre
    List<Role> findAllByOrderByName();
    
    // Verificar si un usuario tiene un rol específico
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM User u JOIN u.roles r WHERE u.id = :userId AND r.name = :roleName")
    boolean userHasRole(@Param("userId") Long userId, @Param("roleName") ERole roleName);
    
    // Obtener roles asignados a un usuario
    @Query("SELECT r FROM User u JOIN u.roles r WHERE u.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
    
    // Buscar roles disponibles para asignar (que el usuario no tiene)
    @Query("SELECT r FROM Role r WHERE r NOT IN " +
           "(SELECT r2 FROM User u JOIN u.roles r2 WHERE u.id = :userId)")
    List<Role> findAvailableRolesForUser(@Param("userId") Long userId);
}