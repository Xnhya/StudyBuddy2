package com.studybuddy.controller;

import com.studybuddy.controller.StudyGroupController.CreateGroupRequest;
import com.studybuddy.service.StudyGroupService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GroupWebController {

    private final StudyGroupService groupService;

    public GroupWebController(StudyGroupService groupService) {
        this.groupService = groupService;
    }

    // ---------------------------------------------------------
    // CREAR GRUPO DESDE FORMULARIO WEB
    // ---------------------------------------------------------
    @PostMapping("/groups/create")
    public String processCreateGroup(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String subject,
            @RequestParam(defaultValue = "10") Integer maxMembers,
            @RequestParam(defaultValue = "public") String visibility,
            @RequestParam(name = "enableChat", defaultValue = "false") String enableChatParam,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            CreateGroupRequest request = new CreateGroupRequest();
            request.setName(name);
            request.setDescription(description);
            request.setSubject(subject);
            request.setMaxMembers(maxMembers);
            // If the DTO supports chatEnabled, set it
            boolean enableChat = "on".equalsIgnoreCase(enableChatParam) || "true".equalsIgnoreCase(enableChatParam);
            try {
                request.getClass().getMethod("setChatEnabled", boolean.class).invoke(request, enableChat);
            } catch (Exception ignored) {
            }

            // CORREGIDO — setter correcto
            boolean isPublic = visibility.equalsIgnoreCase("public");
            request.setPublic(isPublic);

            groupService.createGroup(request, authentication.getName());

            redirectAttributes.addFlashAttribute("success", "¡Grupo creado exitosamente!");
            return "redirect:/groups";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al crear grupo: " + e.getMessage());
            return "redirect:/groups/create";
        }
    }

    // ---------------------------------------------------------
    // UNIRSE A UN GRUPO
    // ---------------------------------------------------------
    @PostMapping("/groups/{id}/join")
    public String joinGroup(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupService.joinGroup(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success",
                    "Te has unido al grupo correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "No pudiste unirte: " + e.getMessage());
        }

        return "redirect:/groups/" + id;
    }

    // ---------------------------------------------------------
    // SALIR DE UN GRUPO
    // ---------------------------------------------------------
    @PostMapping("/groups/{id}/leave")
    public String leaveGroup(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            groupService.leaveGroup(id, authentication.getName());
            redirectAttributes.addFlashAttribute("info", "Has salido del grupo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al salir: " + e.getMessage());
        }

        return "redirect:/groups/" + id;
    }
}
