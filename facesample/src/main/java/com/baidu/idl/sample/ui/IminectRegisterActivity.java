package com.baidu.idl.sample.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.iminect.ColorSurfaceView;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.adapter.CameraPagerAdapter;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.callback.IFaceRegistCalllBack;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.manager.FaceLiveness;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.ImageUtils;
import com.baidu.idl.sample.utils.ToastUtils;
import com.baidu.idl.sample.view.BaseCameraView;
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
 * 华捷艾米镜头 RGB+depth 注册页面
 * Created by v_liujialu01 on 2019/1/4.
 */

public class IminectRegisterActivity extends BaseActivity implements View.OnClickListener, ILivenessCallBack {
    private static final String TAG = IminectRegisterActivity.class.getSimpleName();

    // 摄像头预览相关控件
    private ViewPager mViewPager;
    private CameraPagerAdapter mPagerAdapter;
    private BaseCameraView mLayoutOne;
    private RelativeLayout mLayoutTwo;
    private ImageView mBtnGoLeft;
    private ImageView mBtnGoRight;

    // 注册相关控件
    private RelativeLayout mLayoutInput;
    private EditText mNickView;
    private RelativeLayout mCameraView;
    private View registResultView;
    private TextView mTextBatchRegister;

    // textureView用于绘制人脸框等。
    private TextureView mTextureView;
    private ColorSurfaceView mColorSurfaceView;
    private ColorSurfaceView mDepthSurfaceView;

    private static ImiNect m_ImiNect = null;
    private static MainListener mMainListener = null;
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
    private static final int DEPTH_NEED_PERMISSION = 33;

    private Context mContext;
    private String mNickName;
    private boolean mIsCameraPermission;
    private boolean mInitOk;

