package com.yibaiqi.face.recognition.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.Key;
import com.yibaiqi.face.recognition.db.UserDao;
import com.yibaiqi.face.recognition.network.ApiResponse;
import com.yibaiqi.face.recognition.network.ApiService;
import com.yibaiqi.face.recognition.network.NetworkResource;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by @author xiaofu on 2019/4/23.
 */
@Singleton
public class FaceRepository {
    private final ApiService service;
    private final AppExecutors appExecutors;
    private final UserDao userDao;
    private final ACache mCache;

    @Inject
    FaceRepository(ApiService service, AppExecutors appExecutors, UserDao userDao, ACache aCache) {
        this.service = service;
        this.appExecutors = appExecutors;
        this.userDao = userDao;
        this.mCache = aCache;
    }

    /**
     * 获取设备的密钥
     */
    public LiveData<Resource<BaseResponse<RegisterDevice>>> registerDevice(String devId, String devName) {
        return new NetworkResource<BaseResponse<RegisterDevice>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<RegisterDevice>>> createCall() {
                return service.registerDevice(devId, devName);
            }
        }.asLiveData();
    }

    /**
     * 绑定设备
     */
    public LiveData<Resource<BaseResponse<String>>> bindDevice(String devId) {
        return new NetworkResource<BaseResponse<String>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<String>>> createCall() {
                return service.bindDevice(devId);
            }
        }.asLiveData();
    }


    //-------------------数据库操作相关
    public void insert(List<DbOption> list) {
        appExecutors.diskIO().execute(() -> {
            if (list != null) {
                userDao.insert(list);
            }
        });
    }

    public void update(List<DbOption> list) {
        appExecutors.diskIO().execute(() -> {
            if (list != null) {
                userDao.update(list);
            }
        });
    }

    public void delete(List<DbOption> list) {
        appExecutors.diskIO().execute(() -> {
            if (list != null) {
                userDao.delete(list);
            }
        });
    }

    //------------摄像头
    public boolean isCameraEnable() {
        return mCache.getAsString(Key.KEY_CAMERA_ACCOUNT) != null
                && mCache.getAsString(Key.KEY_CAMERA_IP) != null
                && mCache.getAsString(Key.KEY_CAMERA_PORT) != null
                && mCache.getAsString(Key.KEY_CAMERA_PWD) != null;
    }

    public String getCameraIp() {
        return mCache.getAsString(Key.KEY_CAMERA_IP);
    }

    public int getCameraPort() {
        return Integer.parseInt(mCache.getAsString(Key.KEY_CAMERA_PORT));
    }

    public String getCameraAccount() {
        return mCache.getAsString(Key.KEY_CAMERA_ACCOUNT);
    }

    public String getCameraPwd() {
        return mCache.getAsString(Key.KEY_CAMERA_PWD);
    }


}
