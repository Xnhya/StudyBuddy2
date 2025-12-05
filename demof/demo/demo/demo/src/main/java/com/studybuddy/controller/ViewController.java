package com.studybuddy.controller;

import com.studybuddy.dto.RegisterRequest;
import com.studybuddy.service.StudyGroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    private final StudyGroupService groupService;

    public ViewController(StudyGroupService groupService) {
        this.groupService = groupService;
    }

    /* ============================
       PÁGINAS PRINCIPALES
    ============================ */

    @GetMapping("/")
    public String index() { 
        return "redirect:/home"; 
    }

    @GetMapping("/login")
    public String login() { 
        return "auth/login"; 
    }

    // IMPORTANTE: YA NO incluye /auth/register para evitar conflicto
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerDTO", new RegisterRequest());
        return "auth/register";
    }


    /* ============================
       GRUPOS (PÚBLICOS / SISTEMA)
    ============================ */

    @GetMapping({"/groups", "/user/groups"})
    public String groupsList(Model model) {
        model.addAttribute("groups", groupService.findAll());
        return "group/list";
    }

    @GetMapping({"/groups/create", "/user/groups/create"})
    public String createGroup() {
        return "group/create";
    }

    @GetMapping({"/groups/{id}", "/user/groups/{id}"})
    public String groupDetail(@PathVariable Long id, Model model) {
        model.addAttribute("group", groupService.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado")));
        return "group/detail";
    }


    /* ============================
       USUARIO (DASHBOARD / PERFIL)
    ============================ */

    @GetMapping({"/dashboard", "/user/dashboard"})
    public String userDashboard() { 
        return "user/dashboard"; 
    }

    @GetMapping({"/profile", "/user/profile"})
    public String userProfile() { 
        return "user/profile"; 
    }

    @GetMapping({"/resources", "/user/resources"})
    public String userResources() { 
        return "user/resources"; 
    }

    @GetMapping({"/search", "/user/search"})
    public String userSearch() { 
        return "user/search"; 
    }


    /* ============================
       PÁGINAS ESTÁTICAS
    ============================ */

    @GetMapping("/about") 
    public String about() { return "about"; }

    @GetMapping("/contact") 
    public String contact() { return "contact"; }


    /* ============================
       APIS
    ============================ */

    @GetMapping("/api/currency")
    public String currencyPage() { 
        return "api/currency"; 
    }

    @GetMapping("/api/dni")
    public String dniPage() { 
        return "api/dni"; 
    }

    @GetMapping("/api/external")
    public String externalApiPage() { 
        return "api/external-api"; 
    }


    /* ============================
       ADMIN
    ============================ */

    @GetMapping("/admin/dashboard")
    public String adminDashboard() { 
        return "admin/dashboard"; 
    }

    @GetMapping("/admin/users")
    public String adminUsers() { 
        return "admin/users"; 
    }

    @GetMapping("/admin/groups")
    public String adminGroups() { 
        return "admin/groups"; 
    }

    @GetMapping("/admin/reports")
    public String adminReports() {
        return "admin/reports"; 
    }


    /* ============================
       MODERADOR
    ============================ */

    @GetMapping("/moderator/dashboard")
    public String moderatorDashboard() { 
        return "moderator/dashboard"; 
    }

    @GetMapping("/moderator/content")
    public String moderatorContent() { 
        return "moderator/content"; 
    }
}
