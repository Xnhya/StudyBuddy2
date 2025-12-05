package com.studybuddy.security;

import com.studybuddy.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Usa los roles del User que ya están implementados en el modelo
        // El User.getAuthorities() ya convierte los roles correctamente
        return user.getAuthorities();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Cambia según tu lógica de negocio
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Cambia según tu lógica de negocio
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Cambia según tu lógica de negocio
    }

    @Override
    public boolean isEnabled() {
        return ((UserDetails) user).isEnabled(); // Asumiendo que tu User tiene este campo
    }

    // Métodos adicionales útiles
    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public Long getId() {
        return user.getId();
    }
}