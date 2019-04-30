package com.yibaiqi.face.recognition.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.network.ApiResponse;
import com.yibaiqi.face.recognition.network.ApiService;
import com.yibaiqi.face.recognition.network.NetworkResource;
import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Resource;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by @author xiaofu on 2019/4/23.
 */
@Singleton
public class FaceRepository {
    private final ApiService service;
    private final AppExecutors appExecutors;

    @Inject
    FaceRepository(ApiService service, AppExecutors appExecutors) {
        this.service = service;
        this.appExecutors = appExecutors;
    }

    public LiveData<Resource<BaseResponse<RegisterDevice>>> registerDevice(String devId, String devName) {
        return new NetworkResource<BaseResponse<RegisterDevice>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<RegisterDevice>>> createCall() {
                return service.registerDevice(devId, devName);
            }
        }.asLiveData();
    }
}
