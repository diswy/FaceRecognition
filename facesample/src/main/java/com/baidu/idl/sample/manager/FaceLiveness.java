/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.manager;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.FaceLive;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.callback.FaceCallback;
import com.baidu.idl.sample.callback.IFaceDetectCallBack;
import com.baidu.idl.sample.callback.IFaceRegistCalllBack;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.baidu.idl.sample.manager.FaceLiveness.TaskType.TASK_TYPE_ONETON;

public class FaceLiveness {

    private static final String TAG = "FaceLiveness";
    public static final int MASK_RGB = 0X0001;
    public static final int MASK_IR = 0X0010;
    public static final int MASK_DEPTH = 0X0100;

    private FaceLive mFaceLive;

    private Bitmap bitmap;
    private ILivenessCallBack livenessCallBack;
    private IFaceDetectCallBack faceDetectCallBack;
    private final Object mLock = new Object();
    private String registNickName = "";
    private ArrayList<IFaceRegistCalllBack> registCalllBacks = new ArrayList<>();

    private int[] nirRgbArray;

    private int[] mRgbArray;
    private volatile boolean isVisHavePixls = false;

    private byte[] mIrByte;
    private volatile boolean isIRHavePixls = false;

    private byte[] mDepthArray;
    private volatile boolean isDepthHavePixls;
    private ExecutorService es;
    private ExecutorService es2;
    private Future future;
    private Future future2;
    private int curFaceID = -1;

    public static enum TaskType {
        TASK_TYPE_REGIST,
        TASK_TYPE_ONETON
    }

    private TaskType currentTaskType = TASK_TYPE_ONETON;

    public TaskType getCurrentTaskType() {
        return currentTaskType;
    }

    public void setCurrentTaskType(TaskType currentTaskType) {
        this.currentTaskType = currentTaskType;
    }

    public FaceLiveness() {
        es = Executors.newSingleThreadExecutor();
        es2 = Executors.newSingleThreadExecutor();
    }

    public void initModel(Context context, String visModel, String nirModel, String depthModel, final FaceCallback faceCallback) {
        if (mFaceLive == null) {
            mFaceLive = new FaceLive();
            mFaceLive.initModel(context, visModel, nirModel, depthModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceLive.initModel(context, visModel, nirModel, depthModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }

    }

    public void setLivenessCallBack(ILivenessCallBack callBack) {
        this.livenessCallBack = callBack;
    }

    public void setIFaceDetectCallBack(IFaceDetectCallBack callBack) {
        this.faceDetectCallBack = callBack;
    }

    public void addRegistCallBack(IFaceRegistCalllBack calllBack) {
        synchronized (mLock) {
            if (calllBack != null && registCalllBacks != null) {
                registCalllBacks.add(calllBack);
            }
        }
    }

    public void removeRegistCallBack(IFaceRegistCalllBack calllBack) {
        synchronized (mLock) {
            if (calllBack != null && registCalllBacks != null) {
                registCalllBacks.remove(calllBack);
            }
        }
    }

    public void setRegistNickName(String nickName) {
        if (nickName != null) {
            registNickName = nickName;
        }
    }

    /**
     * 设置可见光图
     *
     * @param bitmap
     */
    public void setRgbBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            if (mRgbArray == null) {
                mRgbArray = new int[bitmap.getWidth() * bitmap.getHeight()];
            }
            bitmap.getPixels(mRgbArray, 0, bitmap.getWidth(), 0, 0,
                    bitmap.getWidth(), bitmap.getHeight());
            this.bitmap = bitmap;
            isVisHavePixls = true;
        }
    }

    public void setNirRgbInt(int[] nirRgbData) {
        if (nirRgbData == null) {
            return;
        }

        if (nirRgbArray == null) {
            nirRgbArray = new int[nirRgbData.length];
        }

        try {
            System.arraycopy(nirRgbData, 0, nirRgbArray, 0, nirRgbData.length);
            isVisHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }
    }

    public void setRgbInt(int[] argbData) {

        if (argbData == null) {
            return;
        }

        if (mRgbArray == null) {
            mRgbArray = new int[argbData.length];
        }
        try {
            System.arraycopy(argbData, 0, mRgbArray, 0, argbData.length);
            isVisHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }
    }

