package com.yibaiqi.face.recognition;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.yibaiqi.face.recognition.db.AppDatabase;
import com.yibaiqi.face.recognition.di.AppComponent;
import com.yibaiqi.face.recognition.di.DaggerAppComponent;


public class App extends Application {

    private static App instance;
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        appComponent = DaggerAppComponent.create();
        appComponent.inject(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public static App getInstance() {
        return instance;
    }
}
