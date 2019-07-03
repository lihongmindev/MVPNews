package com.mycompany.mvpnews.model;

import android.util.Log;

import com.mycompany.mvpnews.bean.News;
import com.mycompany.mvpnews.bean.RecyclerList;
import com.mycompany.mvpnews.gson.LatestNews;
import com.mycompany.mvpnews.gson.LatestStories;
import com.mycompany.mvpnews.gson.Top_Stories;
import com.mycompany.mvpnews.util.HttpUtil;
import com.mycompany.mvpnews.util.TimeUtil;
import com.mycompany.mvpnews.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ReadOnLine implements IReadBehavior {

    private List<News> rlatestNews = new ArrayList<>();
    private List<News> rbeforeNews = new ArrayList<>();
    private List<News> rtopNews = new ArrayList<>();
    private List<RecyclerList> allRecycler = new ArrayList<>();
    private final int Head_Type = 0;
    private final int date_Type = 1;
    private final int New_Type = 2;
    private int m = 0;

    @Override
    public void read(String date,IRequestCallback iRequestCallback) {
        if (date == null){
            requestLatestNews(iRequestCallback);   //请求顶部新闻和今日新闻
            allRecycler.clear();      //将数组中存储的数据清空
            rbeforeNews.clear();
            rlatestNews.clear();
            rtopNews.clear();
            m = 0;
        }else {
            requestNews(date,iRequestCallback);    //请求指定日期新闻
        }
    }

    @Override
    public boolean isOnLineBehavior() {
        return true;
    }

    /**
     * 请求LastestNews信息
     */
    public void requestLatestNews(final IRequestCallback iRequestCallback) {
        String latestNewsUrl = "https://news-at.zhihu.com/api/4/news/latest";
        HttpUtil.sendOkHttpRequest(latestNewsUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();                  //获得原始数据
                final LatestNews latestNews = Utility.handleLatestNewsResponse(responseText);   //解析数据
                if (latestNews != null) {   //判断请求信息是否成功
                    //将最近最新消息显示出来
                    showLatestNewsInfo(latestNews,iRequestCallback);
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * 请求BeforeNews信息
     */
    private void requestNews(final String nextdate,final IRequestCallback iRequestCallback) {
        Log.d("mvp1","开始请求"+ nextdate +"新闻");
        String latestNewsUrl = "https://news-at.zhihu.com/api/4/news/before/" + nextdate;
        HttpUtil.sendOkHttpRequest(latestNewsUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("mvp1","请求到"+ nextdate +"新闻");
                final String responseText = response.body().string();                  //获得原始数据
                final LatestNews latestNews = Utility.handleLatestNewsResponse(responseText);   //解析数据
                if (latestNews != null) {   //判断请求信息是否成功
                    Log.d("mvp1","解析成功");
                    showBeforeNewsInfo(latestNews,iRequestCallback);
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }
        });
    }
    /**
     * 处理并展示LatestNews实体类中的数据
     */
    public void showLatestNewsInfo(LatestNews latestNews,IRequestCallback iRequestCallback) {

        String LatestDate = latestNews.date;     //最新日期

        int i = 0;
        int j = 0;
        for (Top_Stories topStories : latestNews.LatestTopStoriesList) {
            String newid = topStories.newId_top;
            String topNewTitle = topStories.newTitle_top;    //Top新闻题目
            String topNewImage = topStories.newImage_top;    //Top新闻图片
            News topnews = new News(LatestDate, newid, topNewTitle, topNewImage);
            rtopNews.add(j++, topnews);
        }
        RecyclerList head = new RecyclerList(Head_Type, "", "", "", "", rtopNews);
        allRecycler.add(m++, head);

        RecyclerList date = new RecyclerList(date_Type, LatestDate, "", "", "", rtopNews);
        allRecycler.add(m++, date);
        for (LatestStories latestStories : latestNews.LatestStoriesList) {
            String newtitle = latestStories.newTitle;    //最近新闻题目
            String newid = latestStories.newId;           //最近新闻ID
            for (String newsImage : latestStories.NewsImageList) {
                News test = new News(LatestDate, newid, newtitle, newsImage);
                rlatestNews.add(i++, test);
                RecyclerList news = new RecyclerList(New_Type, LatestDate, newid, newtitle, newsImage, rlatestNews);
                allRecycler.add(m++, news);
            }
        }
        iRequestCallback.requestCallback(allRecycler);
        /*
        防止出现最新的新闻条数太少无法充满首页的情况
         */
        requestNews(TimeUtil.getToday(),iRequestCallback);    //请求指定日期的新闻

    }
    /**
     * 处理并展示BeforeNews实体类中的数据
     */
    public void showBeforeNewsInfo(LatestNews latestNews,IRequestCallback iRequestCallback) {
        Log.d("mvp1","开始封装RecyclerList");
        String LatestDate = latestNews.date;     //最新日期
        int i = 0;
        News test;
        RecyclerList date = new RecyclerList(date_Type, LatestDate, "", "", "", rtopNews);
        allRecycler.add(m++, date);

        for (LatestStories latestStories : latestNews.LatestStoriesList) {
            String newtitle = latestStories.newTitle;    //最近新闻题目
            String newid = latestStories.newId;           //最近新闻ID
            for (String newsImage : latestStories.NewsImageList) {
                test = new News(LatestDate, newid, newtitle, newsImage);   //最近新闻图片
                rbeforeNews.add(i++, test);
                RecyclerList news = new RecyclerList(New_Type, LatestDate, newid, newtitle, newsImage, rbeforeNews);
                allRecycler.add(m++, news);
            }
        }
        iRequestCallback.requestCallback(allRecycler);
    }
}
