package com.liar.tsetkeeplive1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.liar.tsetkeeplive1.R;
import com.liar.tsetkeeplive1.service.MainService;
import com.lodz.android.component.base.activity.BaseActivity;

public class MainActivity extends BaseActivity {



    /**
     * 主服务
     **/
    private Intent mainService;

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void findViews(Bundle savedInstanceState) {

    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initData() {
        super.initData();
        starLocalService();
    }

    /**
     * 开启主服务
     **/
    public void starLocalService() {
        mainService = new Intent(getApplication(), MainService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(mainService);
        }else {
            startService(mainService);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
