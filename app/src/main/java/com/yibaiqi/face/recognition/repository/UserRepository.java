package com.yibaiqi.face.recognition.repository;


import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.db.UserDao;
import com.yibaiqi.face.recognition.network.ApiResponse;
import com.yibaiqi.face.recognition.network.NetworkBoundResource;
import com.yibaiqi.face.recognition.network.NetworkResource;
import com.yibaiqi.face.recognition.network.WebService;
import com.yibaiqi.face.recognition.vo.Movie;
import com.yibaiqi.face.recognition.vo.Resource;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class UserRepository {
    private final UserDao userDao;
    private final WebService service;
    private final AppExecutors appExecutors;

    @Inject
    UserRepository(UserDao userDao, WebService service, AppExecutors appExecutors) {
        this.userDao = userDao;
        this.service = service;
        this.appExecutors = appExecutors;
    }

    public void getMovie2(){
    }

    public LiveData<Resource<Movie>> getMovie() {
        return new NetworkBoundResource<Movie, Movie>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull Movie item) {
                String ss = new Gson().toJson(item);
                System.out.println("--->>>>>>>:"+ss);
            }

            @Override
            protected boolean shouldFetch(@Nullable Movie data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<Movie> loadFromDb() {
                return null;
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Movie>> createCall() {
                return service.getMovie("28d0461b30a6d","CN");
            }
        }.asLiveData();
    }


    public LiveData<Resource<Movie>> getMovie3(){
        return new NetworkResource<Movie>(appExecutors){
            @NonNull
            @Override
            protected LiveData<ApiResponse<Movie>> createCall() {
                return service.getMovie("28d0461b30a6d","CN");
            }
        }.asLiveData();
    }


}
