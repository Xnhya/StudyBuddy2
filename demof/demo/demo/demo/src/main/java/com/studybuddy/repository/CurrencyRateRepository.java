package com.studybuddy.repository;

import com.studybuddy.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
    
    Optional<CurrencyRate> findByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);
    
    Optional<CurrencyRate> findByBaseCurrencyAndTargetCurrencyAndRateDate(
            String baseCurrency, String targetCurrency, LocalDate rateDate);
    
    List<CurrencyRate> findByBaseCurrency(String baseCurrency);
    List<CurrencyRate> findByTargetCurrency(String targetCurrency);
    
    @Query("SELECT cr FROM CurrencyRate cr WHERE " +
           "cr.baseCurrency = :baseCurrency AND " +
           "cr.targetCurrency = :targetCurrency AND " +
           "cr.rateDate = (SELECT MAX(cr2.rateDate) FROM CurrencyRate cr2 " +
           "WHERE cr2.baseCurrency = :baseCurrency AND cr2.targetCurrency = :targetCurrency)")
    Optional<CurrencyRate> findLatestRate(@Param("baseCurrency") String baseCurrency,
                                         @Param("targetCurrency") String targetCurrency);
    
    @Query("SELECT cr FROM CurrencyRate cr WHERE cr.rateDate = CURRENT_DATE")
    List<CurrencyRate> findRatesUpdatedToday();
    
    List<CurrencyRate> findByRateDate(LocalDate rateDate);
    List<CurrencyRate> findByIsActiveTrue();
    List<CurrencyRate> findBySource(String source);
    
    // CORRECCIÓN IMPORTANTE: Usamos Native Query para la resta de fechas
    @Query(value = "SELECT * FROM currency_rates WHERE last_updated < DATE_SUB(NOW(), INTERVAL 24 HOUR) OR last_updated IS NULL", nativeQuery = true)
    List<CurrencyRate> findRatesNeedingUpdate();
    
    @Query("SELECT DISTINCT cr.targetCurrency FROM CurrencyRate cr WHERE cr.baseCurrency = :baseCurrency")
    List<String> findAvailableTargetCurrencies(@Param("baseCurrency") String baseCurrency);
    
    @Query("SELECT DISTINCT cr.baseCurrency FROM CurrencyRate cr")
    List<String> findAllBaseCurrencies();
    
    boolean existsByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);
    
    Long countByIsActiveTrue();
    Long countByIsActiveFalse();
}