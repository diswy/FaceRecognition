package com.baidu.idl.sample.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.iminect.ColorSurfaceView;
import com.baidu.aip.iminect.Jni;
import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.adapter.CameraPagerAdapter;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.ImageUtils;
import com.baidu.idl.sample.view.BaseCameraView;
import com.baidu.idl.sample.view.CircleImageView;
import com.baidu.idl.sample.view.CirclePercentView;
import com.hjimi.api.iminect.ImiCamera;
import com.hjimi.api.iminect.ImiCameraFrame;
import com.hjimi.api.iminect.ImiCameraFrameMode;
import com.hjimi.api.iminect.ImiCameraPixelFormat;
import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiDeviceAttribute;
import com.hjimi.api.iminect.ImiDeviceState;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.ImiFrameType;
import com.hjimi.api.iminect.ImiImageFrame;
import com.hjimi.api.iminect.ImiNect;
import com.hjimi.api.iminect.ImiPixelFormat;
import com.hjimi.api.iminect.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @Time: 2018/12/3
 * @Author: v_chaixiaogang
 * @Description: 华捷艾米镜头RGB+depth活体检测页面
 */
public class IminectPassActivity extends BaseActivity implements
        ILivenessCallBack, View.OnClickListener {

    private static final String TAG = "Iminect";
    private final int DEPTH_NEED_PERMISSION = 33;
    private Context mContext;

    private CircleImageView mImage;
    private TextView mNickNameTv;
    private TextView mSimilariryTv;
    private TextView mNumTv;
    private TextView mDetectTv;
    private TextView mFeatureTv;
    private TextView mLiveTv;
    private TextView mAllTv;

    private ViewPager mViewPager;
    private CameraPagerAdapter mPagerAdapter;
    private BaseCameraView mLayoutOne;
    private RelativeLayout mLayoutTwo;
    private ImageView mBtnGoLeft, mBtnGoRight;
    private ImageView mImageTrack;

    private CirclePercentView mRgbCircleView;
    private CirclePercentView mNirCircleView;
    private CirclePercentView mDepthCircleView;

    // textureView用于绘制人脸框等。
    private TextureView mTextureView;
    private ColorSurfaceView mColorSurfaceView;
    private ColorSurfaceView mDepthSurfaceView;

    private static ImiNect m_ImiNect = null;
    private static IminectPassActivity.MainListener mMainListener = null;
    private ImiDevice mImiDevice0 = null;
    private ImiDeviceAttribute mDeviceAttribute = null;
    private boolean mIsExitLoop = false;
    private ByteBuffer mColorBuffer;
    private ByteBuffer mDepthBuffer;

    private ImiCamera mCamera = null;

    private int expectUserFrameWidth = 640;
    private int expectUserFrameHeight = 480;

    private static final int DEVICE_OPEN_FALIED = 1;
    private static final int DEVICE_DISCONNECT = 2;

    private static final int REQUEST_CAMERA_CODE = 0x007;

    private Bitmap mBitmap;
    private String mUserName;
    private RelativeLayout mLayoutInfo;

    private ImiDeviceState deviceState = ImiDeviceState.IMI_DEVICE_STATE_CONNECT;

    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DEVICE_OPEN_FALIED:
                    showMessageDialog();
                    break;
                case DEVICE_DISCONNECT:
                    showMessageDialog();
                    break;
            }
        }
    };

    private void showMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("The device is not connected!!!");
        builder.setPositiveButton("quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    class MainListener implements ImiCamera.OpenCameraListener, ImiDevice.OpenDeviceListener,
            ImiDevice.DeviceStateListener {

        @Override
        public void onOpenCameraSuccess() {
            mCamera = m_ImiNect.Camera();
            m_ImiNect.Device().open(mMainListener);
            Log.e("gangzi", "------onOpenCameraSuccess");
        }

        @Override
        public void onOpenCameraFailed(String errorMsg) {
            mainHandler.sendEmptyMessage(DEVICE_OPEN_FALIED);
            Log.e("gangzi", "------onOpenCameraFailed");
        }

        @Override
        public void onOpenDeviceSuccess() {
            mImiDevice0 = m_ImiNect.Device();
            mDeviceAttribute = mImiDevice0.getAttribute();

            try {
                setUserExpectMode(expectUserFrameWidth, expectUserFrameHeight);
            } catch (Exception e) {
                Log.e(TAG, "setUserExpectMode: falied, invalid frame mode");
            }

            mImiDevice0.setImageRegistration(true); // set image registration.

            new Thread(new ColorViewRefreshRunnable()).start();
        }

        @Override
        public void onOpenDeviceFailed(String errorMsg) {
            mainHandler.sendEmptyMessage(DEVICE_OPEN_FALIED);
        }

        @Override
        public void onDeviceStateChanged(String deviceUri, ImiDeviceState state) {
            if (state == ImiDeviceState.IMI_DEVICE_STATE_DISCONNECT) {
                if (mDeviceAttribute != null && mDeviceAttribute.getUri().equals(deviceUri)) {
                    deviceState = ImiDeviceState.IMI_DEVICE_STATE_DISCONNECT;
                    mainHandler.sendEmptyMessage(DEVICE_DISCONNECT);
                }
            } else if (state == ImiDeviceState.IMI_DEVICE_STATE_CONNECT) {
                if (mDeviceAttribute != null && mDeviceAttribute.getUri().equals(deviceUri)) {
                    deviceState = ImiDeviceState.IMI_DEVICE_STATE_CONNECT;
                }
            }
        }
    }

    private void setUserExpectMode(int width, int height) {
        ImiFrameMode frameMode = new ImiFrameMode(ImiPixelFormat.IMI_PIXEL_FORMAT_DEP_16BIT, width, height);
        mImiDevice0.setFrameMode(ImiFrameType.DEPTH, frameMode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_iminect);
        mContext = this;
        initView();
        initData();
    }

    private void initView() {

        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.pass_1_n);

        mImage = findViewById(R.id.image);
        mNickNameTv = findViewById(R.id.tv_nick_name);
        mSimilariryTv = findViewById(R.id.tv_similarity);
        mNumTv = findViewById(R.id.tv_num);
        mDetectTv = findViewById(R.id.tv_detect);
        mFeatureTv = findViewById(R.id.tv_feature);
        mLiveTv = findViewById(R.id.tv_live);
        mAllTv = findViewById(R.id.tv_all);
        mImageTrack = findViewById(R.id.image_track);

        mRgbCircleView = findViewById(R.id.circle_rgb_live);
        mNirCircleView = findViewById(R.id.circle_nir_live);
        mDepthCircleView = findViewById(R.id.circle_depth_live);

        mLayoutInfo = findViewById(R.id.layout_info);
        initCamera();
    }

    private void initCamera() {
        // 计算并适配显示图像容器的宽高
        String newPix = DensityUtil.calculateCameraOrbView(mContext);
        String[] newPixs = newPix.split(" ");
        int newWidth = Integer.parseInt(newPixs[0]);
        int newHeight = Integer.parseInt(newPixs[1]);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(newWidth, newHeight);


        mViewPager = findViewById(R.id.viewpage);

        mLayoutOne = new BaseCameraView(mContext);
        mLayoutOne.setGravity(Gravity.CENTER);
        mLayoutTwo = new RelativeLayout(mContext);
        mLayoutTwo.setGravity(Gravity.CENTER);

        List<View> viewList = new ArrayList<>();
        viewList.add(mLayoutOne);
        viewList.add(mLayoutTwo);
        mPagerAdapter = new CameraPagerAdapter(viewList);
        mViewPager.setAdapter(mPagerAdapter);


        mBtnGoLeft = findViewById(R.id.btn_go_left);
        mBtnGoLeft.setOnClickListener(this);
        mBtnGoRight = findViewById(R.id.btn_go_right);
        mBtnGoRight.setOnClickListener(this);


        mTextureView = new TextureView(mContext);
        mTextureView.setOpaque(false);
        mTextureView.setKeepScreenOn(true);
        mColorSurfaceView = new ColorSurfaceView(mContext);

        // 创建一个布局放入mTextureView
        RelativeLayout rl = new RelativeLayout(mContext);
        rl.setGravity(Gravity.CENTER);
        rl.addView(mTextureView, lp);
        rl.addView(mColorSurfaceView, lp);
        // 将创建的布局放入mLayoutOne
        mLayoutOne.addView(rl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        // 调用BaseCameraView添加人脸识别框
        mLayoutOne.initFaceFrame(mContext);
        // mTextureView的绘制完毕监听，用于将左边距传入BaseCameraView
        mTextureView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int left = mTextureView.getLeft();
                mLayoutOne.leftDisparity(left);
            }
        });


        mDepthSurfaceView = new ColorSurfaceView(mContext);
        mLayoutTwo.addView(mDepthSurfaceView, lp);

        m_ImiNect = ImiNect.create(IminectPassActivity.this);
        mMainListener = new IminectPassActivity.MainListener();
        m_ImiNect.Device().addDeviceStateListener(mMainListener);
        if (isCameraPermission(IminectPassActivity.this, REQUEST_CAMERA_CODE)) {
            m_ImiNect.Camera().open(mMainListener);
        }
    }

    private void initData() {
        int num = FaceSDKManager.getInstance().setFeature();
        mNumTv.setText(String.format("底库人脸数: %s 个", num));

        FaceSDKManager.getInstance().getFaceLiveness().setLivenessCallBack(this);
    }

    public boolean isCameraPermission(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                Toast.makeText(this, "requestPermissions Camera Permission",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Toast.makeText(this, "Already have Camera Permission", Toast.LENGTH_SHORT).show();
        return true;
    }

    private class ColorViewRefreshRunnable implements Runnable {

        @Override
        public void run() {
            try {
                ImiCameraFrameMode frameMode = new ImiCameraFrameMode(
                        ImiCameraPixelFormat.IMI_CAMERA_PIXEL_FORMAT_RGB888,
                        640, 480, 30);
                mCamera.startPreview(frameMode);

                ImiFrameMode depthFrameMode = new ImiFrameMode(ImiPixelFormat.IMI_PIXEL_FORMAT_DEP_16BIT,
                        640, 480);
                mImiDevice0.setFrameMode(ImiFrameType.DEPTH, depthFrameMode);
                mImiDevice0.openStream(ImiFrameType.DEPTH);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            ImiCameraFrame colorFrame = null;
            ImiImageFrame depthFrame = null;
            ByteBuffer depthByteBuf = null;
            ByteBuffer colorByteBuf = null;
            Jni jni = new Jni();

            while (!mIsExitLoop) {

                if (depthFrame == null) {
                    depthFrame = mImiDevice0.readNextFrame(ImiFrameType.DEPTH, 40);
                }

                if (colorFrame == null) {
                    colorFrame = mCamera.readNextFrame(40);
                }
                if (null == depthFrame || null == colorFrame) {
                    continue;
                }

                mColorBuffer = colorFrame.getData();
                mDepthBuffer = depthFrame.getData();
                ByteBuffer depthframeData;
                depthframeData = Utils.depth2RGB888(depthFrame, true, false);
                int rgbLen = mColorBuffer.remaining();
                byte[] rgbByte = new byte[rgbLen];
                mColorBuffer.get(rgbByte);
                final Bitmap bitmap = ImageUtils.RGB2Bitmap(rgbByte, 640, 480);
                // 用于显示送检时的图片，建议注释掉
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mImageTrack.setImageBitmap(bitmap);
//                    }
//                });
                mColorSurfaceView.updateVertices(mColorBuffer);
                mDepthSurfaceView.updateVertices(depthframeData);
                int depthLen = mDepthBuffer.remaining();
                byte[] depthByte = new byte[depthLen];
                mDepthBuffer.get(depthByte);
                FaceSDKManager.getInstance().getFaceLiveness().setRgbBitmap(bitmap);
                FaceSDKManager.getInstance().getFaceLiveness().setDepthData(depthByte);
                FaceSDKManager.getInstance().getFaceLiveness().livenessCheck(640, 480, 0X0101);
                colorFrame = null;
                depthFrame = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsExitLoop = true;
        if (mImiDevice0 != null) {
            mImiDevice0.closeStream(ImiFrameType.DEPTH);
            mImiDevice0.close();
            mImiDevice0 = null;
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.close();
            mCamera = null;
        }
        m_ImiNect.destroy();
        // 摄像头重启情况之前记录状态
        FaceSDKManager.getInstance().getFaceLiveness().clearInfo();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_go_left) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(0);
            }

        } else if (id == R.id.btn_go_right) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(1);
            }
        }
    }

    @Override
    public void onTip(int code, String msg) {

    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

    @Override
    public void onCallback(final int code, final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectTv.setText(String.format("人脸检测耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getRgbDetectDuration()));
                mFeatureTv.setText(String.format("特征提取耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getFeatureDuration()));
                mLiveTv.setText(String.format("活体检测耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getLiveDuration()));
                mAllTv.setText(String.format("1:N人脸检索耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getCheckDuration()));

                mRgbCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getRgbLivenessScore());

                mNirCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getIrLivenessScore());

                mDepthCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getDepthLivenessScore());

                if (livenessModel == null) {
                    mLayoutInfo.setVisibility(View.INVISIBLE);

                } else {
                    mLayoutInfo.setVisibility(View.VISIBLE);
                    if (code == 0) {
                        Feature feature = livenessModel.getFeature();
                        mSimilariryTv.setText(String.format("相似度: %s", livenessModel.getFeatureScore()));
                        mNickNameTv.setText(String.format("%s，你好！", feature.getUserName()));

                        if (!TextUtils.isEmpty(mUserName) && feature.getUserName().equals(mUserName)) {
                            mImage.setImageBitmap(mBitmap);
                        } else {
                            String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                                    + "/" + feature.getCropImageName();
                            Bitmap bitmap = com.baidu.idl.sample.utils.Utils.getBitmapFromFile(imgPath);
                            mImage.setImageBitmap(bitmap);
                            mBitmap = bitmap;
                            mUserName = feature.getUserName();
                        }
                    } else {
                        mSimilariryTv.setText("未匹配到相似人脸");
                        mNickNameTv.setText("陌生访客，请先注册");
                        mImage.setImageResource(R.mipmap.preview_image_angle);
                    }
                }

            }
        });
    }
}
