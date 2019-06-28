package com.mycompany.mvpnews.model;

import com.mycompany.mvpnews.bean.RecyclerList;

import java.util.List;

public interface IRequestCallback {

    void requestCallback(List<RecyclerList> allRecycler);
    void downloadProgressCallback(String progress);
}
