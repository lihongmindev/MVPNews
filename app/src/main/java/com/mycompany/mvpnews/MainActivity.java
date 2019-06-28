package com.mycompany.mvpnews;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.mvpnews.adapter.RecyclerAdapter;
import com.mycompany.mvpnews.bean.RecyclerList;
import com.mycompany.mvpnews.model.IModelNews;
import com.mycompany.mvpnews.model.IRequestCallback;
import com.mycompany.mvpnews.model.ModelNews;
import com.mycompany.mvpnews.presenter.PresenterNews;
import com.mycompany.mvpnews.util.TimeUtil;
import com.mycompany.mvpnews.view.IViewNews;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.valueOf;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements IViewNews {

    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerAdapter adapter;
    private Context context;
    private PresenterNews presenterNews;
    private int lastVisibleItem = 0;
    private int Nextdate = valueOf(TimeUtil.getToday());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;

        //侧滑菜单
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        //获得NavigationView的list view,从而绑定里面的控件
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_call);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        //获得NavigationView的头部view,从而绑定里面的控件
        View view = navigationView.getHeaderView(0);
        TextView textView = view.findViewById(R.id.lixian);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                   //离线下载监听事件
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.
                            WRITE_EXTERNAL_STORAGE}, 1);     //权限请求
                } else {
                    presenterNews.downloadNews(MainActivity.this);
                }
            }
        });
        /*
        下拉刷新设置
         */
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenterNews.readNews();    //请求今天的新闻
                Nextdate = valueOf(TimeUtil.getToday());
                swipeRefreshLayout.setRefreshing(false);    //将刷新动作去除
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);      //RecyclerView初始化
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 在newState为滑到底部时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("MainActivity", "滑到底部");
                    // 如果没有隐藏footView，那么最后一个条目的位置就比我们的getItemCount少1
                    if (lastVisibleItem + 1 == adapter.getItemCount()) {
                        Nextdate = valueOf(TimeUtil.getNextDay(Nextdate));
                        presenterNews.readNews(String.valueOf(Nextdate));    //请求指定日期的新闻
                    }
                }
            }
                public void onScrolled (@NonNull RecyclerView recyclerView, int dx, int dy){
                    super.onScrolled(recyclerView, dx, dy);
                    // 在滑动完成后，拿到最后一个可见的item的位置
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                }
        });

        presenterNews = new PresenterNews(this);
        presenterNews.readNews();    //请求今天的新闻

    }


    @Override
    public void setNews(final List<RecyclerList> allRecycler) {
        Log.d("mvp1","更新adapter");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.UpdateRecyclerAdapter(context, allRecycler);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void setDownLoadProgress(String progress) {
        //获得NavigationView的list view,从而绑定里面的控件
        NavigationView navigationView = findViewById(R.id.nav_view);
        //获得NavigationView的头部view,从而绑定里面的控件
        View view = navigationView.getHeaderView(0);
        TextView textView = view.findViewById(R.id.lixian);
        textView.setText(progress);
    }

    @Override
    public void error(String str) {
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenterNews.downloadNews(MainActivity.this);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.deny_permission), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}
