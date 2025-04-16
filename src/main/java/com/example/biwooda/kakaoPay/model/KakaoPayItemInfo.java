package com.example.biwooda.kakaoPay.model;

import lombok.Getter;

//클라이언트로부터 받아오는 상품 정보
@Getter
public class KakaoPayItemInfo {
    private String lockerCode;
    private int price;
    private String itemName;
    private int num;

    public String getLockerCode() { return lockerCode; }
    public int getNum() {
        return num;
    }
    public int getPrice() {
        return price;
    }
    public String getItemName(){
        return itemName;
    }
}
