package com.studybuddy.controller;

import com.studybuddy.model.StudyGroup;
import com.studybuddy.service.StudyGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class StudyGroupController {

    private final StudyGroupService groupService;

    public StudyGroupController(StudyGroupService groupService) {
        this.groupService = groupService;
    }

    // ---------------------------------------------------------------
    // LISTAR TODOS LOS GRUPOS
    // ---------------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAllGroups() {

        List<StudyGroup> groups = groupService.findAll();

        List<Map<String, Object>> response = groups.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("groups", response));
    }

    // ---------------------------------------------------------------
    // CREAR GRUPO
    // ---------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication
    ) {
        String creatorUsername = authentication.getName();

        StudyGroup group = groupService.createGroup(request, creatorUsername);

        return ResponseEntity.ok(Map.of(
                "message", "Grupo creado exitosamente",
                "group", convertToDto(group)
        ));
    }

    // ---------------------------------------------------------------
    // CONVERTIR A DTO SEGURO
    // ---------------------------------------------------------------
    private Map<String, Object> convertToDto(StudyGroup group) {

        return Map.of(
                "id", group.getId(),
                "name", group.getName(),
                "description", group.getDescription(),
                "subject", group.getSubject(),
                "currentMembers", group.getMembers() != null ? group.getMembers().size() : 0,
                "maxMembers", group.getMaxMembers(),
                "createdBy", group.getCreator() != null ? group.getCreator().getUsername() : "Desconocido",
                "isPublic", group.getIsPublic()
        );
    }

    // ---------------------------------------------------------------
    // DTO PARA CREAR GRUPO
    // ---------------------------------------------------------------
    public static class CreateGroupRequest {

        @NotBlank(message = "El nombre del grupo es obligatorio")
        private String name;

        private String description;

        private String subject;

        @Min(value = 1, message = "Debe permitir al menos 1 miembro")
        private Integer maxMembers;

        private boolean isPublic = true;

        private boolean chatEnabled = false;

        // Getters y Setters

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public Integer getMaxMembers() { return maxMembers; }
        public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }

        public boolean isChatEnabled() { return chatEnabled; }
        public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }
    }
}
