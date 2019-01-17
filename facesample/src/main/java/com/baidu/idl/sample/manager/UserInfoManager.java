package com.baidu.idl.sample.manager;

import android.text.TextUtils;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.db.DBManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户管理
 * Created by v_liujialu01 on 2018/12/14.
 */

public class UserInfoManager {
    private static UserInfoManager single = null;
    private ExecutorService mExecutorService = null;

    // 私有构造
    private UserInfoManager() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public static UserInfoManager getInstance() {
        if (single == null) {
            synchronized (UserInfoManager.class) {
                if (single == null) {
                    single = new UserInfoManager();
                }
            }
        }
        return single;
    }

    /**
     * 读取数据库信息
     * @param keyWords
     * @param listener
     */
    public void getFeatureInfo(final String keyWords, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (keyWords == null) {
                    listener.featureQuerySuccess(FaceApi.getInstance().featureQuery());
                } else {
                    if (TextUtils.isEmpty(keyWords)) {
                        listener.featureQueryFailure("请输入关键字");
                        return;
                    }
                    listener.featureQuerySuccess(DBManager.getInstance().queryFeatureByName(keyWords));
                }
            }
        });
    }

    /**
     * 批量删除
     * @param listFeatureInfo
     * @param listener
     */
    public void batchRemoveFeatureInfo(final List<Feature> listFeatureInfo,
                                       final UserInfoListener listener, final int selectCount) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                for (int i = 0; i < listFeatureInfo.size(); i++) {
                    if (listFeatureInfo.get(i).isChecked()) {
                        FaceApi.getInstance().featureDelete(listFeatureInfo.get(i));
                        if (selectCount > 0) {
                            listener.showDeleteProgressDialog((float) i / (float) selectCount);
                        }
                    }
                }
                FaceSDKManager.getInstance().getFeatureLRUCache().clear();
                listener.deleteSuccess();
            }
        });

    }

    public static class UserInfoListener {
        public void featureQuerySuccess(List<Feature> listFeatureInfo) {

        }

        public void featureQueryFailure(String message) {

        }

        public void showDeleteProgressDialog(float progress) {

        }

        public void deleteSuccess() {

        }
    }
}
