package com.liar.tsetkeeplive1.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.liar.tsetkeeplive1.R;
import com.liar.tsetkeeplive1.config.NotifiConfig;
import com.liar.tsetkeeplive1.keeplive.foreground.ChannelService;
import com.liar.tsetkeeplive1.keeplive.timer.ScheduleService;
import com.lodz.android.core.log.PrintLog;
import com.lodz.android.core.utils.UiHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by LiarShi on 2018/10/16.
 */
public class MainService extends Service {

    public static final String TAG = "MainService";

    public static final int SERVICE_ID = 9955;

    /**
     * 延迟启动的时间间隔，1分钟
     */
    private final static long DELAYED_INTERVAL =  60000;
    /**
     * 定时唤醒的时间间隔，15 分钟
     */
    private final static long WAKE_INTERVAL = 900000;


    //毫秒
    private long Millis=0;
    //线程池，用来定时打印信息
    private ScheduledExecutorService executorService ;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            // 开启前台服务
            startForeground(SERVICE_ID, getBaseNotify());

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // 开启前台服务
            startForeground(SERVICE_ID, new Notification());
            Log.e(TAG, "启动虚假的渠道服务 ChannelService：：onStartCommand()");
            Intent sendIntend = new Intent(getApplication(), ChannelService.class);
            startService(sendIntend);
        }

        UiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // 定时检查 WorkService 是否在运行，如果不在运行就把它拉起来
                    // Android 5.0+ 使用 JobScheduler，效果比 AlarmManager 好
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Log.i(TAG, "开启 JobService 定时");
                        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                        jobScheduler.cancelAll();
                        JobInfo.Builder builder = new JobInfo.Builder(1024, new ComponentName(getPackageName(), ScheduleService.class.getName()));
                        builder.setPeriodic(WAKE_INTERVAL);
                        builder.setPersisted(true);
                        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                        int schedule = jobScheduler.schedule(builder.build());
                        if (schedule <= 0) {
                            Log.w(TAG, "schedule error！");
                        }
                    } else {
                        // Android 4.4- 使用 AlarmManager
                        Log.i(TAG, "开启 AlarmManager 定时");
                        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                        Intent alarmIntent = new Intent(getApplication(), MainService.class);
                        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1024, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        am.cancel(pendingIntent);
                        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + WAKE_INTERVAL, WAKE_INTERVAL, pendingIntent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "e:", e);
                }
                // 简单守护开机广播
                getPackageManager().setComponentEnabledSetting(
                        new ComponentName(getPackageName(), MainService.class.getName()),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        },DELAYED_INTERVAL);

        if(executorService==null){
            Millis=20000;
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {

                    PrintLog.e(TAG,"MainService开始执行打印信息任务");
                }
            }, 5000, Millis, TimeUnit.MILLISECONDS);
        }

        return super.onStartCommand(intent, flags, startId);
    }


    /** 显示基础通知 */
    private Notification getBaseNotify() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication(),NotifiConfig.NOTIFI_CHANNEL_MAIN_ID);// 获取构造器
        builder.setTicker("APP保活");// 通知栏显示的文字
        builder.setContentTitle("APP保活");// 通知栏通知的标题
        builder.setContentText("APP保活正在运行");// 通知栏通知的详细内容（只有一行）
        builder.setAutoCancel(false);// 设置为true，点击该条通知会自动删除，false时只能通过滑动来删除（一般都是true）
        builder.setSmallIcon(R.mipmap.ic_launcher);//通知上面的小图标（必传）
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);//通知默认的声音 震动 呼吸灯
        builder.setPriority(NotificationCompat.PRIORITY_MAX);//设置优先级，级别高的排在前面
        Notification notification = builder.build();//构建通知
        return notification;
    }

    /**
     * 开启获取定位信息的服务
     **/
    public void starService() {

        PrintLog.d(TAG, "在TencentLocationService开启双服务");
        Intent service = new Intent(getApplication(), MainService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        }else {
            startService(service);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        starService();

    }
}
