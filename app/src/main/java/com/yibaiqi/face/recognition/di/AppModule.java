package com.yibaiqi.face.recognition.di;

import android.arch.persistence.room.Room;

import com.readystatesoftware.chuck.ChuckInterceptor;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.db.AppDatabase;
import com.yibaiqi.face.recognition.db.UserDao;
import com.yibaiqi.face.recognition.network.ApiService;
import com.yibaiqi.face.recognition.network.WebService;
import com.yibaiqi.face.recognition.network.converter.LiveDataCallAdapterFactory;
import com.yibaiqi.face.recognition.network.converter.StringConverterFactory;
import com.yibaiqi.face.recognition.tools.ACache;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(includes = ViewModelModule.class)
class AppModule {

    @Provides
    App provideApp() {
        return App.getInstance();
    }

    @Singleton
    @Provides
    AppDatabase provideAppDatabase(App app) {
        return Room.databaseBuilder(app, AppDatabase.class, "yibaiqi.db")
                .build();
    }

    @Singleton
    @Provides
    ACache provideACache(App app) {
        return ACache.get(app);
    }

    @Singleton
    @Provides
    UserDao provideUserDao(AppDatabase appDatabase) {
        return appDatabase.userDao();
    }

    @Singleton
    @Provides
    WebService provideWebService(App app) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new ChuckInterceptor(app))
                .build();
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://apicloud.mob.com/")
                .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory.create())
                .build()
                .create(WebService.class);
    }

    @Singleton
    @Provides
    ApiService provideApiService(App app) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new ChuckInterceptor(app))
                .build();
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://www.yzx110.com/")
//                .baseUrl("http://toysns.com/")
                .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory.create())
                .build()
                .create(ApiService.class);
    }

}
