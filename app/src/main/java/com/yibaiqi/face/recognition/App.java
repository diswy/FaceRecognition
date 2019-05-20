package com.yibaiqi.face.recognition;

import android.arch.lifecycle.ViewModelProvider;

import com.nanchen.crashmanager.CrashApplication;
import com.nanchen.crashmanager.UncaughtExceptionHandlerImpl;
import com.umeng.commonsdk.UMConfigure;
import com.yibaiqi.face.recognition.di.AppComponent;
import com.yibaiqi.face.recognition.di.DaggerAppComponent;
import com.yibaiqi.face.recognition.ui.core.SplashActivity;

import javax.inject.Inject;

import io.rong.imlib.RongIMClient;


public class App extends CrashApplication {

    private static App instance;
    private AppComponent appComponent;

    @Inject
    public ViewModelProvider.Factory factory;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        appComponent = DaggerAppComponent.builder().application(this).build();
        appComponent.inject(this);

        // 设置崩溃后自动重启 APP
        UncaughtExceptionHandlerImpl.getInstance().init(this, BuildConfig.DEBUG, true, 0, SplashActivity.class);


        // 融云初始化
        RongIMClient.init(this);
//        RongIMClient.setOnReceiveMessageListener((message, left) -> {
//            System.out.println("--->>>>>>消息：" + message);
//            System.out.println("--->>>>>>未拉取：" + left);
//            return false;
//        });

        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(this, "5ce0e0a6570df351a0000ef1", "only_one", UMConfigure.DEVICE_TYPE_PHONE, null);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public static App getInstance() {
        return instance;
    }
}
