package com.baidu.idl.sample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.Utils;

import static com.baidu.idl.sample.common.GlobalSet.PERFER_HEIGH;
import static com.baidu.idl.sample.common.GlobalSet.PREFER_WIDTH;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_TPREVIEW_NINETY_ANGLE;

/**
 * Created by litonghui on 2018/12/8.
 */

public class MonocularView extends BaseCameraView implements ILivenessCallBack {
    private Preview mPreview;
    private Camera mCamera;
    private int mCameraNum;
    private int previewAngle;
    private int trackAngle;

    private TextureView mTextureViewOne;
    private SurfaceView mSurfaceOne;
    protected ImageView faceFrameImg = null;

    private volatile int[] rgbData;
    private int mTrackImageWidth;
    private int mTrackImageHeight;

    private Context context;

    private ILivenessCallBack livenessCallBack;

    public MonocularView(Context context) {
        this(context, null);
    }

    public MonocularView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonocularView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initview(context);
        initFaceFrame(context);
    }

    public void setImageView(ImageView imageView) {
        faceFrameImg = imageView;
    }

    private void initview(Context context) {
        this.context = context;

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mTextureViewOne = new TextureView(context);
        mTextureViewOne.setOpaque(false);
        addView(mTextureViewOne, lp);

        mSurfaceOne = new SurfaceView(context);
        addView(mSurfaceOne, lp);


        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum == 0) {
            Toast.makeText(context, "未检测到摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            mPreview = new Preview(context, mSurfaceOne);
        }

        previewAngle = PreferencesUtil.getInt(TYPE_PREVIEW_ANGLE, TYPE_TPREVIEW_NINETY_ANGLE);
        // trackAngle = PreferencesUtil.getInt(TYPE_TRACK_ANGLE, TYPE_TRACK_TWO_HUNDERED_SEVENTY_ANGLE);
        // 为适配在手机中显示的对应正确人脸可以正确检测到，当预览角度是90、270度时，检测角度与之相反，
        // 送检的宽高也相反；
        // 当预览角度是0、180度时，检测角度与之相同，送检的宽高也相同。
        if (previewAngle == 90) {
            trackAngle = 270;
            mTrackImageWidth = PERFER_HEIGH;
            mTrackImageHeight = PREFER_WIDTH;
        } else if (previewAngle == 270) {
            trackAngle = 90;
            mTrackImageWidth = PERFER_HEIGH;
            mTrackImageHeight = PREFER_WIDTH;
        } else {
            trackAngle = previewAngle;
            mTrackImageWidth = PREFER_WIDTH;
            mTrackImageHeight = PERFER_HEIGH;
        }
        FaceSDKManager.getInstance().getFaceLiveness().setLivenessCallBack(this);
    }

    public void setLivenessCallBack(ILivenessCallBack livenessCallBack) {
        this.livenessCallBack = livenessCallBack;
    }

    public void onResume() {
        if (mCameraNum == 0) {
            Toast.makeText(context, "未检测到摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                Camera.CameraInfo info = new Camera.CameraInfo();
                int numCameras = Camera.getNumberOfCameras();
                for (int i = 0; i < numCameras; i++) {
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCamera = Camera.open(i);
                        break;
                    }
                }
                // 如果没有前置摄像头，则打开默认的后置摄像头
                if (mCamera == null) {
                    mCamera = Camera.open(0);
                }
                // 没有摄像头时，抛出异常
                if (mCamera == null) {
                    throw new RuntimeException("Unable to open camera");
                }

//                if (previewAngle == 90) {
//                    mCamera.setDisplayOrientation(90);
//                } else if (previewAngle == 270) {
//                    mCamera.setDisplayOrientation(270);
//                } else {
//                    mCamera.setDisplayOrientation(previewAngle);
//                }
                mCamera.setDisplayOrientation(previewAngle);
                mPreview.setCamera(mCamera, PREFER_WIDTH, PERFER_HEIGH);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealRgb(data);
                    }
                });
            } catch (RuntimeException e) {
                // Log.e(TAG, e.getMessage());
            }
        }

        if (mTextureViewOne != null) {
            mTextureViewOne.setOpaque(false);
        }
    }

    public void onPause() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        // 摄像头重启情况之前记录状态
        FaceSDKManager.getInstance().getFaceLiveness().clearInfo();
    }

    public void onBDPreviewPause() {

        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (mPreview != null) {
            mPreview.release();
        }
        // 摄像头重启情况之前记录状态
        FaceSDKManager.getInstance().getFaceLiveness().clearInfo();
    }

    public void onBDPreviewResume() {
        onResume();
    }


    private void dealRgb(byte[] data) {
        if (rgbData == null) {
            final int[] argb = new int[PREFER_WIDTH * PERFER_HEIGH];
            int mirror; // 判断是否镜像
            // 如果预览角度是90、270度（竖屏显示），则需要镜像
            if (previewAngle == 90 || previewAngle == 270) {
                mirror = 1;
            } else { // 如果预览角度是0、180度（横屏显示），则不需要镜像
                mirror = 0;
            }
            FaceSDKManager.getInstance().getFaceDetector().yuvToARGB(data,
                    PREFER_WIDTH, PERFER_HEIGH, argb, trackAngle, mirror);
            rgbData = argb;
            // 保存送检时图片
            myBitmap = Utils.getBitmap(argb, mTrackImageHeight, mTrackImageWidth);
//            post(new Runnable() {
//                @Override
//                public void run() {
//                    if (faceFrameImg == null) {
//                        return;
//                    }
//                    // 用于显示送检时的图片，建议注释掉
//                    faceFrameImg.setImageBitmap(Utils.getBitmap(argb, mTrackImageHeight, mTrackImageWidth));
//                }
//            });
        }
        checkData();
    }

    private synchronized void checkData() {
        if (rgbData != null) {
            FaceSDKManager.getInstance().getFaceLiveness().setNirRgbInt(null);
            FaceSDKManager.getInstance().getFaceLiveness().setRgbInt(rgbData);
            FaceSDKManager.getInstance().getFaceLiveness().setIrData(null);
            FaceSDKManager.getInstance().getFaceLiveness().livenessCheck(mTrackImageWidth, mTrackImageHeight,
                    GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB ? 0X0001 : 0X0000);
            rgbData = null;
        }
    }

    @Override
    public void onTip(int code, String msg) {

    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

    @Override
    public void onCallback(int code, LivenessModel livenessModel) {
        if (livenessCallBack != null) {
            livenessCallBack.onCallback(code, livenessModel);
        }
    }

    private Bitmap myBitmap;

    public Bitmap getMyBitmap() {
        return myBitmap;
    }


}