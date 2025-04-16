package com.example.biwooda.login.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordResponse {
    private String status;
    private String message;

    public ResetPasswordResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
