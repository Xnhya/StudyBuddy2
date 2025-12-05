package com.studybuddy.controller;

import com.studybuddy.model.User;
import com.studybuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        List<User> users = userService.findAll();
        
        return ResponseEntity.ok(Map.of(
            "stats", Map.of(
                "totalUsers", users.size(),
                "activeUsers", "No disponible",
                "premiumUsers", "No disponible",
                "todayRegistrations", "No disponible"
            ),
            "recentActivities", List.of(
                "Panel de administración activo",
                "Total usuarios: " + users.size()
            )
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.findAll();
        
        // Crear DTOs básicos sin depender de métodos específicos
        List<Map<String, Object>> userList = users.stream()
            .map(user -> {
                Map<String, Object> dto = Map.of(
                    "id", user.getId(),
                    "username", getSafeString(user, "getUsername", ""),
                    "email", getSafeString(user, "getEmail", "")
                );
                return dto;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "users", userList,
            "total", users.size()
        ));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, 
                                           @RequestBody Map<String, String> request) {
        String newRole = request.get("role");
        
        try {
            User updatedUser = userService.updateRole(userId, newRole);
            return ResponseEntity.ok(Map.of(
                "message", "Rol actualizado exitosamente",
                "userId", userId,
                "newRole", newRole,
                "username", updatedUser.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Error al actualizar rol: " + e.getMessage(),
                "userId", userId
            ));
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId,
                                             @RequestBody Map<String, Boolean> request) {
        Boolean active = request.get("active");
        
        if (active == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "El campo 'active' es requerido",
                "userId", userId
            ));
        }
        
        try {
            User updatedUser = userService.updateStatus(userId, active);
            return ResponseEntity.ok(Map.of(
                "message", active ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente",
                "userId", userId,
                "status", active ? "ACTIVE" : "INACTIVE",
                "username", updatedUser.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Error al actualizar estado: " + e.getMessage(),
                "userId", userId
            ));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteById(userId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Usuario eliminado",
            "userId", userId
        ));
    }

    // Método auxiliar para obtener valores de forma segura
    private String getSafeString(Object obj, String methodName, String defaultValue) {
        try {
            Object result = obj.getClass().getMethod(methodName).invoke(obj);
            return result != null ? result.toString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}