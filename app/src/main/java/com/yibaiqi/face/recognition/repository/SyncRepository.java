package com.yibaiqi.face.recognition.repository;

import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.network.ApiService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by @author xiaofu on 2019/4/23.
 */
@Singleton
public class SyncRepository {
    private final ApiService service;
    private final AppExecutors appExecutors;

    @Inject
    SyncRepository(ApiService service, AppExecutors appExecutors) {
        this.service = service;
        this.appExecutors = appExecutors;
    }



}
