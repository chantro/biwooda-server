package com.example.biwooda.umbrella.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UmbrellaLocker {
    private String locker_code;
    private int current_num;
    private String site;
    private String status;

    public String getLocker_code() { return locker_code; }
    public int getCurrent_num() { return current_num; }
    public String getSite() { return site; }
    public String getStatus() { return status; }
}
