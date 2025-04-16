package com.example.biwooda.login.model;

public class EmailAuthResponse {
    private String token;
    private String message;

    public EmailAuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
}