package com.studybuddy.security;

import com.studybuddy.model.User;
import com.studybuddy.service.TwoFactorService;
import com.studybuddy.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Manejador de login exitoso que implementa lógica de 2FA
 * - Genera OTP para TODOS los usuarios
 * - Redirige a /auth/verify-2fa para ingresar el código
 * - Después de verificación, redirige al dashboard según el rol
 */
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomLoginSuccessHandler.class);
    
    private final UserService userService;
    private final TwoFactorService twoFactorService;
    
    // Inyección por constructor para evitar dependencia circular
    public CustomLoginSuccessHandler(UserService userService, TwoFactorService twoFactorService) {
        this.userService = userService;
        this.twoFactorService = twoFactorService;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        User user = userService.findByUsernameOrEmail(username);
        
        if (user == null) {
            logger.error("User not found during login success handler: {}", username);
            response.sendRedirect("/login?error=true");
            return;
        }
        
        // Guardar username en sesión para el controlador 2FA
        HttpSession session = request.getSession();
        session.setAttribute("2fa_username", username);
        
        logger.info("Login detected for user: {} with roles: {}", username, user.getRoles());
        
        try {
            // Generar OTP, guardarlo en BD y enviar por correo (para TODOS los usuarios)
            twoFactorService.generateAndSendOtp(user);
            
            logger.info("OTP generated and email sent for user: {}", username);
            
            // Redirigir a pantalla de verificación 2FA
            response.sendRedirect("/auth/verify-2fa");
        } catch (Exception e) {
            logger.error("Error generating OTP for user {}: {}", username, e.getMessage(), e);
            response.sendRedirect("/auth/verify-2fa?error=otp_generation_failed");
        }
    }
}

