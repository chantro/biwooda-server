package com.example.biwooda.kakaoPay.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;

@Getter
//카카오서버에 보낼 요청
public class KakaoPayRequest {
    private String url;   //카카오 api url
    private LinkedMultiValueMap<String,String> parameters;   //요청에 담을 내용

    public KakaoPayRequest(String url, LinkedMultiValueMap<String, String> parameters) {
        this.url = url;
        this.parameters = parameters;
    }

    public String getUrl(){
        return url;
    }

    public LinkedMultiValueMap<String,String> getParameters(){
        return parameters;
    }
}
