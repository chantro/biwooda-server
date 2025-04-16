package com.example.biwooda.login.model;

public class IdTokenResponse {
    private final String success;
    private final String msg;

    public IdTokenResponse(String success, String msg){
        super();
        this.success = success;
        this.msg = msg;
    }

    public String getSuccess() {
        return success;
    }

    public String getMsg(){
        return msg;
    }
}
