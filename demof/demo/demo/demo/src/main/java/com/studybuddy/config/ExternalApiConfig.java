package com.studybuddy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Configuration
public class ExternalApiConfig {
    
    @Value("${app.external-api.currency.url:https://api.apilayer.com/exchangerates_data}")
    private String currencyApiUrl;
    
    @Value("${app.external-api.currency.apikey:}")
    private String currencyApiKey;

    @Value("${app.external-api.dni.url:https://dniruc.apisperu.com/api/v1}")
    private String dniApiUrl;
    
    @Value("${app.external-api.dni.apikey:}")
    private String dniApiKey;
    
    // Configuración para API de cambio de moneda
    @Bean(name = "currencyWebClient")
    public WebClient currencyWebClient() {
        return WebClient.builder()
                .baseUrl(currencyApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("apikey", currencyApiKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    // Configuración para API de consulta DNI
    @Bean(name = "dniWebClient")
    public WebClient dniWebClient() {
        return WebClient.builder()
                .baseUrl(dniApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + dniApiKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    // Configuración para API genérica
    @Bean(name = "genericWebClient")
    public WebClient genericWebClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    // Bean para obtener configuración de APIs
    @Bean
    public ExternalApiProperties externalApiProperties() {
        return new ExternalApiProperties(
                currencyApiUrl,
                currencyApiKey,
                dniApiUrl,
                dniApiKey
        );
    }
    
    // Clase para almacenar propiedades de APIs externas (sealed para mejor control)
    public static final class ExternalApiProperties {
        private final String currencyUrl;
        private final String currencyApiKey;
        private final String dniUrl;
        private final String dniApiKey;
        
        public ExternalApiProperties(String currencyUrl, String currencyApiKey, 
                                    String dniUrl, String dniApiKey) {
            this.currencyUrl = currencyUrl;
            this.currencyApiKey = currencyApiKey;
            this.dniUrl = dniUrl;
            this.dniApiKey = dniApiKey;
        }
        
        public String getCurrencyUrl() { 
            return currencyUrl; 
        }
        
        public String getCurrencyApiKey() { 
            return currencyApiKey; 
        }
        
        public String getDniUrl() { 
            return dniUrl; 
        }
        
        public String getDniApiKey() { 
            return dniApiKey; 
        }
        
        public boolean isCurrencyApiConfigured() {
            return currencyApiKey != null && !currencyApiKey.trim().isEmpty();
        }
        
        public boolean isDniApiConfigured() {
            return dniApiKey != null && !dniApiKey.trim().isEmpty();
        }
    }
}