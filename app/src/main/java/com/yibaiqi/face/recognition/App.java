package com.yibaiqi.face.recognition;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;

import com.yibaiqi.face.recognition.di.AppComponent;
import com.yibaiqi.face.recognition.di.DaggerAppComponent;

import javax.inject.Inject;


public class App extends Application {

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
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public static App getInstance() {
        return instance;
    }
}
