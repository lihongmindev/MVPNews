package com.mycompany.mvpnews.model;

public interface IReadBehavior {
    void read(String date,IRequestCallback iRequestCallback);
}
