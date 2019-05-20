/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.manager;


import android.content.Context;

import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.baidu.idl.sample.MyConfig;
import com.baidu.idl.sample.callback.FaceCallback;
import com.baidu.idl.sample.common.FaceEnvironment;
import com.baidu.idl.sample.model.ImageFrame;

public class FaceDetector {
    private Context context;
    private FaceDetect mFaceDetect;

    public void initModel(Context context, String visModel, String nirModel, String alignModel, final FaceCallback faceCallback) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            mFaceDetect.initModel(context, visModel, nirModel, alignModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceDetect.initModel(context, visModel, nirModel, alignModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }
    }

    public void initQuality(final Context context, final String blurModel, final String occlurModel, final FaceCallback faceCallback) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            mFaceDetect.initQuality(context, blurModel, occlurModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceDetect.initQuality(context, blurModel, occlurModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }
    }

    public void loadConfig(FaceEnvironment faceEnvironment) {
        mFaceDetect.loadConfig(faceEnvironment.getConfig());
    }

    /**
     * 人脸图片追踪最大检测的人脸
     *
     * @param argb   人脸argb_8888图片。
     * @param width  图片宽度
     * @param height 图片高度
     * @return 检测结果代码。
     */
    public FaceInfo[] trackMaxFace(int[] argb, int width, int height) {
        int minDetectFace = FaceSDKManager.getInstance().getFaceEnvironmentConfig(new MyConfig()).getMinFaceSize();
        if (width < minDetectFace || height < minDetectFace) {
            return null;
        }
        FaceInfo[] faceInfos = mFaceDetect.trackMaxFace(argb, height, width);

//        Utils.saveBitmapToFile(System.currentTimeMillis() + ".png", Utils.getBitmap(
//                argb, height, width));

        return faceInfos;
    }

    /**
     * 人脸图片追踪最大检测的人脸
     *
     * @param imageFrame 人脸图片帧
     * @return 检测结果代码。
     */
    public FaceInfo[] trackMaxFace(ImageFrame imageFrame) {
        return trackMaxFace(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight());
    }

    /**
     * 人脸图片检测所有人脸框
     *
     * @param argb        人脸argb_8888图片。
     * @param width       图片宽度
     * @param height      图片高度
     * @param minFaceSize 最小人脸大小
     * @return 检测结果代码。
     */
    public FaceInfo[] detect(int[] argb, int width, int height, int minFaceSize) {
        if (width < minFaceSize || height < minFaceSize) {
            return null;
        }
        return mFaceDetect.detect(argb, height, width, minFaceSize);
    }

    /**
     * 人脸图片检测所有人脸框
     *
     * @param imageFrame  人脸图片帧
     * @param minFaceSize 最小人脸大小
     * @return 检测结果代码。
     */
    public FaceInfo[] detect(ImageFrame imageFrame, int minFaceSize) {
        return detect(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight(), minFaceSize);
    }


    /**
     * 人脸图片追踪第一个检测的人脸
     *
     * @param imageData
     * @param height
     * @param width
     * @return
     */
    public FaceInfo[] trackFirstFace(int[] imageData, int height, int width) {
        if (mFaceDetect != null) {
            return mFaceDetect.trackFirstFace(imageData, height, width);
        }
        return null;
    }

    /**
     * 人脸图片检测所有人脸框
     *
     * @param imageFrame 人脸图片帧
     * @return 检测结果代码。
     */
    public FaceInfo[] trackFirstFace(ImageFrame imageFrame) {
        if (mFaceDetect != null) {
            return trackFirstFace(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight());
        }
        return null;
    }

    /**
     * 人脸图片追踪检测所有人脸
     *
     * @param imageData
     * @param height
     * @param width
     * @param num
     * @return
     */
    public FaceInfo[] track(int[] imageData, int height, int width, int num) {
        if (mFaceDetect != null) {
            return mFaceDetect.track(imageData, height, width, num);
        }
        return null;
    }

    /**
     * 人脸图片检测所有人脸框
     *
     * @param imageFrame 人脸图片帧
     * @param num        人脸个数
     * @return 检测结果代码。
     */
    public FaceInfo[] track(ImageFrame imageFrame, int num) {
        return track(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight(), num);
    }

    /**
     * yuv图片转换为相应的argb;
     *
     * @param yuv      yuv_420p图片
     * @param width    图片宽度
     * @param height   图片高度
     * @param argb     接收argb用得 int数组
     * @param rotation yuv图片的旋转角度
     * @param mirror   是否为镜像
     */
    public int yuvToARGB(byte[] yuv, int width, int height, int[] argb, int rotation, int mirror) {
        if (mFaceDetect != null) {
            return mFaceDetect.getDataFromYUVimg(yuv, argb, width, height, rotation, mirror);
        }
        return -1;
    }

    /**
     * 重置跟踪人脸。下次将重新开始跟踪。
     */
    public void clearTrackedFaces() {
        if (mFaceDetect != null) {
            mFaceDetect.clearTrackedFaces();
        }
    }

}
