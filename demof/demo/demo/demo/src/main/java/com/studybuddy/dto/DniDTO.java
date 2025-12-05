package com.studybuddy.dto;

public class DniDTO {
    private String dni;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String error;
    private boolean success;
    
    public DniDTO() {}
    
    public DniDTO(String dni, String error) {
        this.dni = dni;
        this.error = error;
        this.success = false;
    }
    
    // Constructor para éxito
    public DniDTO(String dni, String nombres, String apellidoPaterno, String apellidoMaterno) {
        this.dni = dni;
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.success = true;
    }
    
    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}