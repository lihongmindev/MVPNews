package com.mycompany.mvpnews.view;

import com.mycompany.mvpnews.bean.RecyclerList;

import java.util.List;

public interface IViewNews {

    void setNews(List<RecyclerList> allRecycler);
    void setDownLoadProgress(String progress);
    void error(String str);

}
