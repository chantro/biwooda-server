package com.example.biwooda.kakaoPay.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//카카오페이 api에 request를 보내면 얻는 데이터
@Getter
@Setter
@ToString
public class KakaoReadyResponse {
    private String tid;   //결제 고유 번호
    private String next_redirect_mobile_url; // 모바일 웹일 경우 받는 결제 페이지
    private String next_redirect_pc_url; //PC 웹일 경우 받는 결제  페이지
    private String created_at;

    public String getTid(){
        return tid;
    }

    public String getNext_redirect_mobile_url(){
        return next_redirect_mobile_url;
    }

    public String getNext_redirect_pc_url(){
        return next_redirect_pc_url;
    }

    public String getCreated_at(){
        return created_at;
    }
}
