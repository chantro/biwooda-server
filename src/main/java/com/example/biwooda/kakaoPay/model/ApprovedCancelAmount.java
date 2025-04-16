package com.example.biwooda.kakaoPay.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovedCancelAmount {
    private int total;  //이번 요청으로 취소된 전체 결제 금액
    private int tax_free;  //이번 요청으로 취소된 비과세 금액
    private int vat;  //이번 요청으로 취소된 부과세 금액
}
