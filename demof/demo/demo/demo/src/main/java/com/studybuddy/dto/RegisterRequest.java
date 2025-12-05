package com.studybuddy.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {

    private String fullName;
    private String username;
    private String email;
    private String password;
    private String userType;

    private String firstName;
    private String lastName;
    
    // Campos adicionales para información del usuario
    private String career;
    private String university;
    private Integer semester;
    private LocalDate birthDate;
    private String gender;
}
