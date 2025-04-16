package com.example.biwooda.kakaoPay.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoReturnResponse extends KakaoApproveResponse{
    private int currentNum;
    private String lockerCode;

    public KakaoReturnResponse(){

    }
    public KakaoReturnResponse(KakaoApproveResponse approveResponse) {
        this.setAmount(approveResponse.getAmount());
        this.setSid(approveResponse.getSid());
        this.setTid(approveResponse.getTid());
        this.setApproved_at(approveResponse.getApproved_at());
        this.setCreated_at(approveResponse.getCreated_at());
        this.setItem_name(approveResponse.getItem_name());
    }

    public void setCurrentNum(int currentNum){
        this.currentNum = currentNum;
    }
    public void setLockerCode(String lockerCode){
        this.lockerCode = lockerCode;
    }
}
