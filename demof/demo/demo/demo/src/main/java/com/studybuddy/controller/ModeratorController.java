package com.studybuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderator")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModeratorController {

    // ---------------------------------------------------------
    // DASHBOARD
    // ---------------------------------------------------------
    @GetMapping("/dashboard")
    public ResponseEntity<?> getModeratorDashboard() {

        return ResponseEntity.ok(Map.of(
                "message", "Panel de Moderador",
                "pendingActions", List.of(
                        Map.of("id", 1, "type", "REPORT", "description", "Reporte de contenido inapropiado"),
                        Map.of("id", 2, "type", "USER_REVIEW", "description", "Nuevo usuario por aprobar"),
                        Map.of("id", 3, "type", "GROUP_REVIEW", "description", "Grupo pendiente de revisión")
                ),
                "quickStats", Map.of(
                        "reportsToday", 5,
                        "usersReviewed", 12,
                        "groupsModerated", 3
                )
        ));
    }

    // ---------------------------------------------------------
    // REPORTES
    // ---------------------------------------------------------
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        return ResponseEntity.ok(Map.of(
                "reports", List.of(
                        Map.of(
                                "id", 1,
                                "type", "CONTENT",
                                "reason", "Contenido inapropiado",
                                "status", "PENDING",
                                "reportedBy", "usuario123",
                                "createdAt", "2024-01-15T10:30:00"
                        ),
                        Map.of(
                                "id", 2,
                                "type", "USER",
                                "reason", "Comportamiento inadecuado",
                                "status", "IN_REVIEW",
                                "reportedBy", "estudiante456",
                                "createdAt", "2024-01-14T15:45:00"
                        )
                )
        ));
    }

    // ---------------------------------------------------------
    // RESOLVER REPORTE
    // ---------------------------------------------------------
    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<?> resolveReport(
            @PathVariable Long reportId,
            @RequestBody(required = false) Map<String, String> resolution
    ) {
        if (resolution == null || !resolution.containsKey("action")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Debe proporcionar un campo 'action'"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Reporte resuelto",
                "reportId", reportId,
                "action", resolution.get("action"),
                "notes", resolution.getOrDefault("notes", ""),
                "resolvedAt", LocalDateTime.now().toString()
        ));
    }

    // ---------------------------------------------------------
    // USUARIOS PENDIENTES DE APROBACIÓN
    // ---------------------------------------------------------
    @GetMapping("/users/pending")
    public ResponseEntity<?> getPendingUsers() {
        return ResponseEntity.ok(Map.of(
                "pendingUsers", List.of(
                        Map.of("id", 101, "username", "nuevo_user1", "email", "user1@email.com"),
                        Map.of("id", 102, "username", "nuevo_user2", "email", "user2@email.com")
                )
        ));
    }

    // ---------------------------------------------------------
    // APROBAR USUARIO
    // ---------------------------------------------------------
    @PutMapping("/users/{userId}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "message", "Usuario aprobado",
                "userId", userId,
                "status", "APPROVED"
        ));
    }

    // ---------------------------------------------------------
    // RECHAZAR USUARIO
    // ---------------------------------------------------------
    @PutMapping("/users/{userId}/reject")
    public ResponseEntity<?> rejectUser(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> reason
    ) {
        return ResponseEntity.ok(Map.of(
                "message", "Usuario rechazado",
                "userId", userId,
                "reason", reason != null ? reason.getOrDefault("reason", "Sin motivo") : "Sin motivo",
                "status", "REJECTED"
        ));
    }
}
