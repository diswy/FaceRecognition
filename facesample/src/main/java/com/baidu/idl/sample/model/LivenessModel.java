/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.model;
import com.baidu.idl.facesdk.model.Feature;

public class LivenessModel {

    private ImageFrame imageFrame = new ImageFrame();
    private int faceDetectCode;
    private long rgbDetectDuration;
    private long rgbLivenessDuration;
    private float irLivenessScore;
    private long irLivenessDuration;
    private long detphtLivenessDuration;
    private float rgbLivenessScore;
    private float depthLivenessScore;
    private int liveType;
    private int[] landmarks;
    private int faceID = -1;

    private float featureScore;
    private long featureDuration;
    private long checkDuration;

    private Feature feature;

    private byte[] featureByte;

    public int[] getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(int[] landmarks) {
        this.landmarks = landmarks;
    }

    public int[] getShape() {
        return shape;
    }

    public void setShape(int[] shape) {
        this.shape = shape;
    }

    private int[] shape;

    public ImageFrame getImageFrame() {
        return imageFrame;
    }

    public void setImageFrame(ImageFrame imageFrame) {
        this.imageFrame = imageFrame;
    }

    public int getFaceDetectCode() {
        return faceDetectCode;
    }

    public void setFaceDetectCode(int faceDetectCode) {
        this.faceDetectCode = faceDetectCode;
    }

    public long getRgbDetectDuration() {
        return rgbDetectDuration;
    }

    public void setRgbDetectDuration(long rgbDetectDuration) {
        this.rgbDetectDuration = rgbDetectDuration;
    }

    public long getRgbLivenessDuration() {
        return rgbLivenessDuration;
    }

    public void setRgbLivenessDuration(long rgbLivenessDuration) {
        this.rgbLivenessDuration = rgbLivenessDuration;
    }

    public float getIrLivenessScore() {
        return irLivenessScore;
    }

    public void setIrLivenessScore(float irLivenessScore) {
        this.irLivenessScore = irLivenessScore;
    }

    public long getIrLivenessDuration() {
        return irLivenessDuration;
    }

    public void setIrLivenessDuration(long irLivenessDuration) {
        this.irLivenessDuration = irLivenessDuration;
    }

    public long getDetphtLivenessDuration() {
        return detphtLivenessDuration;
    }

    public void setDetphtLivenessDuration(long detphtLivenessDuration) {
        this.detphtLivenessDuration = detphtLivenessDuration;
    }

    public float getRgbLivenessScore() {
        return rgbLivenessScore;
    }

    public void setRgbLivenessScore(float rgbLivenessScore) {
        this.rgbLivenessScore = rgbLivenessScore;
    }

    public float getDepthLivenessScore() {
        return depthLivenessScore;
    }

    public void setDepthLivenessScore(float depthLivenessScore) {
        this.depthLivenessScore = depthLivenessScore;
    }

    public int getLiveType() {
        return liveType;
    }

    public void setLiveType(int liveType) {
        this.liveType = liveType;
    }

    public long getFeatureDuration() {
        return featureDuration;
    }

    public void setFeatureDuration(long featureDuration) {
        this.featureDuration = featureDuration;
    }

    public long getCheckDuration() {
        return checkDuration;
    }

    public void setCheckDuration(long checkDuration) {
        this.checkDuration = checkDuration;
    }

    public float getFeatureScore() {
        return featureScore;
    }

    public void setFeatureScore(float featureScore) {
        this.featureScore = featureScore;
    }

    public long getLiveDuration() {
        return getRgbLivenessDuration() + getIrLivenessDuration() + getDetphtLivenessDuration();
    }

    public long getAllDuration() {
        return getRgbDetectDuration() + getLiveDuration()
                + getFeatureDuration() + getCheckDuration();
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public byte[] getFeatureByte() {
        return featureByte;
    }

    public void setFeatureByte(byte[] featureByte) {
        this.featureByte = featureByte;
    }

    public int getFaceID() {
        return faceID;
    }

    public void setFaceID(int faceID) {
        this.faceID = faceID;
    }
}

