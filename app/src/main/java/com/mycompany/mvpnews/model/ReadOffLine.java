package com.mycompany.mvpnews.model;

import android.util.Log;

import com.mycompany.mvpnews.bean.News;
import com.mycompany.mvpnews.bean.RecyclerList;
import com.mycompany.mvpnews.db.BeforeNews;
import com.mycompany.mvpnews.db.TodayNews;
import com.mycompany.mvpnews.db.TopNews;
import com.mycompany.mvpnews.util.TimeUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.valueOf;
import static java.lang.String.format;

public class ReadOffLine implements IReadBehavior {

    /**
     * TopNews列表
     */
    private List<TopNews> topNewsList;
    /**
     * TodayNews列表
     */
    private List<TodayNews> todayNewsList;
    /**
     * BeforeNews列表
     */
    private List<BeforeNews> beforeNewsList;
    private List<News> rlatestNews = new ArrayList<>();
    private List<News> rbeforeNews = new ArrayList<>();
    private List<News> rtopNews = new ArrayList<>();
    private List<RecyclerList> allRecycler = new ArrayList<>();
    private final int Head_Type = 0;
    private final int date_Type = 1;
    private final int New_Type = 2;
    private int m = 0;
    private int Nextdate;
    @Override
    public void read(String date, IRequestCallback iRequestCallback) {
        topNewsList = DataSupport.findAll(TopNews.class);
        todayNewsList = DataSupport.findAll(TodayNews.class);
        beforeNewsList = DataSupport.findAll(BeforeNews.class);
        if (topNewsList.size() > 0){
            allRecycler.clear();      //将数组中存储的数据清空
            rbeforeNews.clear();
            rlatestNews.clear();
            rtopNews.clear();
            m = 0;
            requestDataNews(TimeUtil.getToday(),iRequestCallback);   //请求顶部新闻和今日新闻
        }
    }

    public void requestDataNews(String today, IRequestCallback iRequestCallback){
        int j = 0;
        int i = 0;
        int n = 0;
        for (TopNews topnews : topNewsList) {
            News topNews = new News("", topnews.getNewId(), topnews.getNewTitle(), topnews.getNewImage());
            rtopNews.add(j++, topNews);
        }
        //TopNews
        RecyclerList head = new RecyclerList(Head_Type, "", "", "", "", rtopNews);
        allRecycler.add(m++, head);
        Log.d("首页新闻 ", "添加顶部新闻到allRecycler");

        //TodayDate
        RecyclerList date = new RecyclerList(date_Type, today, "", "", "", rtopNews);
        allRecycler.add(m++, date);
        Log.d("首页新闻 ", "添加今天的日期到allRecycler");
        for (TodayNews todaynews : todayNewsList) {
            News test = new News(today, todaynews.getNewId(), todaynews.getNewTitle(), todaynews.getNewImage());
            rlatestNews.add(i++, test);
            //TodayNews
            RecyclerList news = new RecyclerList(New_Type, todaynews.getDate(), todaynews.getNewId(), todaynews.getNewTitle(), todaynews.getNewImage(), rlatestNews);
            allRecycler.add(m++, news);
            Log.d("首页新闻 ", "添加今日新闻到allRecycler");
        }

        Date date1 = new Date(); // 新建一个日期
        Calendar c = Calendar.getInstance(); // 默认得到的是当前的日期
        c.setTime(date1);
        for (int k=1;k<50;k++){                    //将50天内的离线下载新闻显示
            c.set(Calendar.DATE, c.get(Calendar.DATE) - 1);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            String lastDate = format(Locale.CHINA, "%s%02d%02d", year, month, day);  //昨天的日期
            List<BeforeNews> lastDayNews = DataSupport.where("date = ?", lastDate).find(BeforeNews.class);

            if (lastDayNews.size()>0){
                Nextdate = valueOf(lastDate);     //得到最后有新闻的一天，用于滑到recyclerView底部使用
                //LastDate
                RecyclerList date2 = new RecyclerList(date_Type, lastDate, "", "", "", rtopNews);
                allRecycler.add(m++, date2);
                Log.d("首页新闻 ", "添加昨天的日期到allRecycler");
                for (BeforeNews lastdaynews : lastDayNews) {
                    News test = new News(lastdaynews.getDate(), lastdaynews.getNewId(), lastdaynews.getNewTitle(), lastdaynews.getNewImage());
                    rbeforeNews.add(n++, test);
                    RecyclerList news = new RecyclerList(New_Type, lastdaynews.getDate(), lastdaynews.getNewId(), lastdaynews.getNewTitle(), lastdaynews.getNewImage(), rbeforeNews);
                    allRecycler.add(m++, news);
                    Log.d("首页新闻 ", "添加昨天的新闻到allRecycler");
                }
            }
        }
        iRequestCallback.requestCallback(allRecycler);
    }
}
