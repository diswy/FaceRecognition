/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.common;

import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.model.BDFaceSDKConfig;

/**
 * SDK全局配置信息
 */
public class FaceEnvironment {

    // SDK版本号
    public static final String VERSION = "2.0.0";

    public static final float LIVENESS_RGB_THRESHOLD = 0.8f;
    public static final float LIVENESS_IR_THRESHOLD = 0.8f;
    public static final float LIVENESS_DEPTH_THRESHOLD = 0.8f;

    public static boolean isFeatureDetect = false;

    private BDFaceSDKConfig config;

    /**
     * 最小人脸检测大小 建议50
     */
    public int minFaceSize;
    /**
     * 最大人脸检测大小 建议-1(不做限制)
     */
    public int maxFaceSize;

    /**
     * 人脸跟踪，检测的时间间隔 默认 500ms
     */
    public int trackInterval;

    /**
     * 人脸跟踪，跟踪时间间隔 默认 1000ms
     */
    public int detectInterval;

    /**
     * 人脸置信度阈值，建议值0.5
     */
    public float noFaceSize;

    /**
     * 人脸姿态角 pitch,yaw,roll
     */
    public int pitch;
    public int yaw;
    public int roll;

    /**
     * 质量检测模糊，遮挡，光照，默认不做质量检测
     */
    public boolean isCheckBlur;
    public boolean isOcclusion;
    public boolean isIllumination;

    /**
     * 检测图片类型，可见光或者红外
     */
    public FaceDetect.DetectType detectMethodType;

    public BDFaceSDKConfig getConfig() {
        if (config == null) {
            config = new BDFaceSDKConfig();
            config.minFaceSize = getMinFaceSize();
            config.maxFaceSize = getMaxFaceSize();
            config.trackInterval = getTrackInterval();
            config.detectInterval = getDetectInterval();
            config.noFaceSize = getNoFaceSize();
            config.pitch = getPitch();
            config.yaw = getYaw();
            config.roll = getRoll();
            config.isCheckBlur = isCheckBlur;
            config.isOcclusion = isOcclusion;
            config.isIllumination = isIllumination;
            config.detectMethodType = getDetectMethodType();
        }
        return config;
    }

    public void setConfig(BDFaceSDKConfig config) {
        this.config = config;
    }

    public int getMinFaceSize() {
        return minFaceSize;
    }

    public void setMinFaceSize(int minFaceSize) {
        this.minFaceSize = minFaceSize;
    }

    public int getMaxFaceSize() {
        return maxFaceSize;
    }

    public void setMaxFaceSize(int maxFaceSize) {
        this.maxFaceSize = maxFaceSize;
    }

    public int getTrackInterval() {
        return trackInterval;
    }

    public void setTrackInterval(int trackInterval) {
        this.trackInterval = trackInterval;
    }

    public int getDetectInterval() {
        return detectInterval;
    }

    public void setDetectInterval(int detectInterval) {
        this.detectInterval = detectInterval;
    }

    public float getNoFaceSize() {
        return noFaceSize;
    }

    public void setNoFaceSize(float noFaceSize) {
        this.noFaceSize = noFaceSize;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public boolean isCheckBlur() {
        return isCheckBlur;
    }

    public void setCheckBlur(boolean checkBlur) {
        isCheckBlur = checkBlur;
    }

    public boolean isOcclusion() {
        return isOcclusion;
    }

    public void setOcclusion(boolean occlusion) {
        isOcclusion = occlusion;
    }

    public boolean isIllumination() {
        return isIllumination;
    }

    public void setIllumination(boolean illumination) {
        isIllumination = illumination;
    }

    public FaceDetect.DetectType getDetectMethodType() {
        return detectMethodType;
    }

    public void setDetectMethodType(FaceDetect.DetectType detectMethodType) {
        this.detectMethodType = detectMethodType;
    }
}
