package com.onesandzeros.patima.messages.model;

public class Message {
    private String name;
    private String email;
    private String message;

    public Message(String name, String email, String message) {
        this.name = name;
        this.email = email;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }
}
