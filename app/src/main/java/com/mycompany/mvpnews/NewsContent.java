package com.mycompany.mvpnews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mycompany.mvpnews.adapter.ContentViewAdapter;
import com.mycompany.mvpnews.db.BeforeNews;
import com.mycompany.mvpnews.db.TodayNews;
import com.mycompany.mvpnews.db.TopNews;
import com.mycompany.mvpnews.util.HtmlUtil;
import com.mycompany.mvpnews.util.HttpUtil;
import com.mycompany.mvpnews.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NewsContent extends AppCompatActivity {

    private ArrayList<String> mNewsIdList = new ArrayList<>();
    private ArrayList<View> listViews = new ArrayList<>();
    private int count = 0;
    private Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_content);
        drawable = ContextCompat.getDrawable(NewsContent.this,R.drawable.imgcover);  //前景图片
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);   //启用HomeAsUp按钮
        }

        Intent intent = getIntent();
        mNewsIdList = intent.getStringArrayListExtra("new_id");//得到newsId的列表
        int mposition = intent.getIntExtra("new_position", 0);

        while (count != mNewsIdList.size()){        //将所有传过来的view加载
                initListViews(count++);
           }
        ViewPager viewPager = findViewById(R.id.viewpager_news_content);
       //将newsId的列表和view传入适配器
        ContentViewAdapter viewAdapter = new ContentViewAdapter(listViews);
        viewPager.setAdapter(viewAdapter);
        viewPager.setCurrentItem(mposition);       //设置当前的view

    }
    /**
     * listViews添加view对象
     *
     */
    private void initListViews(int count) {

        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View view_content = inflater.inflate(R.layout.viewpager_newscontent, null);
        requestNewsContent(mNewsIdList.get(count),view_content);
        listViews.add(view_content);
        Log.d("hash",String.valueOf(count));
    }

    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.news_content_toolbar, menu);
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item){

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }
    /**
     * 请求LastestNews信息
     */
    @SuppressLint("SetJavaScriptEnabled")
    public void requestNewsContent(String news_id, final View view_content){

        String newsContentUrl = "https://news-at.zhihu.com/api/4/news/"+ news_id;

            HttpUtil.sendOkHttpRequest(newsContentUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();                  //获得原始数据
                    final com.mycompany.mvpnews.gson.NewsContent newsContent = Utility.handleNewsContentResponse(responseText);   //解析数据
                    Log.i("TAG","2");
                    runOnUiThread(new Runnable() {          //回到UI主线程
                        @Override
                        public void run() {
                            Log.i("TAG","4");
                            if (newsContent != null) {   //判断请求信息是否成功
                                showNewsContentInfo(newsContent,view_content);
                            } else {
                                Toast.makeText(NewsContent.this, getResources().getString(R.string.fail_get_news), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Log.i("TAG","3");
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NewsContent.this, getResources().getString(R.string.bad_net), Toast.LENGTH_SHORT).show();
                        }


                    });
                }
            });
            Log.i("TAG","1");
    }

    /**
     * 处理并展示NewsContent实体类中的数据
     */
    @SuppressLint("SetJavaScriptEnabled")
    public void showNewsContentInfo(final com.mycompany.mvpnews.gson.NewsContent newsContent, final View view_content) {

        String newTitle = newsContent.NewTitle;
        String newImage = newsContent.NewImage;
        String newBody = newsContent.NewBody;

        WebView webView = view_content.findViewById(R.id.web_view);    //必须要加上对应layout，否则不知道给哪个view
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        String htmlData = HtmlUtil.createHtmlData(newBody,newsContent.NewCssList, newsContent.NewJs);
        ImageView imageView = view_content.findViewById(R.id.new_image_view);
        URL imageUrl = null;
        try {
            imageUrl = new URL(newImage);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Glide.with(NewsContent.this).load(imageUrl).into(imageView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            imageView.setForeground(drawable);
        }
        webView.loadData(htmlData, HtmlUtil.MIME_TYPE, HtmlUtil.ENCODING);

        TextView textView = view_content.findViewById(R.id.new_title);
        textView.setText(newTitle);
    }
}
