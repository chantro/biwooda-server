package com.example.biwooda.kakaoPay.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

//카카오페이 api에 approve request를 보냈을 때 받는 reponse
@Setter
@Getter
public class KakaoApproveResponse {
    private String tid;     //결제 고유 번호
    private String sid;    //정기결제를 위한 고유 번호
    private KakaoPayAmount amount; // 결제 금액 정보
    private String item_name; // 상품명
    private String created_at; // 결제 요청 시간
    private String approved_at; // 결제 승인 시간


    public String getTid(){ return tid; }
    public String getSid() { return sid; }

    public KakaoPayAmount getAmount(){
        return amount;
    }
    public String getCreated_at(){
        return created_at;
    }

    public String getApproved_at(){
        return approved_at;
    }

    public String getItem_name(){
        return item_name;
    }

    public void setTid(String tid){
        this.tid = tid;
    }
    public void setSid(String sid){
        this.sid = sid;
    }
    public void setAmount(KakaoPayAmount amount){
        this.amount = amount;
    }
    public void setApproved_at(String approvedAt){
        this.approved_at = approvedAt;
    }
    public void setItem_name(String itemName){
        this.item_name = itemName;
    }
    public void setCreated_at(String created_at){
        this.created_at = created_at;
    }
}