    private int[] byte2int(byte[] b) {
        // 数组长度对4余数
        int r;
        byte[] copy;
        if ((r = b.length % 4) != 0) {
            copy = new byte[b.length - r + 4];
            System.arraycopy(b, 0, copy, 0, b.length);
        } else {
            copy = b;
        }

        int[] x = new int[copy.length / 4 + 1];
        int pos = 0;
        for (int i = 0; i < x.length - 1; i++) {
            x[i] = (copy[pos] << 24 & 0xff000000) | (copy[pos + 1] << 16 & 0xff0000)
                    | (copy[pos + 2] << 8 & 0xff00) | (copy[pos + 3] & 0xff);
            pos += 4;
        }
        x[x.length - 1] = r;
        return x;
    }

    /**
     * 设置深度图
     *
     * @param irData
     */
    public void setIrData(byte[] irData) {

        if (irData == null) {
            return;
        }
        if (mIrByte == null) {
            mIrByte = new byte[irData.length];
        }
        try {
            System.arraycopy(irData, 0, mIrByte, 0, irData.length);
            isIRHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }
    }


    /**
     * 设置深度图
     *
     * @param depthData
     */
    public void setDepthData(byte[] depthData) {

        if (mDepthArray == null) {
            mDepthArray = new byte[depthData.length];
        }
        try {
            System.arraycopy(depthData, 0, mDepthArray, 0, depthData.length);
            isDepthHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }
    }


    public void clearInfo() {
        try {
            isDepthHavePixls = false;
            isIRHavePixls = false;
            isVisHavePixls = false;
            curFaceID = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float rgbLiveness(int[] data, int width, int height, int[] landmarks) {
        final float rgbScore = mFaceLive.silentLive(FaceLive.LiveType.LIVEID_VIS, data, height, width, landmarks);
        return rgbScore;
    }

    public float irLiveness(byte[] data, int width, int height, int[] landmarks) {
        final float irScore = mFaceLive.silentLive(FaceLive.LiveType.LIVEID_NIR, data, height, width, landmarks);
        return irScore;
    }

    public float depthLiveness(byte[] data, int width, int height, int[] landmarks) {
        final float depthScore = mFaceLive.silentLive(FaceLive.LiveType.LIVEID_DEPTH, data, height, width, landmarks);
        return depthScore;
    }

    public void livenessCheck(final int width, final int height, final int type) {
        if (future != null && !future.isDone()) {
            return;
        }
        future = es.submit(new Runnable() {
            @Override
            public void run() {
                // 如果是注册判断超时
                onLivenessCheck(width, height, type);
            }
        });
    }

    // 活体检测
    private boolean onLivenessCheck(int width, int height, int type) {
        boolean isLiveness = false;

        long startTime = System.currentTimeMillis();
        FaceInfo[] faceInfos = null;
        if (mRgbArray != null) {
            faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(mRgbArray, width, height);
        }
        LivenessModel livenessModel = new LivenessModel();
        livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startTime);
        livenessModel.getImageFrame().setArgb(mRgbArray);
        livenessModel.getImageFrame().setIr(mIrByte);
        livenessModel.getImageFrame().setDepth(mDepthArray);
        livenessModel.getImageFrame().setWidth(width);
        livenessModel.getImageFrame().setHeight(height);
        livenessModel.setLiveType(type);

        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            livenessModel.setLandmarks(faceInfo.landmarks);
            livenessModel.setFaceID(faceInfo.face_id);

            // 返回检测到人脸的事件
            if (faceDetectCallBack != null && faceInfos[0].mConf >= GlobalSet.getDetectConfValue()) {
                faceDetectCallBack.onFaceDetectCallback(true, (int) faceInfo.mWidth,
                        (int) faceInfo.mWidth, (int) faceInfo.mCenter_x, (int) faceInfo.mCenter_y,
                        width, height);
            }

            if (currentTaskType == TASK_TYPE_ONETON && faceInfos[0].face_id == curFaceID) {
                return true;
            }

            return livenessFeatures(livenessModel);
        } else {
            // 返回检测到人脸的事件
            if (faceDetectCallBack != null) {
                faceDetectCallBack.onFaceDetectCallback(false, 0,
                        0, 0, 0, 0, 0);
            }
            if (livenessCallBack != null) {
                livenessCallBack.onCallback(1, null);
                livenessCallBack.onTip(1, "未检测到人脸");
            }
        }
        if (livenessCallBack != null) {
            livenessCallBack.onCanvasRectCallback(livenessModel);
        }
        return isLiveness;
    }

