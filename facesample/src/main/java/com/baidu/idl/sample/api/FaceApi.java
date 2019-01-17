package com.baidu.idl.sample.api;

import android.text.TextUtils;

import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.common.FaceEnvironment;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.ARGBImg;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库、SDK相关封装操作
 * Created by v_liujialu01 on 2018/11/28.
 */

public class FaceApi {
    private static FaceApi instance;

    public static synchronized FaceApi getInstance() {
        if (instance == null) {
            instance = new FaceApi();
        }
        return instance;
    }

    /**
     * 添加特征信息
     *
     * @param feature
     * @return
     */
    public boolean featureAdd(Feature feature) {
        if (feature == null || TextUtils.isEmpty(feature.getGroupId())) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(feature.getUserId());
        if (!matcher.matches()) {
            return false;
        }
        return DBManager.getInstance().addFeature(feature);
    }

    public List<Feature> featureQuery() {
        return DBManager.getInstance().queryFeature();
    }

    /**
     * 删除特征信息
     *
     * @param feature
     * @return
     */
    public boolean featureDelete(Feature feature) {
        if (feature == null) {
            return false;
        }
        return DBManager.getInstance().deleteFeature(feature.getUserId(), feature.getGroupId(),
                feature.getFaceToken());
    }

    /**
     * 提取特征值
     */
    public float getFeature(ARGBImg argbImg, byte[] feature, FaceFeature.FeatureType featureType,
                            FaceEnvironment environment, FaceInfo[] newFaceInfos) {
        if (argbImg == null) {
            return -1;
        }
        // 最大检测人脸，获取人脸信息
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(
                argbImg.data, argbImg.width, argbImg.height);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            newFaceInfos[0] = faceInfos[0];
            FaceInfo faceInfo = faceInfos[0];
            // 人脸识别，提取人脸特征值
            ret = FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbImg.data,
                    argbImg.height, argbImg.width, feature, faceInfo.landmarks);
        }
        // 清除人脸缓存
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        return ret;
    }

    /**
     * 是否是有效姓名
     *
     * @param username
     * @return
     */
    public String isValidName(String username) {
        if (username == null || "".equals(username.trim())) {
            return "姓名为空";
        }

        // 姓名过长
        if (username.length() > 10) {
            return "姓名过长";
        }

        // 含有特殊符号
        String regex = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）—"
                + "—+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        if (m.find()) {
            return "姓名中含有特殊符号";
        }

        return "0";
    }
}
