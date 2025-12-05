package com.studybuddy.controller;

import com.studybuddy.model.User;
import com.studybuddy.service.TwoFactorService;
import com.studybuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para manejar la verificación de 2FA después del login
 */
@Controller
@RequestMapping("/auth")
public class TwoFactorController {
    
    private static final Logger logger = LoggerFactory.getLogger(TwoFactorController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TwoFactorService twoFactorService;
    
    /**
     * Muestra la vista de verificación 2FA
     */
    @GetMapping("/verify-2fa")
    public String showVerify2faPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("2fa_username");
        
        if (username == null) {
            logger.warn("Unauthorized access to verify-2fa page without username in session");
            return "redirect:/login";
        }
        
        model.addAttribute("username", username);
        return "auth/verify-2fa";
    }
    
    /**
     * Verifica el código OTP ingresado por el usuario
     */
    @PostMapping("/verify-2fa")
    public String verify2fa(@RequestParam("otp") String otp,
                           HttpSession session,
                           Model model) {
        String username = (String) session.getAttribute("2fa_username");
        
        if (username == null) {
            logger.warn("Unauthorized 2FA verification attempt without username in session");
            return "redirect:/login";
        }
        
        User user = userService.findByUsernameOrEmail(username);
        
        if (user == null) {
            logger.error("User not found during 2FA verification: {}", username);
            return "redirect:/login?error=true";
        }
        
        // Validar el OTP
        if (twoFactorService.validateOtp(user, otp)) {
            logger.info("2FA verification successful for user: {}", username);
            
            // Limpiar el OTP de la BD
            twoFactorService.clearOtp(user);
            
            // Autenticar al usuario y guardar en SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Limpiar sesión
            session.removeAttribute("2fa_username");
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            logger.info("User {} successfully authenticated after 2FA verification", username);
            
            // Determinar el dashboard según el rol del usuario
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
            
            String redirectUrl = isAdmin ? "/admin/dashboard" : "/user/dashboard";
            logger.info("Redirecting user {} to {}", username, redirectUrl);
            
            return "redirect:" + redirectUrl;
        } else {
            logger.warn("Invalid OTP attempt for user: {}", username);
            model.addAttribute("error", "Código OTP inválido o expirado. Intenta de nuevo.");
            model.addAttribute("username", username);
            return "auth/verify-2fa";
        }
    }
}
