package com.example.biwooda.kakaoPay.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoInactiveResponse {
    private String cid;
    private String sid;
    private String status;  //결제 상태, ACTIVE(활성) 또는 INACTIVE(비활성) 중 하나
    private  String created_at;  //SID 발급 시각
    private String last_approved_at;   //마지막 결제승인 시각
    private String inactivated_at;    //정기 결제 비활성화 시각

}
