package com.example.biwooda.login.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailSignupRequest {
    private String email;
    private String pwd;

    public String getPwd(){
        return pwd;
    }

    public String getEmail(){
        return email;
    }
}
