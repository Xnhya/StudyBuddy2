package com.studybuddy.service;

import com.studybuddy.dto.DniDTO;
import com.studybuddy.exception.ApiException;
import com.studybuddy.model.DniInfo;
import com.studybuddy.repository.DniInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class DniService {
    
    private static final Logger logger = LoggerFactory.getLogger(DniService.class);
    
    
    @Value("${app.external-api.dni.url}")
    private String apiUrl;
    
    
    @Value("${app.external-api.dni.apikey}")
    private String apiToken;
    
    private final DniInfoRepository dniInfoRepository;
    private WebClient webClient;
    
    public DniService(DniInfoRepository dniInfoRepository) {
        this.dniInfoRepository = dniInfoRepository;
    }
    
    @PostConstruct
    public void init() {
        // Validación básica
        String finalToken = (apiToken == null || apiToken.trim().isEmpty()) ? "" : apiToken;
        
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // Usamos Bearer Token estándar
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + finalToken)
                .build();
                
        logger.info("DniService iniciado con URL: {}", apiUrl);
    }
    
    public Mono<DniDTO> consultarDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            return Mono.just(new DniDTO(dni, "DNI inválido"));
        }
        
        return webClient.get()
                .uri("/dni/{dni}", dni) // Se añade a la URL base definida en properties
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    Mono.error(new ApiException("Error API: " + response.statusCode()))
                )
                .bodyToMono(Map.class)
                .map(this::mapToDniDTO)
                .doOnSuccess(dto -> {
                    if(dto.isSuccess()) saveToDatabase(dni, dto);
                })
                .onErrorResume(e -> {
                    logger.error("Error consultando API externa: {}", e.getMessage());
                    // AQUÍ ESTABA EL PROBLEMA: Si falla, devolvía datos fake.
                    // Ahora devolvemos el error real para que sepas qué pasa.
                    return Mono.just(new DniDTO(dni, "Error al consultar RENIEC: " + e.getMessage()));
                });
    }

    @SuppressWarnings("unchecked")
    private DniDTO mapToDniDTO(Map<String, Object> response) {
        
        boolean success = Boolean.TRUE.equals(response.get("success"));
        if (!success) return new DniDTO("00000000", "No encontrado");

        
        Map<String, Object> data = response.containsKey("data") ? 
            (Map<String, Object>) response.get("data") : response;

        return new DniDTO(
            (String) data.getOrDefault("numero", data.get("dni")),
            (String) data.get("nombres"),
            (String) data.getOrDefault("apellido_paterno", data.get("apellidoPaterno")),
            (String) data.getOrDefault("apellido_materno", data.get("apellidoMaterno"))
        );
    }

    private void saveToDatabase(String dni, DniDTO dto) {
      
        try {
            DniInfo info = new DniInfo();
            info.setDniNumber(dni);
            info.setFullName(dto.getNombres() + " " + dto.getApellidoPaterno());
            info.setConsultedAt(LocalDateTime.now());
            info.setIsValid(true);
            dniInfoRepository.save(info);
        } catch (Exception e) {
           
        }
    }
    
    public Map<String, Object> getApiStatus() {
        return Map.of("status", "ONLINE", "url", apiUrl);
    }
}