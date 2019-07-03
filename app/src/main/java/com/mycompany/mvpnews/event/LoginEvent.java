package com.mycompany.mvpnews.event;

public class LoginEvent {
    private String messeage;

    public LoginEvent(String messeage) {
        this.messeage = messeage;
    }

    public String getMessage() {
        return messeage;
    }
}
