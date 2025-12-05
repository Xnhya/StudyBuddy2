package com.studybuddy.service;

import com.studybuddy.model.User;
import com.studybuddy.model.Role;
import com.studybuddy.model.Interest;
import com.studybuddy.repository.UserRepository;
import com.studybuddy.repository.RoleRepository;
import com.studybuddy.repository.InterestRepository;
import com.studybuddy.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           InterestRepository interestRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.interestRepository = interestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= BÚSQUEDAS ===================

    @Override
    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    @Override
    public List<User> findAll() { return userRepository.findAll(); }

    @Override
    public User save(User user) { return userRepository.save(user); }

    @Override
    public void deleteById(Long id) { userRepository.deleteById(id); }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // ================= CREAR USUARIO ===================

    @Override
    public User createUser(RegisterRequest registerRequest) {

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Nombre y apellido
        if (registerRequest.getFullName() != null) {
            String[] names = registerRequest.getFullName().split(" ", 2);
            user.setFirstName(names.length > 0 ? names[0] : "");
            user.setLastName(names.length > 1 ? names[1] : "");
        }
        
        // Si firstName y lastName vienen directamente, usarlos
        if (registerRequest.getFirstName() != null) {
            user.setFirstName(registerRequest.getFirstName());
        }
        if (registerRequest.getLastName() != null) {
            user.setLastName(registerRequest.getLastName());
        }

        // Campos adicionales de información del usuario
        if (registerRequest.getCareer() != null && !registerRequest.getCareer().trim().isEmpty()) {
            user.setCareer(registerRequest.getCareer().trim());
        }
        if (registerRequest.getUniversity() != null && !registerRequest.getUniversity().trim().isEmpty()) {
            user.setUniversity(registerRequest.getUniversity().trim());
        }
        if (registerRequest.getSemester() != null) {
            user.setSemester(registerRequest.getSemester());
        }
        if (registerRequest.getBirthDate() != null) {
            user.setBirthDate(registerRequest.getBirthDate());
        }
        if (registerRequest.getGender() != null && !registerRequest.getGender().trim().isEmpty()) {
            user.setGender(registerRequest.getGender().trim());
        }

        // Rol por defecto: STUDENT
        Role defaultRole = roleRepository.findByName(Role.ERole.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Rol STUDENT no existe en la BD"));

        user.setRoles(Set.of(defaultRole));

        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // ================= ACTUALIZAR CAMPOS ===================

    @Override
    public User updateUser(String username, Map<String, String> updates) {

        User user = findByUsernameOrEmail(username);

        updates.forEach((key, value) -> {
            if (value == null || value.trim().isEmpty()) {
                return; // Saltar valores vacíos
            }
            
            switch (key) {
                case "email" -> user.setEmail(value.trim());
                case "firstName" -> user.setFirstName(value.trim());
                case "lastName" -> user.setLastName(value.trim());
                case "password" -> {
                    if (value.length() >= 6) {
                        user.setPassword(passwordEncoder.encode(value));
                    }
                }
                case "career" -> user.setCareer(value.trim());
                case "university" -> user.setUniversity(value.trim());
                case "semester" -> {
                    try {
                        Integer semester = Integer.parseInt(value);
                        if (semester > 0) {
                            user.setSemester(semester);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar si no es un número válido
                    }
                }
                case "birthDate" -> {
                    try {
                        LocalDate birthDate = LocalDate.parse(value);
                        user.setBirthDate(birthDate);
                    } catch (Exception e) {
                        // Ignorar si no es una fecha válida
                    }
                }
                case "gender" -> user.setGender(value.trim());
            }
        });

        return userRepository.save(user);
    }

    // ================= CONTADORES ===================

    @Override
    public long countAllUsers() { return userRepository.count(); }

    @Override
    public long countActiveUsers() { return userRepository.countByEnabled(true); }

    @Override
    public long countByRole(String role) {
        return findAll().stream()
                .filter(user -> getUserRole(user).equals(role))
                .count();
    }

    @Override
    public long countTodayRegistrations() {
        LocalDate today = LocalDate.now();
        return userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null &&
                             u.getCreatedAt().toLocalDate().equals(today))
                .count();
    }

    // ================= ROLES ===================

    @Override
    public User updateRole(Long userId, String role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // role → "admin", "student", "moderator"
        String normalized = "ROLE_" + role.toUpperCase();

        Role.ERole enumRole = Role.ERole.valueOf(normalized);

        Role newRole = roleRepository.findByName(enumRole)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + normalized));

        user.setRoles(Set.of(newRole));

        return userRepository.save(user);
    }

    @Override
    public User updateStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setEnabled(active);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) { userRepository.deleteById(userId); }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private String getUserRole(User user) {
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            return user.getRoles().iterator().next().getName().name();
        }
        return "ROLE_USER";
    }
    
    // ================= GESTIÓN DE INTERESES ===================
    
    @Override
    public List<Interest> getUserInterests(String username) {
        User user = findByUsernameOrEmail(username);
        return new ArrayList<>(user.getInterests());
    }
    
    @Override
    public User addInterestToUser(String username, Long interestId) {
        User user = findByUsernameOrEmail(username);
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new RuntimeException("Interés no encontrado"));
        
        if (!user.getInterests().contains(interest)) {
            user.getInterests().add(interest);
            return userRepository.save(user);
        }
        return user;
    }
    
    @Override
    public User removeInterestFromUser(String username, Long interestId) {
        User user = findByUsernameOrEmail(username);
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new RuntimeException("Interés no encontrado"));
        
        user.getInterests().remove(interest);
        return userRepository.save(user);
    }
    
    @Override
    public List<Interest> getAvailableInterests(String username) {
        User user = findByUsernameOrEmail(username);
        return interestRepository.findAvailableInterestsForUser(user.getId());
    }
}
