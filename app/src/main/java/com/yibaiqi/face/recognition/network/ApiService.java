package com.yibaiqi.face.recognition.network;

import android.arch.lifecycle.LiveData;

import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.ExData;
import com.yibaiqi.face.recognition.vo.OSSKey;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Remote;
import com.yibaiqi.face.recognition.vo.RemoteRecord;

import java.util.List;

import io.reactivex.Flowable;
import retrofit2.http.Body;
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

    @POST("devices/FaceDevices/activation_success")
    Flowable<String> bindDevice2(@Header("token") String token, @Query("device_fingerprint") String key);

    @POST("devices/BrushFaceLog/add")
    LiveData<ApiResponse<BaseResponse<Object>>> addRecord(@Header("token") String token, @Body Remote remote);

    @GET("devices/BrushFaceLog/oss_config")
    LiveData<ApiResponse<BaseResponse<OSSKey>>> getOSSConfig(@Header("token") String token);

    @GET("devices/FaceDevices/update_data")
    LiveData<ApiResponse<BaseResponse<ExData>>> requestData(@Header("token") String token);

}
