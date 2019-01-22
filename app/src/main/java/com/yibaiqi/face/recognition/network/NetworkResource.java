package com.yibaiqi.face.recognition.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.tools.Objects;
import com.yibaiqi.face.recognition.vo.Resource;

/**
 * Created by @author xiaofu on 2019/1/22.
 */
public abstract class NetworkResource<RequestType> {
    private final AppExecutors appExecutors;

    private final MediatorLiveData<Resource<RequestType>> result = new MediatorLiveData<>();

    @MainThread
    public NetworkResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        result.setValue(Resource.loading(null));
        fetchFromNetwork();
    }

    @MainThread
    private void setValue(Resource<RequestType> newValue) {
        if (!Objects.equals(result.getValue(), newValue)) {
            result.setValue(newValue);
        }
    }

    private void fetchFromNetwork() {
        LiveData<ApiResponse<RequestType>> apiResponse = createCall();
        result.addSource(apiResponse, response -> {
            result.removeSource(apiResponse);
            if (response != null && response.isSuccessful()) {
                appExecutors.mainThread().execute(() ->
                        setValue(Resource.success(response.body))
                );
            } else {
                onFetchFailed();
                appExecutors.mainThread().execute(() ->
                        setValue(Resource.error(response != null ? response.errorMessage : "unknown", null))
                );
            }
        });

    }

    protected void onFetchFailed() {
    }

    public LiveData<Resource<RequestType>> asLiveData() {
        return result;
    }

    @NonNull
    @MainThread
    protected abstract LiveData<ApiResponse<RequestType>> createCall();
}
