package com.studybuddy.service;

import com.studybuddy.model.User;
import com.studybuddy.dto.RegisterRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    // ============================
    // BÚSQUEDAS
    // ============================
    User findByUsernameOrEmail(String usernameOrEmail);
    Optional<User> findById(Long id);
    User findByUsername(String username);
    List<User> findAll();

    // ============================
    // VERIFICACIONES
    // ============================
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ============================
    // CRUD PRINCIPAL
    // ============================
    User save(User user);
    void deleteById(Long id);

    // Crear usuario desde registro
    User createUser(RegisterRequest registerRequest);

    // Actualizar datos del usuario autenticado
    User updateUser(String username, Map<String, String> updates);

    // ============================
    // FUNCIONES PARA ADMIN
    // ============================

    long countAllUsers();
    long countActiveUsers();
    long countByRole(String role);
    long countTodayRegistrations();

    User updateRole(Long userId, String role);
    User updateStatus(Long userId, boolean active);
    void deleteUser(Long userId);
    
    // ============================
    // GESTIÓN DE INTERESES
    // ============================
    List<com.studybuddy.model.Interest> getUserInterests(String username);
    com.studybuddy.model.User addInterestToUser(String username, Long interestId);
    com.studybuddy.model.User removeInterestFromUser(String username, Long interestId);
    List<com.studybuddy.model.Interest> getAvailableInterests(String username);
}
