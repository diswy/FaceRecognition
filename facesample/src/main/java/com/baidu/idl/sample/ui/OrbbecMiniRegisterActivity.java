package com.baidu.idl.sample.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
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
import com.orbbec.view.OpenGLView;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 奥比中光mini RGB+depth 注册页面
 * Created by v_liujialu01 on 2019/1/7.
 */

public class OrbbecMiniRegisterActivity extends BaseActivity implements View.OnClickListener,
        OpenNIHelper.DeviceOpenListener, ILivenessCallBack,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "OrbbecMiniRegister";
    private final int depthNeedPermission = 33;

    // 摄像头预览相关控件
    private ViewPager mViewPager;
    private CameraPagerAdapter mPagerAdapter;
    private BaseCameraView mLayoutOne;
    private RelativeLayout mLayoutTwo;
    private ImageView mBtnGoLeft;
    private ImageView mBtnGoRight;
    private View mViewBg;

    // 注册相关控件
    private RelativeLayout mLayoutInput;
    private EditText mNickView;
    private View registResultView;
    private TextView mTextBatchRegister;

    private OpenGLView mDepthGLView;
    private OpenGLView mRgbGLView;
    private TextureView mTextureView;

    private Context mContext;
    private boolean initOk = false;
    private Device device;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream depthStream;
    private VideoStream rgbStream;

    private int mWidth = com.orbbec.utils.GlobalDef.RESOLUTION_X;
    private int mHeight = com.orbbec.utils.GlobalDef.RESOLUTION_Y;
    private Bitmap depthBitmap;
    private float rgbThresholdValue;
    private float depthThresholdValue;

    private Object sync = new Object();
    private boolean exit = false;

    private String mNickName;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            faceRegistCalllBack.onRegistCallBack(1, null, null);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orbbec_register);
        mContext = this;
        initView();
        setAction();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
        // 摄像头重启情况之前记录状态
        FaceSDKManager.getInstance().getFaceLiveness().clearInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (rgbStream != null) {
            rgbStream.stop();
            rgbStream = null;
        }
        if (depthStream != null) {
            depthStream.stop();
            depthStream.stop();
        }
        if (depthBitmap != null) {
            depthBitmap = null;
        }
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.face_regiseter);

        mLayoutInput = findViewById(R.id.layout_input);
        mNickView = findViewById(R.id.nick_name);
        registResultView = findViewById(R.id.regist_result);
        mTextBatchRegister = findViewById(R.id.text_batch_register);
        mViewBg = findViewById(R.id.view_bg);
        findViewById(R.id.go_btn).setOnClickListener(this);
        mTextBatchRegister.setOnClickListener(this);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mViewPager = findViewById(R.id.viewpager);
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

        // 创建一个布局放入mTextureView
        RelativeLayout rl = new RelativeLayout(mContext);
        rl.setGravity(Gravity.CENTER);
        rl.addView(mTextureView, lp);

        mRgbGLView = new OpenGLView(mContext);
        rl.addView(mRgbGLView, lp);
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

        mDepthGLView = new OpenGLView(mContext);
        mLayoutTwo.addView(mDepthGLView, lp);

        mOpenNIHelper = new OpenNIHelper(this);
    }

    private void setAction() {
        FaceSDKManager.getInstance().getFaceLiveness()
                .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_REGIST);
        FaceSDKManager.getInstance().getFaceLiveness().setLivenessCallBack(this);

        // 注册人脸注册事件
        FaceSDKManager.getInstance().getFaceLiveness().addRegistCallBack(faceRegistCalllBack);

        // 设置完成事件
        registResultView.findViewById(R.id.complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handler.postDelayed(runnable, 1000 * 30);
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

        if (id == R.id.go_btn) {
            Editable editable = mNickView.getText();
            if (editable != null) {
                mNickName = mNickView.getText().toString();
                String nameResult = FaceApi.getInstance().isValidName(mNickName);
                if ("0".equals(nameResult)) {
                    // 设置注册时的昵称
                    FaceSDKManager.getInstance().getFaceLiveness().setRegistNickName(mNickName);
                    // 隐藏键盘
                    com.baidu.idl.sample.utils.Utils.hideKeyboard((Activity) mContext);
                    mOpenNIHelper.requestDeviceOpen(this);
                    mLayoutInput.setVisibility(View.GONE);
                    mViewBg.setVisibility(View.GONE);
                    mTextBatchRegister.setVisibility(View.GONE);
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
    public void onDeviceOpened(UsbDevice device) {
        init(device);

        depthStream = VideoStream.create(this.device, SensorType.DEPTH);
        List<VideoMode> mVideoModes = depthStream.getSensorInfo().getSupportedVideoModes();

        for (VideoMode mode : mVideoModes) {
            int X = mode.getResolutionX();
            int Y = mode.getResolutionY();
            int fps = mode.getFps();
            if (X == mWidth && Y == mHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                depthStream.setVideoMode(mode);
                break;
            }
        }
        rgbStream = VideoStream.create(this.device, SensorType.COLOR);
        List<VideoMode> mColorVideoModes = rgbStream.getSensorInfo().getSupportedVideoModes();

        for (VideoMode mode : mColorVideoModes) {
            int X = mode.getResolutionX();
            int Y = mode.getResolutionY();
            int fps = mode.getFps();

            if (X == mWidth && Y == mHeight && mode.getPixelFormat() == PixelFormat.RGB888) {
                rgbStream.setVideoMode(mode);
                break;
            }
        }
        startData();
    }

    @Override
    public void onDeviceOpenFailed(String s) {
        showAlertAndExit("Open Device failed: " + s);
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

        if (requestCode == depthNeedPermission) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void init(UsbDevice device) {
        OpenNI.setLogAndroidOutput(false);
        OpenNI.setLogMinSeverity(0);
        OpenNI.initialize();

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            ToastUtils.toast(mContext, " openni enumerateDevices 0 devices");
            return;
        }

        this.device = null;
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.device = Device.open();
                break;
            }
        }
        if (this.device == null) {
            ToastUtils.toast(mContext, " openni open devices failed: " + device.getDeviceName());
            return;
        }
    }

    private void startData() {
        initOk = true;
        thread = new Thread() {
            @Override
            public void run() {
                List<VideoStream> streams = new ArrayList<>();
                streams.add(rgbStream);
                streams.add(depthStream);
                depthStream.start();
                rgbStream.start();
                while (!exit) {
                    try {
                        OpenNI.waitForAnyStream(streams, 2000);
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }
                    synchronized (sync) {
                        mDepthGLView.update(depthStream, com.orbbec.utils.GlobalDef.TYPE_DEPTH);
                        mRgbGLView.update(rgbStream, com.orbbec.utils.GlobalDef.TYPE_COLOR);

                        ByteBuffer depthByteBuf = depthStream.readFrame().getData();
                        ByteBuffer colorByteBuf = rgbStream.readFrame().getData();
                        int depthLen = depthByteBuf.remaining();
                        int rgbLen = colorByteBuf.remaining();

                        byte[] depthByte = new byte[depthLen];
                        byte[] rgbByte = new byte[rgbLen];

                        depthByteBuf.get(depthByte);
                        colorByteBuf.get(rgbByte);

                        final Bitmap bitmap = ImageUtils.RGB2Bitmap(rgbByte, mWidth, mHeight);

                        FaceSDKManager.getInstance().getFaceLiveness().setRgbBitmap(bitmap);
                        FaceSDKManager.getInstance().getFaceLiveness().setDepthData(depthByte);
                        FaceSDKManager.getInstance().getFaceLiveness().livenessCheck(mWidth, mHeight, 0X0101);
                    }
                }
            }
        };
        thread.start();
    }

    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    // 释放摄像头
    private void releaseCamera() {
        if (initOk) {
            exit = true;
            FaceSDKManager.getInstance().getFaceLiveness().removeRegistCallBack(faceRegistCalllBack);
            // 重置状态为默认状态
            FaceSDKManager.getInstance().getFaceLiveness()
                    .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_ONETON);

            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (depthStream != null) {
                depthStream.stop();
            }
            if (rgbStream != null) {
                rgbStream.stop();
            }

            if (device != null) {
                device.close();
            }

            if (mOpenNIHelper != null) {
                mOpenNIHelper.shutdown();
            }
            initOk = false;
        }
    }

    // 注册结果回调
    private IFaceRegistCalllBack faceRegistCalllBack = new IFaceRegistCalllBack() {

        @Override
        public void onRegistCallBack(int code, LivenessModel livenessModel, final Bitmap cropBitmap) {
            handler.removeCallbacks(runnable);
            // 停止摄像头采集
            registResultView.post(new Runnable() {
                @Override
                public void run() {
                    // 释放摄像头
                    releaseCamera();
                    mLayoutInput.setVisibility(View.GONE);
                    mTextBatchRegister.setVisibility(View.GONE);
                    registResultView.setVisibility(View.VISIBLE);
                    mViewBg.setVisibility(View.VISIBLE);
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
}
