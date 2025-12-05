package com.studybuddy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class ExternalApiService {

    private final WebClient currencyWebClient;
    private final WebClient dniWebClient;

    @Value("${app.external-api.currency.apikey:demo}")
    private String currencyApiKey;

    @Value("${app.external-api.dni.apikey:demo}")
    private String dniApiKey;

    // Agregamos @Qualifier para asegurar que Spring sepa cuál WebClient usar
    public ExternalApiService(@Qualifier("currencyWebClient") WebClient currencyWebClient, 
                              @Qualifier("dniWebClient") WebClient dniWebClient) {
        this.currencyWebClient = currencyWebClient;
        this.dniWebClient = dniWebClient;
    }

    // CONSULTAR TASA DE CAMBIO
    public Mono<Map<String, Object>> getExchangeRate(String from, String to, Double amount) {
        if (from == null || to == null || amount == null || amount <= 0) {
            return Mono.just(Map.of("error", "Parámetros inválidos", "success", false));
        }

        String url = "/convert?from=" + from + "&to=" + to + "&amount=" + amount;
        
        if (!isCurrencyApiConfigured()) {
            log.warn("API de moneda no configurada, usando simulación");
            return Mono.just(simulateExchangeRate(from, to, amount));
        }

        return currencyWebClient.get()
                .uri(url)
                .header("apikey", currencyApiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(response -> log.info("Tasa obtenida: {} a {}", from, to))
                .onErrorResume(ex -> Mono.just(Map.of("error", ex.getMessage(), "success", false)));
    }

    // CONSULTAR DNI
    public Mono<Map<String, Object>> getDniInfo(String dni) {
        if (dni == null || dni.length() != 8) {
            return Mono.just(Map.of("error", "DNI inválido", "success", false));
        }

        if (!isDniApiConfigured()) {
            log.warn("API de DNI no configurada, usando simulación");
            return Mono.just(simulateDniInfo(dni));
        }

        return dniWebClient.get()
                .uri("/" + dni)
                .header("Authorization", "Bearer " + dniApiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(ex -> Mono.just(Map.of("error", ex.getMessage(), "success", false)));
    }

    // OBTENER TASAS DE CAMBIO MÚLTIPLES
    public Mono<Map<String, Object>> getLatestRates(String base) {
        if (base == null || base.trim().isEmpty()) {
            return Mono.just(Map.of("error", "Moneda base inválida", "success", false));
        }
        
        // Si no está configurada, retornamos éxito vacío para no romper CurrencyService
        if (!isCurrencyApiConfigured()) {
             return Mono.just(Map.of("success", true, "base", base, "rates", Map.of()));
        }

        String url = "/latest?base=" + base;

        return currencyWebClient.get()
                .uri(url)
                .header("apikey", currencyApiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(ex -> Mono.just(Map.of("error", ex.getMessage(), "success", false)));
    }

    public boolean isCurrencyApiConfigured() {
        return currencyApiKey != null && !currencyApiKey.equals("demo");
    }

    public boolean isDniApiConfigured() {
        return dniApiKey != null && !dniApiKey.equals("demo");
    }

    // Métodos de simulación (Públicos)
    public Map<String, Object> simulateDniInfo(String dni) {
        return Map.of(
                "success", true,
                "data", Map.of(
                        "dni", dni,
                        "nombres", "JUAN CARLOS (DEMO)",
                        "apellido_paterno", "PEREZ",
                        "apellido_materno", "GARCIA",
                        "sexo", "MASCULINO"));
    }

    public Map<String, Object> simulateExchangeRate(String from, String to, Double amount) {
        double rate = 0.0;
        if (from.equals("USD") && to.equals("PEN")) rate = 3.75;
        else if (from.equals("PEN") && to.equals("USD")) rate = 0.27;
        else if (from.equals("EUR") && to.equals("PEN")) rate = 4.07;
        else rate = 1.0; 

        double result = amount * rate;

        return Map.of(
                "success", true,
                "query", Map.of("from", from, "to", to, "amount", amount),
                "result", result,
                "rate", rate);
    }
}