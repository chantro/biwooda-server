package com.example.biwooda.kakaoPay.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoApproveRequest {
    String lockerCode;  //우산함 코드
    String pgToken;  //결제 승인을 위한 일회성 토큰 (ready의 리다이렉트 결과)

    public String getLockerCode() {
        return lockerCode;
    }

    public String getPgToken() {
        return pgToken;
    }

}
