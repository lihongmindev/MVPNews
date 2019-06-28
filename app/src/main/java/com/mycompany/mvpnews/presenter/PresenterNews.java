package com.mycompany.mvpnews.presenter;

import android.app.Activity;
import android.util.Log;

import com.mycompany.mvpnews.MainActivity;
import com.mycompany.mvpnews.bean.RecyclerList;
import com.mycompany.mvpnews.gson.LatestNews;
import com.mycompany.mvpnews.model.IModelNews;
import com.mycompany.mvpnews.model.ModelNews;
import com.mycompany.mvpnews.model.IRequestCallback;
import com.mycompany.mvpnews.view.IViewNews;

import java.util.List;

public class PresenterNews implements IRequestCallback{
    private IViewNews mIViewNews;
    private IModelNews mIModelNews;

    public PresenterNews(IViewNews mIViewNews){
        this.mIViewNews = mIViewNews;    //mIViewNews这个对象操作的是传过来的MainActivity的view
        mIModelNews = new ModelNews();   //mIModelNews这个对象使用ModelNews类中的接口方法
    }

    public void readNews(String date){
        mIModelNews.readNews(date,this);

    }
    public void readNews(){
        mIModelNews.readNews(null,this);


    }
    public void downloadNews(Activity activity){
        mIModelNews.downloadNews(activity,this);
    }

    @Override
    public void requestCallback(List<RecyclerList> allRecycler) {
        if (allRecycler != null){
            Log.d("mvp1","拿到allRecycler");
            mIViewNews.setNews(allRecycler);
        }else {
            mIViewNews.error("没有找到");
        }
    }

    @Override
    public void downloadProgressCallback(String progress) {
        mIViewNews.setDownLoadProgress(progress);
    }


}
