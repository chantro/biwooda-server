package com.example.biwooda.login.model;

import lombok.Data;

@Data
public class EmailVerifyResponse {
    private String email;
    private String verifyCode;
    private String message;

    public EmailVerifyResponse(String email, String verifyCode, String message) {
        this.email = email;
        this.verifyCode = verifyCode;
        this.message = message;
    }
}
