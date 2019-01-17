package com.yibaiqi.face.recognition;

import android.app.Application;

import com.baidu.crabsdk.CrabSDK;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrabSDK.init(this, "8bc935ee31c9b769");
    }
}
