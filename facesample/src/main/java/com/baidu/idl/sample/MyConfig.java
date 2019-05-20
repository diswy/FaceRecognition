package com.baidu.idl.sample;

import java.io.Serializable;

/**
 * Created by @author xiaofu on 2019/5/20.
 */
public class MyConfig implements Serializable {
    private int minFace = 50;
    private int maxFaceSize = -1;
    private int detectInterval = 200;
    private int trackInterval = 1000;
    private float noFaceSize = 0.5f;
    private int pitch = 30;
    private int yaw = 30;
    private int roll = 30;
    private boolean checkBlur = true;
    private boolean occlusion = true;
    private boolean illumination = true;

    public int getMinFace() {
        return minFace;
    }

    public void setMinFace(int minFace) {
        this.minFace = minFace;
    }

    public int getMaxFaceSize() {
        return maxFaceSize;
    }

    public void setMaxFaceSize(int maxFaceSize) {
        this.maxFaceSize = maxFaceSize;
    }

    public int getDetectInterval() {
        return detectInterval;
    }

    public void setDetectInterval(int detectInterval) {
        this.detectInterval = detectInterval;
    }

    public int getTrackInterval() {
        return trackInterval;
    }

    public void setTrackInterval(int trackInterval) {
        this.trackInterval = trackInterval;
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
        return checkBlur;
    }

    public void setCheckBlur(boolean checkBlur) {
        this.checkBlur = checkBlur;
    }

    public boolean isOcclusion() {
        return occlusion;
    }

    public void setOcclusion(boolean occlusion) {
        this.occlusion = occlusion;
    }

    public boolean isIllumination() {
        return illumination;
    }

    public void setIllumination(boolean illumination) {
        this.illumination = illumination;
    }
}
