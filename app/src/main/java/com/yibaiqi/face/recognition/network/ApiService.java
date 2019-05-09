package com.yibaiqi.face.recognition.network;

import android.arch.lifecycle.LiveData;

import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.Movie;
import com.yibaiqi.face.recognition.vo.OSSConfig;
import com.yibaiqi.face.recognition.vo.RegisterDevice;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by @author xiaofu on 2019/4/23.
 */
public interface ApiService {

    @PUT("devices/FaceDevices/register")
    LiveData<ApiResponse<BaseResponse<RegisterDevice>>> registerDevice(@Query("device_id") String devId, @Query("device_name") String devName);

    @POST("devices/FaceDevices/activation_success")
    LiveData<ApiResponse<BaseResponse<String>>> bindDevice(@Header("token") String token, @Query("device_fingerprint") String key);

    @POST("devices/BrushFaceLog/add")
    LiveData<ApiResponse<BaseResponse<Object>>> addRecord(@Header("token") String token);

    @GET("devices/BrushFaceLog/oss_config")
    LiveData<ApiResponse<BaseResponse<OSSConfig>>> getOSSConfig(@Header("token") String token);

}
