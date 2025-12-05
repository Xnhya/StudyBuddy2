package com.studybuddy.security;

import com.studybuddy.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Puedes buscar por username o email
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username)) // Busca también por email
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado con nombre de usuario o email: " + username
                ));
    }

    // Método adicional para cargar usuario por ID
    @Transactional
    public UserDetails loadUserById(Long id) {
        return userRepository.findById(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado con ID: " + id
                ));
    }
}