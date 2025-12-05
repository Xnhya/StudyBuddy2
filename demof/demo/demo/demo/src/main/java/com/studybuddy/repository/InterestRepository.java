package com.studybuddy.repository;

import com.studybuddy.model.Interest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    
    Optional<Interest> findByName(String name);
    
    List<Interest> findByNameContainingIgnoreCase(String name);
    
    List<Interest> findByCategory(String category);
    
    @Query("SELECT i FROM Interest i WHERE LOWER(i.category) = LOWER(:category)")
    List<Interest> findByCategoryIgnoreCase(@Param("category") String category);
    
    List<Interest> findByIsActiveTrue();
    
    List<Interest> findByIsActiveFalse();
    
    @Query("SELECT DISTINCT i.category FROM Interest i ORDER BY i.category")
    List<String> findAllDistinctCategories();
    
    @Query("SELECT i.category, COUNT(i) FROM Interest i GROUP BY i.category ORDER BY i.category")
    List<Object[]> findCategoriesWithCount();
    
    @Query("SELECT i FROM Interest i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Interest> searchByNameOrDescriptionOrCategory(@Param("query") String query);
    
    @Query("SELECT i FROM Interest i WHERE " +
           "(:query IS NULL OR " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:isActive IS NULL OR i.isActive = :isActive)")
    Page<Interest> searchInterests(@Param("query") String query,
                                  @Param("category") String category,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);
    
    @Query("SELECT COUNT(i) FROM Interest i WHERE i.category = :category")
    Long countByCategory(@Param("category") String category);
    
    // --- CORRECCIÓN AQUÍ ---
    // Invertimos la consulta: Buscamos Usuarios y sus Intereses, en lugar de Intereses y sus "users" (que no existen)
    @Query("SELECT i, COUNT(u) as userCount FROM User u JOIN u.interests i " +
           "GROUP BY i " +
           "ORDER BY userCount DESC")
    List<Object[]> findPopularInterests(Pageable pageable);
    // -----------------------
    
    @Query("SELECT i FROM User u JOIN u.interests i WHERE u.id = :userId")
    List<Interest> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT i FROM Interest i WHERE i.id NOT IN " +
           "(SELECT i2.id FROM User u JOIN u.interests i2 WHERE u.id = :userId) " +
           "AND i.isActive = true")
    List<Interest> findAvailableInterestsForUser(@Param("userId") Long userId);
    
    List<Interest> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    List<Interest> findByIcon(String icon);
    
    @Query("SELECT i FROM Interest i WHERE i.category IN :categories")
    List<Interest> findByCategories(@Param("categories") List<String> categories);
    
    @Query("SELECT i FROM Interest i WHERE " +
           "i.category IN (SELECT DISTINCT i2.category FROM User u JOIN u.interests i2 WHERE u.id = :userId) " +
           "AND i.id NOT IN (SELECT i3.id FROM User u2 JOIN u2.interests i3 WHERE u2.id = :userId) " +
           "AND i.isActive = true " +
           "ORDER BY i.createdAt DESC")
    List<Interest> recommendInterests(@Param("userId") Long userId, Pageable pageable);
}