package com.studybuddy.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String firstName;
    private String lastName;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    
    private boolean enabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Campos opcionales
    private String career;
    private String university;
    private Integer semester;
    private Boolean emailVerified = false;
    
    // CORRECCIÓN FINAL: Agregamos birthDate y gender
    private LocalDate birthDate;
    
    @Column(length = 20)
    private String gender;
    
    // 2FA (Autenticación de Dos Factores)
    private String otpCode;
    private LocalDateTime otpExpiry;
    private Boolean twoFactorEnabled = false;
    
    @ManyToMany
    @JoinTable(name = "user_interests",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private Set<Interest> interests = new HashSet<>();
    
    // ========== UserDetails METHODS ==========
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getPassword() { return password; }
    
    @Override
    public String getUsername() { return username; }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return enabled; }
    
    // ========== GETTERS Y SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public String getCareer() { return career; }
    public void setCareer(String career) { this.career = career; }
    
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Set<Interest> getInterests() { return interests; }
    public void setInterests(Set<Interest> interests) { this.interests = interests; }
    
    // Getters y setters para 2FA
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    
    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }
    
    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    
    // Métodos auxiliares
    public String getFullName() {
        return (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
    }
    
    public String getPrimaryRole() {
        if (roles != null && !roles.isEmpty()) {
            // Retorna el nombre del primer rol encontrado (ej: ROLE_ADMIN)
            return roles.iterator().next().getName().name();
        }
        return "ROLE_USER"; // Rol por defecto si no tiene ninguno
    }
    
    public String getRole() {
        return getPrimaryRole();
    }
}