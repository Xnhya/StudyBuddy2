package com.studybuddy.service;

import com.studybuddy.model.CurrencyRate;
import com.studybuddy.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CurrencyService {
    
    @Autowired
    private CurrencyRateRepository currencyRateRepository;
    
    @Autowired
    private ExternalApiService externalApiService;
    
    // CONVERTIR MONEDA
    public Double convertCurrency(String from, String to, Double amount) {
        // 1. Intentar buscar tasa en base de datos local primero
        Optional<CurrencyRate> rateOpt = currencyRateRepository.findLatestRate(from, to);
        
        if (rateOpt.isPresent()) {
            CurrencyRate rate = rateOpt.get();
            // Verificar si la tasa es reciente (hoy)
            if (rate.isCurrentRate()) {
                return rate.convert(amount);
            }
        }
        
        // 2. Si no existe o es antigua, usar API externa (o simulación)
        try {
            Map<String, Object> result = externalApiService
                    .getExchangeRate(from, to, amount)
                    .block(); // Llamada bloqueante segura aquí
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                // Intentar obtener el resultado numérico de forma segura
                Object res = result.get("result");
                if (res instanceof Number) {
                    return ((Number) res).doubleValue();
                }
            }
        } catch (Exception e) {
            // Log del error silencioso
            System.err.println("Error en API externa, usando simulación: " + e.getMessage());
        }
        
        // 3. Fallback a simulación directa si todo falla
        Map<String, Object> simulated = externalApiService.simulateExchangeRate(from, to, amount);
        Object result = simulated.get("result");
        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        }
        // Si todo falla, retornar el monto original
        return amount;
    }
    
    // GUARDAR TASA DE CAMBIO
    public CurrencyRate saveRate(CurrencyRate rate) {
        rate.setLastUpdated(LocalDateTime.now());
        return currencyRateRepository.save(rate);
    }
    
    // OBTENER TODAS LAS TASAS
    public List<CurrencyRate> getAllRates() {
        return currencyRateRepository.findAll();
    }
    
    // ACTUALIZAR TASAS DESDE API (Llamada Batch)
    public void updateRatesFromApi() {
        if (!externalApiService.isCurrencyApiConfigured()) {
            return;
        }
        
        try {
            Map<String, Object> response = externalApiService
                    .getLatestRates("USD")
                    .block();
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                @SuppressWarnings("unchecked")
                Map<String, Number> rates = (Map<String, Number>) response.get("rates");
                
                if (rates != null) {
                    for (Map.Entry<String, Number> entry : rates.entrySet()) {
                        CurrencyRate rate = new CurrencyRate();
                        rate.setBaseCurrency("USD");
                        rate.setTargetCurrency(entry.getKey());
                        rate.setExchangeRate(entry.getValue().doubleValue());
                        rate.setRateDate(LocalDate.now());
                        rate.setSource("apilayer");
                        
                        currencyRateRepository.save(rate);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error actualizando tasas masivas: " + e.getMessage());
        }
    }
    
    public List<String> getAvailableCurrencies() {
        return List.of("USD", "PEN", "EUR", "GBP", "JPY");
    }
}