package com.yibaiqi.face.recognition.network;

import android.arch.lifecycle.LiveData;

import com.yibaiqi.face.recognition.vo.Movie;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WebService {

    @GET("boxoffice/day/query")
    LiveData<ApiResponse<Movie>> getMovie(@Query("key") String key, @Query("area") String area);
}
