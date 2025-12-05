package com.studybuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    // Datos de ejemplo en memoria
    private final List<Map<String, Object>> mockResources = createMockResources();

    @GetMapping("/resources")
    public ResponseEntity<?> getResources(@RequestParam(defaultValue = "all") String type,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        
        List<Map<String, Object>> filteredResources = filterResourcesByType(type);
        
        // Paginación
        int start = page * size;
        int end = Math.min(start + size, filteredResources.size());
        
        if (start >= filteredResources.size()) {
            return ResponseEntity.ok(Map.of(
                "resources", Collections.emptyList(),
                "page", page,
                "total", filteredResources.size()
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "resources", filteredResources.subList(start, end),
            "page", page,
            "total", filteredResources.size()
        ));
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<?> getResourceById(@PathVariable Long id) {
        return mockResources.stream()
                .filter(resource -> id.equals(resource.get("id")))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/resources")
    public ResponseEntity<?> createResource(@RequestBody Map<String, Object> resourceData) {
        Long newId = (long) (mockResources.size() + 1);
        Map<String, Object> newResource = new HashMap<>(resourceData);
        newResource.put("id", newId);
        newResource.put("createdAt", new Date());
        mockResources.add(newResource);
        
        return ResponseEntity.ok(Map.of(
            "message", "Recurso creado exitosamente",
            "resourceId", newId,
            "resource", newResource
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q,
                                    @RequestParam(defaultValue = "all") String category) {
        List<Map<String, Object>> results = mockResources.stream()
                .filter(resource -> {
                    String title = (String) resource.get("title");
                    String description = (String) resource.get("description");
                    String type = (String) resource.get("type");
                    
                    boolean matchesQuery = (title != null && title.toLowerCase().contains(q.toLowerCase())) ||
                                          (description != null && description.toLowerCase().contains(q.toLowerCase()));
                    
                    boolean matchesCategory = "all".equalsIgnoreCase(category) ||
                                             (category != null && category.equalsIgnoreCase(type));
                    
                    return matchesQuery && matchesCategory;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "query", q,
            "category", category,
            "results", results,
            "count", results.size()
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getApiStats() {
        return ResponseEntity.ok(Map.of(
            "apiName", "StudyBuddy API",
            "version", "1.0.0",
            "uptime", "99.5%",
            "totalResources", mockResources.size(),
            "endpoints", Map.of(
                "GET /api/v1/resources", "Listar recursos paginados",
                "GET /api/v1/resources/{id}", "Obtener recurso por ID",
                "POST /api/v1/resources", "Crear nuevo recurso",
                "GET /api/v1/search", "Buscar recursos por término",
                "GET /api/v1/stats", "Estadísticas de la API"
            )
        ));
    }

    // Métodos auxiliares
    private List<Map<String, Object>> filterResourcesByType(String type) {
        if ("all".equalsIgnoreCase(type)) {
            return new ArrayList<>(mockResources);
        }
        
        return mockResources.stream()
                .filter(resource -> type.equalsIgnoreCase((String) resource.get("type")))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<Map<String, Object>> createMockResources() {
        List<Map<String, Object>> resources = new ArrayList<>();
        
        // Crear algunos recursos de ejemplo
        for (int i = 1; i <= 15; i++) {
            Map<String, Object> resource = new HashMap<>();
            resource.put("id", (long) i);
            resource.put("title", "Recurso Educativo " + i);
            resource.put("description", "Este es un recurso educativo de ejemplo número " + i);
            resource.put("type", i % 3 == 0 ? "PDF" : i % 3 == 1 ? "VIDEO" : "ARTICLE");
            resource.put("category", i % 4 == 0 ? "Math" : i % 4 == 1 ? "Science" : i % 4 == 2 ? "History" : "Programming");
            resource.put("author", "Autor " + (i % 5 + 1));
            resource.put("views", 50 + i * 10);
            resource.put("rating", 3.5 + (i % 10) * 0.15);
            resource.put("createdAt", new Date(System.currentTimeMillis() - i * 86400000L)); // i días atrás
            resources.add(resource);
        }
        
        return resources;
    }
}