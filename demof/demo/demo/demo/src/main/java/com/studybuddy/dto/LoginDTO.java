package com.studybuddy.dto;

public class LoginDTO {
    private String usernameOrEmail;
    private String password;
    
    // Constructor vacío
    public LoginDTO() {}
    
    // Constructor con parámetros
    public LoginDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
    
    // Getters y Setters
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}