    @Nullable
    private boolean livenessFeatures(final LivenessModel livenessModel) {
        if (future2 != null && !future2.isDone()) {
            return true;
        }

        future2 = es2.submit(new Runnable() {
            @Override
            public void run() {
                float rgbScore = 0;
                if ((livenessModel.getLiveType() & MASK_RGB) == MASK_RGB) {
                    long startTime = System.currentTimeMillis();
                    rgbScore = rgbLiveness(
                            livenessModel.getImageFrame().getArgb(),
                            livenessModel.getImageFrame().getWidth(),
                            livenessModel.getImageFrame().getHeight(),
                            livenessModel.getLandmarks()
                    );
                    livenessModel.setRgbLivenessScore(rgbScore);
                    livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startTime);
                }
                float irScore = 0;
                if ((livenessModel.getLiveType() & MASK_IR) == MASK_IR) {
                    long startTime = System.currentTimeMillis();
                    irScore = irLiveness(
                            livenessModel.getImageFrame().getIr(),
                            livenessModel.getImageFrame().getWidth(),
                            livenessModel.getImageFrame().getHeight(),
                            livenessModel.getLandmarks()
                    );
                    livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startTime);
                    livenessModel.setIrLivenessScore(irScore);
                }
                float depthScore = 0;
                if ((livenessModel.getLiveType() & MASK_DEPTH) == MASK_DEPTH) {
                    long startTime = System.currentTimeMillis();
                    depthScore = depthLiveness(
                            livenessModel.getImageFrame().getDepth(),
                            livenessModel.getImageFrame().getWidth(),
                            livenessModel.getImageFrame().getHeight(),
                            livenessModel.getLandmarks()
                    );
                    livenessModel.setDetphtLivenessDuration(System.currentTimeMillis() - startTime);
                    livenessModel.setDepthLivenessScore(depthScore);
                }
                if (livenessCallBack != null) {
                    // 增加注册功能
                    switch (currentTaskType) {
                        case TASK_TYPE_ONETON:
                            filterFeature(livenessModel);
                            break;
                        case TASK_TYPE_REGIST:
                            registFace(livenessModel);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        return false;
    }

    public boolean onFeatureCheck(LivenessModel livenessModel) {
        if ((GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.NO) ||
                (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB
                        && livenessModel.getRgbLivenessScore() > GlobalSet.getLiveRgbValue()) ||
                (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR
                        && livenessModel.getRgbLivenessScore() > GlobalSet.getLiveRgbValue()
                        && livenessModel.getIrLivenessScore() > GlobalSet.getLiveNirValue()) ||
                (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB_DEPTH
                        && livenessModel.getRgbLivenessScore() > GlobalSet.getLiveRgbValue()
                        && livenessModel.getDepthLivenessScore() > GlobalSet.getLiveDepthValue())
                ) {
            byte[] visFeature = new byte[512];
            long sTime = System.currentTimeMillis();
            float length = FaceSDKManager.getInstance().getFaceFeature().extractFeature(
                    livenessModel.getImageFrame().getArgb(),
                    livenessModel.getImageFrame().getHeight(),
                    livenessModel.getImageFrame().getWidth(),
                    visFeature,
                    livenessModel.getLandmarks());
            livenessModel.setFeatureDuration(System.currentTimeMillis() - sTime);
            if (length == 128) {
                livenessModel.setFeatureByte(visFeature);
                return true;
            }
        }
        return false;
    }

    public void filterFeature(LivenessModel livenessModel) {
        if (onFeatureCheck(livenessModel)) {
            long sTime = System.currentTimeMillis();
            Feature feature = FaceSDKManager.getInstance().getFeature(
                    FaceFeature.FeatureType.FEATURE_VIS, livenessModel.getFeatureByte(), livenessModel);
            livenessModel.setCheckDuration(System.currentTimeMillis() - sTime);
            Log.e("lth_sc", "" + livenessModel.getCheckDuration());
            if (feature != null) {
                curFaceID = livenessModel.getFaceID();
                livenessModel.setFeature(feature);
                livenessCallBack.onCallback(0, livenessModel);
                return;
            }
        }
        livenessCallBack.onCallback(1, livenessModel);
    }

    /**
     * 人脸注册
     *
     * @param livenessModel
     */
    public void registFace(LivenessModel livenessModel) {
        long sTime = System.currentTimeMillis();

        if ((GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.NO)
                || (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB
                && livenessModel.getRgbLivenessScore() > GlobalSet.getLiveRgbValue())
                || (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR
                && livenessModel.getRgbLivenessScore() > GlobalSet.getLiveRgbValue()
                && livenessModel.getIrLivenessScore() > GlobalSet.getLiveNirValue())
                || (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB_DEPTH
                && livenessModel.getRgbLivenessScore() > GlobalSet.getLiveRgbValue()
                && livenessModel.getDepthLivenessScore() > GlobalSet.getLiveDepthValue())) {
            byte[] bytes = new byte[512];
            float length = FaceSDKManager.getInstance().getFaceFeature().extractFeature(
                    livenessModel.getImageFrame().getArgb(),
                    livenessModel.getImageFrame().getHeight(),
                    livenessModel.getImageFrame().getWidth(),
                    bytes,
                    livenessModel.getLandmarks());
            if (length == 128) {
                Feature feature = new Feature();
                feature.setCtime(System.currentTimeMillis());
                feature.setFeature(bytes);
                feature.setUserName(registNickName);
                final String uid = UUID.randomUUID().toString();
                feature.setUserId(uid);
                feature.setGroupId("0");
                // TODO:增加图片
                int imgWidth = livenessModel.getImageFrame().getWidth();
                int imgHeight = livenessModel.getImageFrame().getHeight();
                Bitmap registBmp = registBmp = Bitmap.createBitmap(imgWidth,
                        imgHeight, Bitmap.Config.ARGB_8888);
                registBmp.setPixels(livenessModel.getImageFrame().getArgb(), 0, imgWidth, 0, 0, imgWidth, imgHeight);
                StringBuilder logBuilder = new StringBuilder();
                logBuilder.append("姓名\t图片名\t成功/失败\t失败原因\n");
                // 保存图片
                // 保存图片到新目录中
                File facePicDir = FileUtils.getFacePicDirectory();
                // 保存抠图图片到新目录中
                File faceCropDir = FileUtils.getFaceCropPicDirectory();
                String picFile = "regist_" + uid + "_rgb.png";

                if (facePicDir != null) {
                    File savePicPath = new File(facePicDir, picFile);
                    if (FileUtils.saveFile(savePicPath, registBmp)) {
                        Log.i(TAG, "图片保存成功");
                        feature.setImageName(picFile);
                    }
                }

                Bitmap cropBitmap = null;
                String cropImgName = null;
                // 人脸抠图
                int[] landmarks = livenessModel.getLandmarks();
                if (landmarks != null) {
                    cropBitmap = ImageUtils.noBlackBoundImgCrop(landmarks,
                            livenessModel.getImageFrame().getHeight(), livenessModel.getImageFrame().getWidth(),
                            livenessModel.getImageFrame().getArgb());

                    if (cropBitmap == null) {
                        cropBitmap = registBmp;
                    }

                    cropImgName = "crop_" + picFile;
                }
                if (faceCropDir != null && cropBitmap != null) {
                    File saveCropPath = new File(faceCropDir, cropImgName);
                    if (FileUtils.saveFile(saveCropPath, cropBitmap)) {
                        Log.i(TAG, "抠图图片保存成功");
                        feature.setCropImageName(cropImgName);
                    }
                }

                logBuilder.append(registNickName + "\t" + picFile + "\t" + "成功\n");
                if (FaceApi.getInstance().featureAdd(feature)) {
                    returnRegistResult(0, livenessModel, cropBitmap);
                }
                return;
            }
        }
    }

    /**
     * 返回注册结果给相关的调用方
     *
     * @param code
     * @param livenessModel
     */
    private void returnRegistResult(int code, LivenessModel livenessModel, Bitmap cropBitmap) {
        synchronized (mLock) {
            for (IFaceRegistCalllBack faceRegistCalllBack : registCalllBacks) {
                try {
                    faceRegistCalllBack.onRegistCallBack(code, livenessModel, cropBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void release() {
        if (future != null) {
            future.cancel(true);
            Log.w("ebq-bd","future1的确执行了释放");
        }

        if (future2 != null) {
            future2.cancel(true);
            Log.w("ebq-bd","future2的确执行了释放");
        }
    }
}