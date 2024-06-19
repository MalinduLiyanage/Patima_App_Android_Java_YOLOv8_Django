package com.onesandzeros.patima.authentication.models;

public class LoginResponse {
    private String status;
    private String message;

    private Number role;
    private Token token;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Number getRole() {
        return role;
    }

    public void setRole(Number role) {
        this.role = role;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

}
