package com.mycompany.mvpnews.model;

import android.app.Activity;

public interface IModelNews {
    void readNews(String date,IRequestCallback iRequestCallback);
    void downloadNews(Activity activity,IRequestCallback iRequestCallback);


}
