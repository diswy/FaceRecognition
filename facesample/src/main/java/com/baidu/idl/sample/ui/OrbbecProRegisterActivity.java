package com.baidu.idl.sample.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.adapter.CameraPagerAdapter;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.callback.IFaceRegistCalllBack;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.manager.FaceLiveness;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.ToastUtils;
import com.baidu.idl.sample.view.BaseCameraView;
import com.orbbec.Native.DepthUtils;
import com.orbbec.obDepth2.HomeKeyListener;
import com.orbbec.view.OpenGLView;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 奥比中光pro RGB+depth 注册页面
 * Created by v_liujialu01 on 2019/1/7.
 */

public class OrbbecProRegisterActivity extends BaseActivity implements View.OnClickListener,
        OpenNIHelper.DeviceOpenListener, ActivityCompat.OnRequestPermissionsResultCallback,
        ILivenessCallBack {
    private static final String TAG = "OrbbecProRegister";
    private static final int MSG_WHAT = 5;
    private static final String MSG_KEY = "YUV";
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

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private OpenGLView mDepthGLView;
    private Matrix matrix = new Matrix();

    private Context mContext;
    private boolean initOk = false;
    private Device device;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream depthStream;

    private int mWidth = com.orbbec.utils.GlobalDef.RESOLUTION_X;
    private int mHeight = com.orbbec.utils.GlobalDef.RESOLUTION_Y;

    private USBMonitor mUSBMonitor;
    private Camera mCamera;

    private HomeKeyListener mHomeListener;
    private MyHandler mHandler;

    private byte[] yuv = new byte[mWidth * mHeight * 3 / 2];

    private Object sync = new Object();
    private boolean exit = false;

    private String mNickName;
    private boolean mIsCameraPermission;
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
    protected void onResume() {
        super.onResume();
        Log.e("lkdong", "onResume:");
        mHandler = new MyHandler(this);
        // 注意此处的注册和反注册  注册后会有相机usb设备的回调
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        // 摄像头重启情况之前记录状态
        FaceSDKManager.getInstance().getFaceLiveness().clearInfo();
        finish();
    }

    @Override
    public void onDestroy() {
        Log.v("lkdong", "onDestroy:");
        super.onDestroy();
        FaceSDKManager.getInstance().getFaceLiveness().removeRegistCallBack(faceRegistCalllBack);
        // 重置状态为默认状态
        FaceSDKManager.getInstance().getFaceLiveness()
                .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_ONETON);
        unRegisterHomeListener();
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
        // mTextureView.setOpaque(false);
        // mTextureView.setKeepScreenOn(true);

        // 创建一个布局放入mTextureView
        RelativeLayout rl = new RelativeLayout(mContext);
        rl.setGravity(Gravity.CENTER);
        rl.addView(mTextureView, lp);
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
        mOpenNIHelper.requestDeviceOpen(this);
        matrix.postScale(-1, 1);   // 镜像水平翻转

        initUvc();
        registerHomeListener();
    }

    private void initUvc() {
        mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener); // 创建
        mTextureView.setRotationY(180); // 旋转90度
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture = surface;
//                Log.i("lkdong", "onSurfaceTextureAvailable:==" + mSurfaceTexture);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.i("lkdong", "onSurfaceTextureSizeChanged :  width==" + width + "height=" + height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.i("lkdong", "onSurfaceTextureDestroyed :  ");
//                if (mUVCCamera != null) {
//                    mUVCCamera.stopPreview();
//                }
                mSurfaceTexture = null;
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void registerHomeListener() {
        mHomeListener = new HomeKeyListener(this);
        mHomeListener.setOnHomePressedListener(new HomeKeyListener.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                finish();
            }

            @Override
            public void onHomeLongPressed() {

            }
        });
        mHomeListener.startWatch();
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

    private void unRegisterHomeListener() {
        if (mHomeListener != null) {
            mHomeListener.stopWatch();
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
                    if (mCamera != null) {
                        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                            @Override
                            public void onPreviewFrame(byte[] bytes, Camera camera) {
                                if (mHandler != null) {
                                    mHandler.removeMessages(MSG_WHAT);
                                    Message message = mHandler.obtainMessage();
                                    message.getData().putByteArray(MSG_KEY, bytes);
                                    message.what = MSG_WHAT;
                                    mHandler.sendMessage(message);
                                }
                            }
                        });
                    }
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
            int x = mode.getResolutionX();
            int y = mode.getResolutionY();
            int fps = mode.getFps();

            if (x == mWidth && y == mHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                depthStream.setVideoMode(mode);
                Log.v(TAG, " setmode");
            }

        }
        startThread();
    }

    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == depthNeedPermission) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission Grant");
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Permission Denied");
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
        // find device ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.device = Device.open();
                break;
            }
        }

        if (this.device == null) {
            ToastUtils.toast(mContext, " openni open devices failed: " + device.getDeviceName());
        }
    }

    private void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {
                List<VideoStream> streams = new ArrayList<>();

                streams.add(depthStream);
                depthStream.start();
                while (!exit) {
                    try {
                        OpenNI.waitForAnyStream(streams, 2000);
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {
                        mDepthGLView.update(depthStream, com.orbbec.utils.GlobalDef.TYPE_DEPTH);
                        ByteBuffer depthByteBuf = depthStream.readFrame().getData();
                        int depthLen = depthByteBuf.remaining();
                        byte[] depthByte = new byte[depthLen];
                        depthByteBuf.get(depthByte);
                        FaceSDKManager.getInstance().getFaceLiveness().setDepthData(depthByte);
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

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {

        @Override
        public void onAttach(final UsbDevice device) {
            if (device.getDeviceClass() == 239 && device.getDeviceSubclass() == 2) {
                mUSBMonitor.requestPermission(device);
            }
        }

        private void initCamera() {
            try {
                if (mCamera == null) {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    mCamera = Camera.open(0);
                }
                Camera.Parameters params = mCamera.getParameters();
                params.setPreviewSize(mWidth, mHeight);
                mCamera.setParameters(params);
                try {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                    mCamera.startPreview();
                } catch (IOException e) {
                    Log.e("chenjianping", e.getMessage());
                    e.printStackTrace();
                }
            } catch (RuntimeException e) {
                Log.e("chenjianping", e.getMessage());
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock,
                              final boolean createNew) {
            initCamera();
//            mUVCCamera = new UVCCamera();
//            Log.e("lkdong", "创建相机完成时间:" + System.currentTimeMillis());
//            mUVCCamera.open(ctrlBlock);
//            Log.i("lkdong", "supportedSize:" + camera.getSupportedSize());
//            if (mSurfaceTexture != null) {
//                mUVCCamera.setPreviewTexture(mSurfaceTexture);
//                previewSize = camera.getPreviewSize();
//                mUVCCamera.setPreviewSize(mWidth, mHeight);
//                mUVCCamera.setFrameCallback(iFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);
//                mUVCCamera.startPreview();
//            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Log.v("lkdong", "onDisconnect:");
            ctrlBlock.close();
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Log.v("lkdong", "onDettach:");
            ToastUtils.toast(mContext, "USB_DEVICE_DETACHED");
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    // 判断人脸识别是否已经完成了，如果没有完成，则不会进行下一次人脸识别。
    IFrameCallback iFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer byteBuffer) {
            final int len = byteBuffer.capacity();
            if (len > 0) {
                byteBuffer.get(yuv);
                if (mHandler != null) {
                    mHandler.removeMessages(MSG_WHAT);
                    Message message = mHandler.obtainMessage();
                    message.getData().putByteArray(MSG_KEY, yuv);
                    message.what = MSG_WHAT;
                    mHandler.sendMessage(message);
                }
            }
        }
    };

    private static class MyHandler extends Handler {
        private WeakReference<OrbbecProRegisterActivity> mWeakReference;
        private Bitmap mRgbBitmap = null;
        Bitmap bitmap = null;
        int[] rgba = null;

        public MyHandler(OrbbecProRegisterActivity pActivity) {
            mWeakReference = new WeakReference<>(pActivity);
            rgba = new int[mWeakReference.get().mWidth * mWeakReference.get().mHeight];
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT) {
                if (mWeakReference.get() != null) {
                    if (rgba == null) {
                        rgba = new int[mWeakReference.get().mWidth * mWeakReference.get().mHeight];
                    }
                    byte[] data = msg.getData().getByteArray(MSG_KEY);
                    if (data == null) {
                        return;
                    }
                    bitmap = mWeakReference.get().cameraByte2Bitmap(data, rgba,
                            mWeakReference.get().mWidth, mWeakReference.get().mHeight);
                    if (bitmap != null) {
                        mRgbBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                bitmap.getWidth(), bitmap.getHeight(), mWeakReference.get().matrix, true);
                        bitmap.recycle();
                        bitmap = null;

                        // 传入rgb人脸识别bitmap数据
                        FaceSDKManager.getInstance().getFaceLiveness().setRgbBitmap(mRgbBitmap);
                        FaceSDKManager.getInstance().getFaceLiveness().livenessCheck(mWeakReference.get().mWidth,
                                mWeakReference.get().mHeight, 0X0101);
                        mRgbBitmap.recycle();
                        mRgbBitmap = null;
                    }
                }
            }
        }
    }

    // 注册结果
    private IFaceRegistCalllBack faceRegistCalllBack = new IFaceRegistCalllBack() {

        @Override
        public void onRegistCallBack(int code, LivenessModel livenessModel, final Bitmap cropBitmap) {
            handler.removeCallbacks(runnable);
            // 停止摄像头采集
            registResultView.post(new Runnable() {
                @Override
                public void run() {
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

    public Bitmap cameraByte2Bitmap(byte[] data, int[] rgba, int width, int height) {
        DepthUtils.cameraByte2Bitmap(data, rgba, width, height);
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
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

    private void releaseCamera() {
        if (initOk) {
            exit = true;
            mHandler.removeMessages(MSG_WHAT);
            mHandler = null;

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
            if (device != null) {
                device.close();
            }

            if (mOpenNIHelper != null) {
                mOpenNIHelper.shutdown();
            }

            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
            initOk = false;
        }
    }
}
