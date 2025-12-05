package com.studybuddy.config;

import com.studybuddy.model.*;
import com.studybuddy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

@Configuration
public class DataInitializer {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            InterestRepository interestRepository,
            StudyGroupRepository studyGroupRepository) {
        
        return args -> {
            // 1. Crear Roles
            createRoleIfNotFound(roleRepository, Role.ERole.ROLE_STUDENT);
            createRoleIfNotFound(roleRepository, Role.ERole.ROLE_MODERATOR);
            createRoleIfNotFound(roleRepository, Role.ERole.ROLE_ADMIN);
            
            // 2. Crear Usuarios
            if (userRepository.count() == 0) {
                createUser(userRepository, roleRepository, "admin", "admin@studybuddy.com", "admin123", Role.ERole.ROLE_ADMIN);
                createUser(userRepository, roleRepository, "user", "user@studybuddy.com", "user123", Role.ERole.ROLE_STUDENT);
                System.out.println("Usuarios creados: admin/admin123 y user/user123");
            }
        };
    }

    private void createRoleIfNotFound(RoleRepository repo, Role.ERole roleName) {
        if (repo.findByName(roleName).isEmpty()) {
            repo.save(new Role(roleName));
        }
    }

    private void createUser(UserRepository userRepo, RoleRepository roleRepo, 
                          String username, String email, String password, Role.ERole roleName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        
        Role role = roleRepo.findByName(roleName).orElseThrow();
        user.setRoles(Set.of(role));
        
        userRepo.save(user);
    }
}