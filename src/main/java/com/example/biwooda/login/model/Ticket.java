package com.example.biwooda.login.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticket {
    private String startDate;
    private String endDate;

    private String itemName;

    public Ticket(String startDate, String endDate, String itemName){
        this.startDate = startDate;
        this.endDate = endDate;
        this.itemName = itemName;
    }
}
