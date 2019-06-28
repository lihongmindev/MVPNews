package com.mycompany.mvpnews.model;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.mycompany.mvpnews.MainActivity;
import com.mycompany.mvpnews.R;
import com.mycompany.mvpnews.bean.News;
import com.mycompany.mvpnews.bean.RecyclerList;
import com.mycompany.mvpnews.gson.LatestNews;
import com.mycompany.mvpnews.gson.LatestStories;
import com.mycompany.mvpnews.gson.Top_Stories;
import com.mycompany.mvpnews.presenter.PresenterNews;
import com.mycompany.mvpnews.service.OffLineDownLoad;
import com.mycompany.mvpnews.util.HttpUtil;
import com.mycompany.mvpnews.util.TimeUtil;
import com.mycompany.mvpnews.util.Utility;

import org.litepal.LitePalApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.BIND_AUTO_CREATE;
import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static org.litepal.LitePalApplication.getContext;


public class ModelNews implements IModelNews {

    private List<News> rlatestNews = new ArrayList<>();
    private List<News> rbeforeNews = new ArrayList<>();
    private List<News> rtopNews = new ArrayList<>();
    private List<RecyclerList> allRecycler = new ArrayList<>();
    private final int Head_Type = 0;
    private final int date_Type = 1;
    private final int New_Type = 2;
    private int m = 0;
    private IReadBehavior iReadBehavior;
    private Timer timer;
    private int downloadprogress = 0;
    private Handler handler;

    private OffLineDownLoad.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        //这两个方法在活动与服务绑定和解除绑定时调用
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (OffLineDownLoad.DownloadBinder) service;  //可以在活动中调用DownloadBinder的任何public方法
            // 初始化定时器
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d("bindService ", "开启定时器");
                    downloadprogress = downloadBinder.getProgress();
                    Message message = new Message();
                    message.what = downloadprogress;
                    handler.sendMessage(message);
                    Log.d("bindService ", "downloadprogress " + downloadprogress);
                }
            }, 0, 200);   //200ms
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void readNews(String date,IRequestCallback iRequestCallback) {
        if (isNetworkAvailable(getContext())){
            if (iReadBehavior == null){                //保证只new出来一个iReadBehavior
                iReadBehavior = new ReadOnLine();
            }
            iReadBehavior.read(date,iRequestCallback);
        }else {                                         //不需要保证只new出来一个iReadBehavior
            iReadBehavior = new ReadOffLine();
            iReadBehavior.read(date,iRequestCallback);
        }
        /*
        if (date == null){
            allRecycler.clear();      //将数组中存储的数据清空
            rbeforeNews.clear();
            rlatestNews.clear();
            rtopNews.clear();
            m = 0;
            requestLatestNews(iRequestCallback);   //请求顶部新闻和今日新闻
        }else {
            requestNews(date,iRequestCallback);    //请求指定日期新闻
        }  */
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void downloadNews(final Activity activity, final IRequestCallback iRequestCallback) {
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != 100) {
                    if (msg.what == 101){
                        Toast.makeText(activity, getContext().getResources().getString(R.string.latest_news_download_fail), Toast.LENGTH_SHORT).show();
                        timer.cancel();                                  //Timer停止
                        timer = null;
                        Log.d("bindService ", "关闭定时器");
                        activity.unbindService(connection);              //对服务进行解绑，否则会造成内存泄漏
                        iRequestCallback.downloadProgressCallback(getContext().getResources().getString(R.string.fail));
                    }else {
                        iRequestCallback.downloadProgressCallback(String.format("%s%s", String.valueOf(msg.what), getContext().getResources().getString(R.string.baifenhao)));
                    }
                } else {
                    iRequestCallback.downloadProgressCallback(getContext().getResources().getString(R.string.complete));
                    Toast.makeText(activity, getContext().getResources().getString(R.string.download_success), Toast.LENGTH_SHORT).show();
                    timer.cancel();                        //Timer停止
                    timer = null;
                    Log.d("bindService ", "关闭定时器");
                    activity.unbindService(connection);              //对服务进行解绑，否则会造成内存泄漏
                }
            }
        };
        Toast.makeText(activity, "开始下载", Toast.LENGTH_SHORT).show();
        Intent bindIntent = new Intent(activity, OffLineDownLoad.class);
        activity.bindService(bindIntent, connection, BIND_AUTO_CREATE);   //绑定服务
        //BIND_AUTO_CREATE表示活动与服务绑定后自动创建服务
    }

    @SuppressWarnings("unused")
    /**
     * 请求LastestNews信息
     */
    public void requestLatestNews(final IRequestCallback iRequestCallback) {
        String latestNewsUrl = "https://zhihu-daily.leanapp.cn/api/v1/last-stories";
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
        String latestNewsUrl = "https://zhihu-daily.leanapp.cn/api/v1/before-stories/" + nextdate;
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
    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                return info.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }
}
