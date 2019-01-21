package com.yibaiqi.face.recognition.di;

import android.arch.persistence.room.Room;

import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.db.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AppModule {

    @Provides
    App provideApp() {
        return App.getInstance();
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase(App app) {
        return Room.databaseBuilder(app, AppDatabase.class, "yibaiqi")
                .build();
    }


}
