package com.example.biwooda.kakaoPay.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoCancelResponse {
    private String tid;
    private String status;
    private ApprovedCancelAmount approved_cancel_amount;
    private String canceled_at;

    public String getTid() {
        return tid;
    }
}
