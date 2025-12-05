package com.studybuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dni_info",
       indexes = {
           @Index(name = "idx_dni_number", columnList = "dni_number", unique = true),
           @Index(name = "idx_dni_consulted", columnList = "consulted_at"),
           @Index(name = "idx_dni_last_name", columnList = "last_name")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DniInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dni_number", nullable = false, unique = true, length = 8)
    private String dniNumber;
    
    @Column(name = "full_name", length = 200)
    private String fullName;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Column(name = "mother_last_name", length = 100)
    private String motherLastName;
    
    @Column(name = "second_name", length = 100)
    private String secondName;
    
    @Column(name = "gender", length = 20)
    private String gender;
    
    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;
    
    @Column(name = "voting_site", length = 200)
    private String votingSite;
    
    @Column(name = "voting_address", length = 500)
    private String votingAddress;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "province", length = 100)
    private String province;
    
    @Column(name = "district", length = 100)
    private String district;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "consulted_at")
    private java.time.LocalDateTime consultedAt;
    
    @Column(name = "consultation_count")
    private Integer consultationCount = 1;
    
    @Column(name = "source", length = 50)
    private String source = "apiperu";
    
    @Column(name = "is_valid")
    private Boolean isValid = true;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "last_verified")
    private java.time.LocalDateTime lastVerified;
    
    @PrePersist
    protected void onCreate() {
        consultedAt = java.time.LocalDateTime.now();
        if (consultationCount == null) consultationCount = 1;
        if (source == null) source = "apiperu";
        if (isValid == null) isValid = true;
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastVerified = java.time.LocalDateTime.now();
    }
    
    // ========== MÉTODOS HELPER ==========
    
    // Incrementar contador de consultas
    public void incrementConsultationCount() {
        if (consultationCount == null) {
            consultationCount = 0;
        }
        consultationCount++;
        consultedAt = java.time.LocalDateTime.now();
    }
    
    // Obtener edad calculada
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        
        java.time.LocalDate now = java.time.LocalDate.now();
        int age = now.getYear() - birthDate.getYear();
        
        // Ajustar si aún no ha pasado el cumpleaños este año
        if (now.getMonthValue() < birthDate.getMonthValue() ||
            (now.getMonthValue() == birthDate.getMonthValue() && 
             now.getDayOfMonth() < birthDate.getDayOfMonth())) {
            age--;
        }
        
        return age;
    }
    
    // Obtener nombre completo formateado
    public String getFormattedFullName() {
        StringBuilder sb = new StringBuilder();
        
        if (firstName != null) {
            sb.append(firstName);
        }
        if (secondName != null && !secondName.isEmpty()) {
            sb.append(" ").append(secondName);
        }
        if (lastName != null) {
            sb.append(" ").append(lastName);
        }
        if (motherLastName != null && !motherLastName.isEmpty()) {
            sb.append(" ").append(motherLastName);
        }
        
        return sb.toString().trim();
    }
    
    // Obtener ubicación completa
    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        
        if (department != null) {
            sb.append(department);
        }
        if (province != null && !province.equals(department)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(province);
        }
        if (district != null && !district.equals(province)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        
        return sb.toString();
    }
    
    // Verificar si es mayor de edad
    public boolean isAdult() {
        Integer age = getAge();
        return age != null && age >= 18;
    }
    
    // Obtener género completo
    public String getGenderFull() {
        if (gender == null) return "No especificado";
        
        return switch (gender.toUpperCase()) {
            case "M" -> "Masculino";
            case "F" -> "Femenino";
            case "MASCULINO" -> "Masculino";
            case "FEMENINO" -> "Femenino";
            default -> gender;
        };
    }
    
    // Obtener icono de género
    public String getGenderIcon() {
        if (gender == null) return "fas fa-user";
        
        return switch (gender.toUpperCase()) {
            case "M", "MASCULINO" -> "fas fa-mars";
            case "F", "FEMENINO" -> "fas fa-venus";
            default -> "fas fa-user";
        };
    }
    
    // Obtener color de género
    public String getGenderColor() {
        if (gender == null) return "secondary";
        
        return switch (gender.toUpperCase()) {
            case "M", "MASCULINO" -> "primary";
            case "F", "FEMENINO" -> "danger";
            default -> "secondary";
        };
    }
    
    // Verificar si la información está completa
    public boolean isComplete() {
        return dniNumber != null && 
               firstName != null && 
               lastName != null && 
               fullName != null;
    }
    
    // Obtener DNI formateado (con puntos)
    public String getFormattedDni() {
        if (dniNumber == null || dniNumber.length() != 8) {
            return dniNumber;
        }
        
        return dniNumber.substring(0, 2) + "." + 
               dniNumber.substring(2, 5) + "." + 
               dniNumber.substring(5, 8);
    }
    
    // Obtener fecha de nacimiento formateada
    public String getFormattedBirthDate() {
        if (birthDate == null) {
            return "No disponible";
        }
        
        return birthDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    // Marcar como inválido
    public void markAsInvalid(String error) {
        this.isValid = false;
        this.errorMessage = error;
        this.lastVerified = java.time.LocalDateTime.now();
    }
    
    // Verificar si es una consulta reciente (menos de 30 días)
    public boolean isRecentConsultation() {
        if (consultedAt == null) return false;
        
        java.time.Duration duration = java.time.Duration.between(
            consultedAt, java.time.LocalDateTime.now()
        );
        
        return duration.toDays() < 30;
    }
}