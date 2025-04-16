package com.example.biwooda.kakaoPay.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoBaseResponse{
    private int status;
    private String message;

    public KakaoBaseResponse(int value, String message) {
        this.message = message;
        this.status = value;
    }
}
