package com.studybuddy.controller;

import com.studybuddy.dto.RegisterRequest;
import com.studybuddy.model.User;
import com.studybuddy.security.JwtTokenProvider;
import com.studybuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    // -----------------------------------------------------------
    // LOGIN
    // -----------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());

        return ResponseEntity.ok(Map.of(
                "accessToken", jwt,
                "tokenType", "Bearer",
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getPrimaryRole()
                )
        ));
    }

    // -----------------------------------------------------------
    // REGISTER
    // -----------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {

        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username ya está en uso"));
        }

        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email ya está registrado"));
        }

        User user = userService.createUser(request);

        return ResponseEntity.ok(Map.of(
                "message", "Usuario registrado exitosamente",
                "userId", user.getId()
        ));
    }

    // -----------------------------------------------------------
    // LOGIN REQUEST DTO INTERNO (ESTE SÍ DEBE QUEDAR AQUÍ)
    // -----------------------------------------------------------
    public static class LoginRequest {
        private String usernameOrEmail;
        private String password;

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
