package com.yibaiqi.face.recognition.di;

import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.db.AppDatabase;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(App app);

    AppDatabase appDatabase();
    AppExecutors appExcutor();
}
