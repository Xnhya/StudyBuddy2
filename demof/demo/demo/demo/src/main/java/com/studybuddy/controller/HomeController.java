package com.studybuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    // ---------------------------------------------------------
    // INFORMACIÓN GENERAL DE LA API
    // ---------------------------------------------------------
    @GetMapping("/home")
    public ResponseEntity<?> getHomeInfo() {

        return ResponseEntity.ok(Map.of(
                "apiName", "StudyBuddy API",
                "message", "Bienvenido a StudyBuddy",
                "version", "1.0.0",
                "documentation", "/api/docs",
                "endpoints", Map.of(
                        "auth", "/api/auth/**",
                        "users", "/api/users/**",
                        "groups", "/api/groups/**",
                        "admin", "/api/admin/**",
                        "moderator", "/api/moderator/**",
                        "external", "/api/external/**"
                )
        ));
    }

    // ---------------------------------------------------------
    // ESTADO DEL SERVICIO
    // ---------------------------------------------------------
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "StudyBuddy API",
                "timestamp", Instant.now().toString()
        ));
    }
}
