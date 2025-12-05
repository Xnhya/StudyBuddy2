package com.studybuddy.dto;

public class CurrencyDTO {
    private String from;
    private String to;
    private Double amount;
    private Double result;
    private Double rate;
    private boolean success;
    private String error;
    
    // Constructor vacío
    public CurrencyDTO() {}
    
    // Constructor para respuesta exitosa
    public CurrencyDTO(String from, String to, Double amount, Double result, Double rate) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.result = result;
        this.rate = rate;
        this.success = true;
    }
    
    // Constructor para error
    public CurrencyDTO(String from, String to, Double amount, String error) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.error = error;
        this.success = false;
    }
    
    // Getters y Setters
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public Double getResult() {
        return result;
    }
    
    public void setResult(Double result) {
        this.result = result;
    }
    
    public Double getRate() {
        return rate;
    }
    
    public void setRate(Double rate) {
        this.rate = rate;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}