package com.studybuddy.repository;

import com.studybuddy.model.DniInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DniInfoRepository extends JpaRepository<DniInfo, Long> {
    
    // Buscar por número de DNI
    Optional<DniInfo> findByDniNumber(String dniNumber);
    
    // Verificar si existe DNI
    boolean existsByDniNumber(String dniNumber);
    
    // Buscar por nombre completo
    @Query("SELECT d FROM DniInfo d WHERE " +
           "LOWER(d.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<DniInfo> searchByName(@Param("query") String query);
    
    // Métodos básicos
    List<DniInfo> findByLastName(String lastName);
    List<DniInfo> findByMotherLastName(String motherLastName);
    List<DniInfo> findByGender(String gender);
    List<DniInfo> findByDepartment(String department);
    List<DniInfo> findByProvince(String province);
    List<DniInfo> findByDistrict(String district);
    List<DniInfo> findByBirthDate(LocalDate birthDate);
    
    // Buscar mayores de edad
    @Query("SELECT d FROM DniInfo d WHERE YEAR(CURRENT_DATE) - YEAR(d.birthDate) >= 18")
    List<DniInfo> findAdults();
    
    List<DniInfo> findByIsValidTrue();
    List<DniInfo> findByIsValidFalse();
    List<DniInfo> findBySource(String source);
    
    @Query("SELECT d FROM DniInfo d WHERE d.consultedAt >= :startDate")
    List<DniInfo> findRecentConsultations(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT d FROM DniInfo d ORDER BY d.consultationCount DESC")
    List<DniInfo> findMostConsulted(Pageable pageable);
    
    // CORRECCIÓN IMPORTANTE: Usamos Native Query para la resta de fechas
    // SQL Puro para MySQL
    @Query(value = "SELECT * FROM dni_info WHERE last_verified < DATE_SUB(NOW(), INTERVAL 30 DAY) OR last_verified IS NULL", nativeQuery = true)
    List<DniInfo> findDniNeedingVerification();
    
    // Búsqueda avanzada
    @Query("SELECT d FROM DniInfo d WHERE " +
           "(:query IS NULL OR " +
           "LOWER(d.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.dniNumber) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:department IS NULL OR d.department = :department) AND " +
           "(:isValid IS NULL OR d.isValid = :isValid)")
    Page<DniInfo> searchDniInfo(@Param("query") String query,
                               @Param("department") String department,
                               @Param("isValid") Boolean isValid,
                               Pageable pageable);
    
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE DniInfo d SET d.consultationCount = d.consultationCount + 1, d.consultedAt = :consultedAt WHERE d.id = :id")
    void incrementConsultationCount(@Param("id") Long id, @Param("consultedAt") LocalDateTime consultedAt);
    
    @Query("SELECT SUM(d.consultationCount) FROM DniInfo d")
    Long getTotalConsultations();
    
    @Query("SELECT d FROM DniInfo d WHERE d.errorMessage IS NOT NULL")
    List<DniInfo> findDniWithErrors();

    Long countByIsValidFalse();
    Long countByIsValidTrue();
}