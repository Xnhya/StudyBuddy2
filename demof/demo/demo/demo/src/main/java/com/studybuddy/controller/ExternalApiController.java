package com.studybuddy.controller;

import com.studybuddy.dto.ApiResponse;
import com.studybuddy.dto.CurrencyDTO;
import com.studybuddy.dto.DniDTO;
import com.studybuddy.service.CurrencyService;
import com.studybuddy.service.DniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    private final DniService dniService;
    private final CurrencyService currencyService;

    // Inyectamos ambos servicios
    public ExternalApiController(DniService dniService, CurrencyService currencyService) {
        this.dniService = dniService;
        this.currencyService = currencyService;
    }

    // ==========================================
    // NUEVO: ENDPOINT DE MONEDA
    // ==========================================
    @GetMapping("/currency/convert")
    public ResponseEntity<ApiResponse<CurrencyDTO>> convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam Double amount) {

        try {
            // Usamos el servicio que ya tiene lógica de caché y API real
            Double result = currencyService.convertCurrency(from, to, amount);
            
            // Calculamos la tasa implícita para mostrarla
            Double rate = (amount != 0) ? result / amount : 0.0;

            CurrencyDTO dto = new CurrencyDTO(from, to, amount, result, rate);
            
            return ResponseEntity.ok(ApiResponse.success("Conversión exitosa", dto));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Error en conversión: " + e.getMessage(), null)
            );
        }
    }

    // ==========================================
    // ENDPOINTS DE DNI (EXISTENTES)
    // ==========================================
    @GetMapping("/dni/{dni}")
    public ResponseEntity<ApiResponse<DniDTO>> consultarDni(@PathVariable String dni) {
        if (!isDniValido(dni)) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("DNI debe tener 8 dígitos numéricos", null)
            );
        }
        try {
            DniDTO resultado = dniService.consultarDni(dni).block();
            if (resultado != null && resultado.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("DNI encontrado", resultado));
            }
            return ResponseEntity.ok(ApiResponse.error(resultado != null ? resultado.getError() : "No encontrado", resultado));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error interno: " + e.getMessage(), null));
        }
    }

    @GetMapping("/dni/status")
    public ResponseEntity<ApiResponse<Object>> getDniApiStatus() {
        return ResponseEntity.ok(ApiResponse.success("Estado API", dniService.getApiStatus()));
    }

    // Métodos auxiliares
    private boolean isDniValido(String dni) {
        return dni != null && dni.matches("\\d{8}");
    }
}