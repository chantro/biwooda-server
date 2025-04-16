package com.example.biwooda.login.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String idToken = null;
    private Boolean rentalState = false;
    private String message = null;
    private Ticket ticket = null;

    public void setIdToken(String idToken){
        this.idToken = idToken;
    }

    public void setRentalState(Boolean rentalState){
        this.rentalState = rentalState;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setTicket(Ticket ticket){
        this.ticket = ticket;
    }

}
