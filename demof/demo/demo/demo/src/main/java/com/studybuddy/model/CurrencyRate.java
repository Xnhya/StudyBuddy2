package com.studybuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "currency_rates",
       indexes = {
           @Index(name = "idx_currency_base", columnList = "base_currency"),
           @Index(name = "idx_currency_target", columnList = "target_currency"),
           @Index(name = "idx_currency_date", columnList = "rate_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency = "USD";
    
    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;
    
    @Column(name = "exchange_rate", nullable = false)
    private Double exchangeRate;
    
    @Column(name = "rate_date", nullable = false)
    private java.time.LocalDate rateDate;
    
    @Column(name = "last_updated")
    private java.time.LocalDateTime lastUpdated;
    
    @Column(name = "source", length = 50)
    private String source = "apilayer";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        if (rateDate == null) {
            rateDate = java.time.LocalDate.now();
        }
        if (lastUpdated == null) {
            lastUpdated = java.time.LocalDateTime.now();
        }
        if (baseCurrency == null) {
            baseCurrency = "USD";
        }
        if (isActive == null) {
            isActive = true;
        }
        if (source == null) {
            source = "apilayer";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = java.time.LocalDateTime.now();
    }
    
    // ========== MÉTODOS HELPER ==========
    
    // Convertir cantidad de base a target
    public Double convert(Double amount) {
        if (amount == null || exchangeRate == null) {
            return null;
        }
        return amount * exchangeRate;
    }
    
    // Convertir cantidad de target a base
    public Double convertReverse(Double amount) {
        if (amount == null || exchangeRate == null || exchangeRate == 0) {
            return null;
        }
        return amount / exchangeRate;
    }
    
    // Verificar si la tasa está actualizada (menos de 24 horas)
    public boolean isRateUpdatedToday() {
        if (lastUpdated == null) return false;
        
        java.time.Duration duration = java.time.Duration.between(
            lastUpdated, java.time.LocalDateTime.now()
        );
        
        return duration.toHours() < 24;
    }
    
    // Obtener nombre completo de moneda base
    public String getBaseCurrencyName() {
        return getCurrencyName(baseCurrency);
    }
    
    // Obtener nombre completo de moneda target
    public String getTargetCurrencyName() {
        return getCurrencyName(targetCurrency);
    }
    
    // Obtener símbolo de moneda base
    public String getBaseCurrencySymbol() {
        return getCurrencySymbol(baseCurrency);
    }
    
    // Obtener símbolo de moneda target
    public String getTargetCurrencySymbol() {
        return getCurrencySymbol(targetCurrency);
    }
    
    // Obtener nombre de moneda
    private String getCurrencyName(String currencyCode) {
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "Dólar Estadounidense";
            case "EUR" -> "Euro";
            case "PEN" -> "Sol Peruano";
            case "GBP" -> "Libra Esterlina";
            case "JPY" -> "Yen Japonés";
            case "CAD" -> "Dólar Canadiense";
            case "AUD" -> "Dólar Australiano";
            case "CHF" -> "Franco Suizo";
            case "CNY" -> "Yuan Chino";
            case "MXN" -> "Peso Mexicano";
            case "BRL" -> "Real Brasileño";
            case "ARS" -> "Peso Argentino";
            case "CLP" -> "Peso Chileno";
            case "COP" -> "Peso Colombiano";
            default -> currencyCode;
        };
    }
    
    // Obtener símbolo de moneda
    private String getCurrencySymbol(String currencyCode) {
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "PEN" -> "S/";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "CAD", "AUD" -> "$";
            case "CHF" -> "CHF";
            case "CNY" -> "¥";
            case "MXN", "ARS", "CLP", "COP" -> "$";
            case "BRL" -> "R$";
            default -> currencyCode;
        };
    }
    
    // Obtener bandera de moneda (emoji)
    public String getBaseCurrencyFlag() {
        return getCurrencyFlag(baseCurrency);
    }
    
    public String getTargetCurrencyFlag() {
        return getCurrencyFlag(targetCurrency);
    }
    
    private String getCurrencyFlag(String currencyCode) {
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "🇺🇸";
            case "EUR" -> "🇪🇺";
            case "PEN" -> "🇵🇪";
            case "GBP" -> "🇬🇧";
            case "JPY" -> "🇯🇵";
            case "CAD" -> "🇨🇦";
            case "AUD" -> "🇦🇺";
            case "CHF" -> "🇨🇭";
            case "CNY" -> "🇨🇳";
            case "MXN" -> "🇲🇽";
            case "BRL" -> "🇧🇷";
            case "ARS" -> "🇦🇷";
            case "CLP" -> "🇨🇱";
            case "COP" -> "🇨🇴";
            default -> "🌐";
        };
    }
    
    // Obtener tasa formateada
    public String getFormattedRate() {
        if (exchangeRate == null) return "N/A";
        return String.format("%.4f", exchangeRate);
    }
    
    // Verificar si es la tasa actual (del día de hoy)
    public boolean isCurrentRate() {
        return rateDate != null && rateDate.equals(java.time.LocalDate.now());
    }
}