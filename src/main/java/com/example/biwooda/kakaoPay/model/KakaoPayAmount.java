package com.example.biwooda.kakaoPay.model;

import lombok.Getter;

//approve request를 보냈을 때 얻는 response
@Getter
public class KakaoPayAmount {
    private int total; // 총 결제 금액
    private int taxFree; // 비과세 금액
    private int tax; // 부가세 금액

    public int getTotal(){
        return total;
    }

    public void setTotal(int total){
        this.total = total;
    }
    public void setTax(int tax){
        this.tax = tax;
    }
    public void setTaxFree(int taxFree){
        this.taxFree =taxFree;
    }
}