    private Handler handler = new Handler();
    private Thread mThread = null;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            faceRegistCalllBack.onRegistCallBack(1, null, null);
        }
    };

    private Handler mMainHandler = new Handler() {
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
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iminect_register);
        mContext = this;
        initView();
        setAction();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FaceSDKManager.getInstance().getFaceLiveness().removeRegistCallBack(faceRegistCalllBack);
        if (mMainListener != null) {
            m_ImiNect.Device().removeDeviceStateListener(mMainListener);
            mMainListener = null;
        }

        if (m_ImiNect != null) {
            m_ImiNect.destroy();
            m_ImiNect = null;
        }

        if (mContext != null) {
            mContext = null;
        }
        release();
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.face_regiseter);

        mLayoutInput = findViewById(R.id.layout_input);
        mNickView = findViewById(R.id.nick_name);
        registResultView = findViewById(R.id.regist_result);
        mTextBatchRegister = findViewById(R.id.text_batch_register);
        mCameraView = findViewById(R.id.layout_camera);
        findViewById(R.id.go_btn).setOnClickListener(this);
        mTextBatchRegister.setOnClickListener(this);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mViewPager = findViewById(R.id.viewpager);
        mBtnGoLeft = findViewById(R.id.btn_go_left);
        mBtnGoRight = findViewById(R.id.btn_go_right);
        mLayoutOne = new BaseCameraView(mContext);
        mLayoutTwo = new RelativeLayout(mContext);
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
    }

    private void setAction() {
        // 注册人脸注册事件
        FaceSDKManager.getInstance().getFaceLiveness().addRegistCallBack(faceRegistCalllBack);
        mInitOk = true;

        // 设置完成事件
        registResultView.findViewById(R.id.complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handler.postDelayed(runnable, 1000 * 30);
    }

    private void initData() {
        FaceSDKManager.getInstance().getFaceLiveness()
                .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_REGIST);

        m_ImiNect = ImiNect.create(mContext);
        mMainListener = new MainListener();
        m_ImiNect.Device().addDeviceStateListener(mMainListener);
        mIsCameraPermission = isCameraPermission(IminectRegisterActivity.this,
                REQUEST_CAMERA_CODE);

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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_go_left) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(0);
            }

        } else if (id == R.id.btn_go_right) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(1);
            }
        }

        if (id == R.id.go_btn) {
            Editable editable = mNickView.getText();
            if (editable != null && editable.length() > 0) {
                mNickName = mNickView.getText().toString();
                String nameResult = FaceApi.getInstance().isValidName(mNickName);
                if ("0".equals(nameResult)) {
                    if (mIsCameraPermission) {
                        // 打开摄像头
                        m_ImiNect.Camera().open(mMainListener);
                    }
                    // 设置注册时的昵称
                    FaceSDKManager.getInstance().getFaceLiveness().setRegistNickName(mNickName);
                    // 隐藏键盘
                    com.baidu.idl.sample.utils.Utils.hideKeyboard((Activity) mContext);
                    mLayoutInput.setVisibility(View.GONE);
                    mCameraView.setVisibility(View.VISIBLE);
                } else {
                    ToastUtils.toast(mContext, nameResult);
                }
            }
        }

        if (id == R.id.text_batch_register) {    // 批量注册
            Intent intent = new Intent(mContext, BatchImportActivity.class);
            startActivity(intent);
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

    // 注册结果
    private IFaceRegistCalllBack faceRegistCalllBack = new IFaceRegistCalllBack() {

        @Override
        public void onRegistCallBack(int code, LivenessModel livenessModel, final Bitmap cropBitmap) {
            if (handler != null) {
                handler.removeCallbacks(runnable);
                handler = null;
                runnable = null;
            }

            // 停止摄像头采集
            registResultView.post(new Runnable() {
                @Override
                public void run() {
                    release();   // 释放
                    mLayoutInput.setVisibility(View.GONE);
                    mTextBatchRegister.setVisibility(View.GONE);
                    registResultView.setVisibility(View.VISIBLE);
                    mCameraView.setVisibility(View.GONE);
                }
            });
            switch (code) {
                case 0:
                    // 注册成功，显示注册信息
                    registResultView.post(new Runnable() {
                        @Override
                        public void run() {
                            (registResultView.findViewById(R.id.ic_right))
                                    .setBackground(getDrawable(R.mipmap.ic_success));
                            if (cropBitmap != null) {
                                ((ImageView) registResultView.findViewById(R.id.ic_portrait))
                                        .setImageBitmap(cropBitmap);
                            }
                            ((TextView) registResultView.findViewById(R.id.nick_name))
                                    .setText(mNickName);
                            (registResultView.findViewById(R.id.result))
                                    .setVisibility(View.VISIBLE);
                            ((TextView) registResultView.findViewById(R.id.complete))
                                    .setText("确定");
                        }
                    });
                    break;
                case 1: // 注册超时
                    registResultView.post(new Runnable() {
                        @Override
                        public void run() {
                            (registResultView.findViewById(R.id.ic_right))
                                    .setVisibility(View.INVISIBLE);
                            (registResultView.findViewById(R.id.ic_portrait))
                                    .setBackground(getDrawable(R.mipmap.ic_track));
                            ((TextView) registResultView.findViewById(R.id.nick_name))
                                    .setText("注册超时");
                            (registResultView.findViewById(R.id.result))
                                    .setVisibility(View.GONE);
                            ((TextView) registResultView.findViewById(R.id.complete))
                                    .setText("确定");
                            registResultView.setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    private class MainListener implements ImiCamera.OpenCameraListener, ImiDevice.OpenDeviceListener,
            ImiDevice.DeviceStateListener {

        @Override
        public void onOpenCameraSuccess() {
            mCamera = m_ImiNect.Camera();
            m_ImiNect.Device().open(mMainListener);
            Log.e("gangzi", "------onOpenCameraSuccess");
        }

        @Override
        public void onOpenCameraFailed(String errorMsg) {
            mMainHandler.sendEmptyMessage(DEVICE_OPEN_FALIED);
            Log.e("gangzi", "------onOpenCameraFailed");
        }

        @Override
        public void onOpenDeviceSuccess() {
            Log.e("gangzi", "------onOpenDeviceSuccess");
            mImiDevice0 = m_ImiNect.Device();
            mDeviceAttribute = mImiDevice0.getAttribute();

            try {
                setUserExpectMode(expectUserFrameWidth, expectUserFrameHeight);
            } catch (Exception e) {
                Log.e(TAG, "setUserExpectMode: falied, invalid frame mode");
            }

            mImiDevice0.setImageRegistration(true); // set image registration.

            if (mThread == null) {
                mThread = new Thread(new ColorViewRefreshRunnable());
            }
            mThread.start();
        }

        @Override
        public void onOpenDeviceFailed(String errorMsg) {
            mMainHandler.sendEmptyMessage(DEVICE_OPEN_FALIED);
        }

        @Override
        public void onDeviceStateChanged(String deviceUri, ImiDeviceState state) {
            if (state == ImiDeviceState.IMI_DEVICE_STATE_DISCONNECT) {
                if (mDeviceAttribute != null && mDeviceAttribute.getUri().equals(deviceUri)) {
                    mMainHandler.sendEmptyMessage(DEVICE_DISCONNECT);
                }
            } else if (state == ImiDeviceState.IMI_DEVICE_STATE_CONNECT) {
                if (mDeviceAttribute != null && mDeviceAttribute.getUri().equals(deviceUri)) {
                    Log.i(TAG, "IMI_DEVICE_STATE_CONNECT");
                }
            }
        }
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

            while (!mIsExitLoop) {
                if (depthFrame == null) {
                    depthFrame = mImiDevice0.readNextFrame(ImiFrameType.DEPTH, 40);
                }

                if (colorFrame == null && mCamera != null) {
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

    private void setUserExpectMode(int width, int height) {
        ImiFrameMode frameMode = new ImiFrameMode(ImiPixelFormat.IMI_PIXEL_FORMAT_DEP_16BIT, width, height);
        mImiDevice0.setFrameMode(ImiFrameType.DEPTH, frameMode);
    }

    /**
     * 释放
     */
    private void release() {
        if (mInitOk) {
            mIsExitLoop = true;
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }

            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.close();
                mCamera = null;
            }

            if (mImiDevice0 != null) {
                mImiDevice0.closeStream(ImiFrameType.DEPTH);
                mImiDevice0.close();
                mImiDevice0 = null;
            }

            FaceSDKManager.getInstance().getFaceLiveness().release();
            // 重置状态为默认状态
            FaceSDKManager.getInstance().getFaceLiveness()
                    .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_ONETON);
            // 摄像头重启情况之前记录状态
            FaceSDKManager.getInstance().getFaceLiveness().clearInfo();
            mInitOk = false;
        }
    }
}
