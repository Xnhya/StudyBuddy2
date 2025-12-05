package com.studybuddy.controller;

import com.studybuddy.dto.RegisterRequest;
import com.studybuddy.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthViewController {

    private final UserService userService;

    public AuthViewController(UserService userService) {
        this.userService = userService;
    }

    // Mostrar formulario de registro
    @GetMapping("/auth/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new RegisterRequest());
        return "auth/register";
    }

    // Procesar registro desde HTML
    @PostMapping("/auth/register")
    public String submitRegister(
            @ModelAttribute("registerDTO") RegisterRequest request,
            Model model) {

        if (userService.existsByUsername(request.getUsername())) {
            model.addAttribute("error", "El nombre de usuario ya está en uso");
            return "auth/register";
        }

        if (userService.existsByEmail(request.getEmail())) {
            model.addAttribute("error", "El email ya está registrado");
            return "auth/register";
        }

        userService.createUser(request);

        model.addAttribute("success", "Registro exitoso. Ahora inicia sesión.");
        return "redirect:/login";
    }
}
