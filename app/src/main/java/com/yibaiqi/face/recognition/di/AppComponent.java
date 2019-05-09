package com.yibaiqi.face.recognition.di;

import android.app.Application;

import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.db.AppDatabase;
import com.yibaiqi.face.recognition.db.UserDao;
import com.yibaiqi.face.recognition.tools.ACache;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(App app);

    AppDatabase appDatabase();

    AppExecutors appExecutors();

    UserDao userDao();

    ACache aCache();
}
