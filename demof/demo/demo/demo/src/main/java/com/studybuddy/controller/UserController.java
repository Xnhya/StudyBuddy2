package com.studybuddy.controller;

import com.studybuddy.model.User;
import com.studybuddy.model.Interest;
import com.studybuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ------------------------------------------------------------
    // PERFIL DEL USUARIO AUTENTICADO
    // ------------------------------------------------------------
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByUsernameOrEmail(username);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
        userMap.put("lastName", user.getLastName() != null ? user.getLastName() : "");
        userMap.put("fullName", getUserFullName(user));
        userMap.put("role", getUserRole(user));
        userMap.put("createdAt", user.getCreatedAt());
        
        // Campos adicionales
        userMap.put("career", user.getCareer() != null ? user.getCareer() : "");
        userMap.put("university", user.getUniversity() != null ? user.getUniversity() : "");
        userMap.put("semester", user.getSemester() != null ? user.getSemester() : null);
        userMap.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : null);
        userMap.put("gender", user.getGender() != null ? user.getGender() : "");
        
        // Intereses del usuario
        List<Map<String, Object>> interestsList = user.getInterests().stream()
                .map(interest -> {
                    Map<String, Object> interestMap = new HashMap<>();
                    interestMap.put("id", interest.getId());
                    interestMap.put("name", interest.getName());
                    interestMap.put("category", interest.getCategory());
                    return interestMap;
                })
                .collect(Collectors.toList());
        userMap.put("interests", interestsList);

        return ResponseEntity.ok(Map.of(
                "user", userMap,
                "menuItems", getMenuByRole(getUserRole(user))
        ));
    }

    // ------------------------------------------------------------
    // ACTUALIZAR PERFIL
    // ------------------------------------------------------------
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates,
                                           Authentication authentication) {

        String username = authentication.getName();
        User updatedUser = userService.updateUser(username, updates);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", updatedUser.getUsername());
        userMap.put("email", updatedUser.getEmail());
        userMap.put("firstName", updatedUser.getFirstName() != null ? updatedUser.getFirstName() : "");
        userMap.put("lastName", updatedUser.getLastName() != null ? updatedUser.getLastName() : "");
        userMap.put("fullName", updatedUser.getFullName());
        userMap.put("career", updatedUser.getCareer() != null ? updatedUser.getCareer() : "");
        userMap.put("university", updatedUser.getUniversity() != null ? updatedUser.getUniversity() : "");
        userMap.put("semester", updatedUser.getSemester() != null ? updatedUser.getSemester() : null);
        userMap.put("birthDate", updatedUser.getBirthDate() != null ? updatedUser.getBirthDate().toString() : null);
        userMap.put("gender", updatedUser.getGender() != null ? updatedUser.getGender() : "");

        return ResponseEntity.ok(Map.of(
                "message", "Perfil actualizado correctamente",
                "user", userMap
        ));
    }

    // ------------------------------------------------------------
    // DASHBOARD GENERAL DE USUARIO
    // ------------------------------------------------------------
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('STUDENT') or hasRole('PREMIUM')")
    public ResponseEntity<?> getUserDashboard(Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByUsernameOrEmail(username);

        return ResponseEntity.ok(Map.of(
                "welcome", "Bienvenido a tu dashboard, " + getUserFullName(user),
                "stats", Map.of(
                        "groupsJoined", 5,
                        "resourcesUploaded", 3,
                        "studyHours", 24
                ),
                "quickActions", List.of(
                        "Crear nuevo grupo",
                        "Buscar recursos",
                        "Unirse a una sesión"
                )
        ));
    }

    // ------------------------------------------------------------
    // DASHBOARD PREMIUM
    // ------------------------------------------------------------
    @GetMapping("/premium/dashboard")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<?> getPremiumDashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Dashboard Premium",
                "features", List.of(
                        "Acceso completo a recursos",
                        "Grupos ilimitados",
                        "Soporte prioritario",
                        "Estadísticas avanzadas"
                )
        ));
    }
    
    // ------------------------------------------------------------
    // GESTIÓN DE INTERESES/MATERIAS
    // ------------------------------------------------------------
    @GetMapping("/interests")
    public ResponseEntity<?> getUserInterests(Authentication authentication) {
        String username = authentication.getName();
        List<Interest> interests = userService.getUserInterests(username);
        
        List<Map<String, Object>> interestsList = interests.stream()
                .map(interest -> {
                    Map<String, Object> interestMap = new HashMap<>();
                    interestMap.put("id", interest.getId());
                    interestMap.put("name", interest.getName());
                    interestMap.put("category", interest.getCategory());
                    interestMap.put("icon", interest.getIcon());
                    return interestMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("interests", interestsList));
    }
    
    @GetMapping("/interests/available")
    public ResponseEntity<?> getAvailableInterests(Authentication authentication) {
        String username = authentication.getName();
        List<Interest> interests = userService.getAvailableInterests(username);
        
        List<Map<String, Object>> interestsList = interests.stream()
                .map(interest -> {
                    Map<String, Object> interestMap = new HashMap<>();
                    interestMap.put("id", interest.getId());
                    interestMap.put("name", interest.getName());
                    interestMap.put("category", interest.getCategory());
                    interestMap.put("icon", interest.getIcon());
                    return interestMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("interests", interestsList));
    }
    
    @PostMapping("/interests/{interestId}")
    public ResponseEntity<?> addInterest(@PathVariable Long interestId, Authentication authentication) {
        String username = authentication.getName();
        try {
            userService.addInterestToUser(username, interestId);
            return ResponseEntity.ok(Map.of(
                    "message", "Materia agregada correctamente",
                    "success", true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "success", false
            ));
        }
    }
    
    @DeleteMapping("/interests/{interestId}")
    public ResponseEntity<?> removeInterest(@PathVariable Long interestId, Authentication authentication) {
        String username = authentication.getName();
        try {
            userService.removeInterestFromUser(username, interestId);
            return ResponseEntity.ok(Map.of(
                    "message", "Materia eliminada correctamente",
                    "success", true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "success", false
            ));
        }
    }

    // ------------------------------------------------------------
    // MÉTODOS AUXILIARES
    // ------------------------------------------------------------

    // FULL NAME (tu User NO tiene firstName/lastName)
    private String getUserFullName(User user) {
        return (user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName()
                : user.getUsername();
    }

// ROLE normalizado (maneja múltiples firmas posibles en la clase Role)
private String getUserRole(User user) {
    if (user == null || user.getRole() == null) {
        return "ROLE_USER";
    }

    Object roleObj = user.getRole();

    try {
        Class<?> rc = roleObj.getClass();

        // 1) Intentar getName()
        try {
            java.lang.reflect.Method m = rc.getMethod("getName");
            Object raw = m.invoke(roleObj);
            if (raw != null) return normalizeRole(raw.toString());
        } catch (NoSuchMethodException ignored) {}

        // 2) Intentar getRoleName()
        try {
            java.lang.reflect.Method m = rc.getMethod("getRoleName");
            Object raw = m.invoke(roleObj);
            if (raw != null) return normalizeRole(raw.toString());
        } catch (NoSuchMethodException ignored) {}

        // 3) Intentar name() (si Role es un enum)
        try {
            java.lang.reflect.Method m = rc.getMethod("name");
            Object raw = m.invoke(roleObj);
            if (raw != null) return normalizeRole(raw.toString());
        } catch (NoSuchMethodException ignored) {}

        // 4) Intentar getValue() u otros getters genéricos
        try {
            java.lang.reflect.Method m = rc.getMethod("getValue");
            Object raw = m.invoke(roleObj);
            if (raw != null) return normalizeRole(raw.toString());
        } catch (NoSuchMethodException ignored) {}

        // 5) Fallback: usar toString() del objeto Role
        String fallback = roleObj.toString();
        return normalizeRole(fallback);
    } catch (Exception e) {
        // En caso de cualquier error inesperado, devolvemos rol por defecto
        return "ROLE_USER";
    }
}

// Normaliza cadenas de rol a formato ROLE_*
private String normalizeRole(String roleStr) {
    if (roleStr == null) return "ROLE_USER";
    roleStr = roleStr.trim();
    if (roleStr.isEmpty()) return "ROLE_USER";

    // Si ya viene en mayúsculas y con ROLE_ lo devolvemos
    if (roleStr.startsWith("ROLE_")) return roleStr;

    // Si viene en minúsculas o sin ROLE_, normalizamos a mayúsculas y añadimos prefijo
    roleStr = roleStr.toUpperCase();
    return roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr;
}


    // MENÚ SEGÚN ROL
    private List<Map<String, String>> getMenuByRole(String role) {

        switch (role) {

            case "ROLE_ADMIN":
                return List.of(
                        Map.of("title", "Dashboard", "path", "/admin/dashboard"),
                        Map.of("title", "Usuarios", "path", "/admin/users"),
                        Map.of("title", "Grupos", "path", "/admin/groups"),
                        Map.of("title", "Reportes", "path", "/admin/reports")
                );

            case "ROLE_MODERATOR":
                return List.of(
                        Map.of("title", "Panel Moderador", "path", "/moderator/dashboard"),
                        Map.of("title", "Reportes", "path", "/moderator/reports"),
                        Map.of("title", "Moderación", "path", "/moderator/content")
                );

            case "ROLE_PREMIUM":
                return List.of(
                        Map.of("title", "Mi Dashboard", "path", "/dashboard"),
                        Map.of("title", "Grupos", "path", "/groups"),
                        Map.of("title", "Recursos", "path", "/resources"),
                        Map.of("title", "Estadísticas", "path", "/stats")
                );

            default: // USER o STUDENT
                return List.of(
                        Map.of("title", "Inicio", "path", "/home"),
                        Map.of("title", "Mis Grupos", "path", "/my-groups"),
                        Map.of("title", "Recursos", "path", "/resources"),
                        Map.of("title", "Perfil", "path", "/profile")
                );
        }
    }
}
