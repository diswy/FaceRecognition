package com.baidu.idl.sample.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.adapter.CameraPagerAdapter;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.Utils;
import com.baidu.idl.sample.view.BaseCameraView;
import com.baidu.idl.sample.view.CircleImageView;
import com.baidu.idl.sample.view.CirclePercentView;
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

import static com.baidu.idl.sample.utils.Utils.TAG;

/**
 * Created by litonghui on 2018/11/17.
 */

public class OrbbecProPassActivity extends BaseActivity implements
        ILivenessCallBack, OpenNIHelper.DeviceOpenListener,
        ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {

    private static final int MSG_WHAT = 5;
    private static final String MSG_KEY = "YUV";
    private final int depthNeedPermission = 33;

    private Context mContext;

    private CircleImageView mImage;
    private TextView mNickNameTv;
    private TextView mSimilariryTv;
    private TextView mNumTv;
    private TextView mDetectTv;
    private TextView mFeatureTv;
    private TextView mLiveTv;
    private TextView mAllTv;

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private OpenGLView mDepthGLView;
    private Matrix matrix = new Matrix();

    private ViewPager mViewPager;
    private CameraPagerAdapter mPagerAdapter;
    private BaseCameraView mLayoutOne;
    private RelativeLayout mLayoutTwo;
    private ImageView mBtnGoLeft, mBtnGoRight;

    private CirclePercentView mRgbCircleView;
    private CirclePercentView mNirCircleView;
    private CirclePercentView mDepthCircleView;

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

    private Bitmap mBitmap;
    private String mUserName;
    private RelativeLayout mLayoutInfo;
    private ImageView mImageTrack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_orbbec);
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

    private void initData() {
        int num = FaceSDKManager.getInstance().setFeature();
        mNumTv.setText(String.format("底库人脸数: %s 个", num));

        FaceSDKManager.getInstance().getFaceLiveness().setLivenessCallBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("lkdong", "onStart:");
        mHandler = new MyHandler(this, mImageTrack);
        // 注意此处的注册和反注册  注册后会有相机usb设备的回调
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_WHAT);
        mHandler = null;
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
            mUSBMonitor.destroy();
        }
        exit = true;
        if (initOk) {
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
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.e("lkdong", "onPause:");
        }
        // 摄像头重启情况之前记录状态
        FaceSDKManager.getInstance().getFaceLiveness().clearInfo();
        finish();
    }

    @Override
    public void onDestroy() {
        Log.v("lkdong", "onDestroy:");
        super.onDestroy();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        unRegisterHomeListener();
    }

    private void registerHomeListener() {
        mHomeListener = new HomeKeyListener(this);
        mHomeListener
                .setOnHomePressedListener(new HomeKeyListener.OnHomePressedListener() {

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

    private void unRegisterHomeListener() {
        if (mHomeListener != null) {
            mHomeListener.stopWatch();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener
            = new USBMonitor.OnDeviceConnectListener() {
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
//               previewSize = camera.getPreviewSize();
//                mUVCCamera.setPreviewSize(mWidth, mHeight);
//                设置回调 和回调数据类型
//                mUVCCamera.setFrameCallback(iFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);
//                mUVCCamera.startPreview();
//            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            ctrlBlock.close();
//            if (mCamera != null) {
//                mUVCCamera.stopPreview();
//                mUVCCamera.close();
//            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Log.v("lkdong", "onDettach:");
            Toast.makeText(OrbbecProPassActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    //判断人脸识别是否已经完成了，如果没有完成，则不会进行下一次人脸识别。
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
                            Bitmap bitmap = Utils.getBitmapFromFile(imgPath);
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

    private static class MyHandler extends Handler {
        private WeakReference<OrbbecProPassActivity> mWeakReference;
        private Bitmap RgbBitmap = null;
        Bitmap bitmap = null;
        int[] rgba = null;
        private ImageView imageView;

        public MyHandler(OrbbecProPassActivity pActivity, ImageView imageView) {
            mWeakReference = new WeakReference<>(pActivity);
            rgba = new int[mWeakReference.get().mWidth * mWeakReference.get().mHeight];
            this.imageView = imageView;
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
                    if (data == null)
                        return;
                    bitmap = mWeakReference.get().cameraByte2Bitmap(data, rgba,
                            mWeakReference.get().mWidth, mWeakReference.get().mHeight);
                    if (bitmap != null) {
                        RgbBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                bitmap.getWidth(), bitmap.getHeight(), mWeakReference.get().matrix, true);
                        bitmap.recycle();
                        bitmap = null;

                        // 用于测试送检图片的显示，使用时建议注释掉
//                        if (imageView != null) {
//                            imageView.setImageBitmap(Utils.getBitmap(rgba, mWeakReference.get().mHeight, mWeakReference.get().mWidth));
//                        }
                        //传入rgb人脸识别bitmap数据
                        FaceSDKManager.getInstance().getFaceLiveness().setRgbBitmap(RgbBitmap);
                        FaceSDKManager.getInstance().getFaceLiveness().livenessCheck(mWeakReference.get().mWidth, mWeakReference.get().mHeight, 0X0101);
                        RgbBitmap.recycle();
                        RgbBitmap = null;
                    }
                }
            }
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


    private void init(UsbDevice device) {
        OpenNI.setLogAndroidOutput(false);
        OpenNI.setLogMinSeverity(0);
        OpenNI.initialize();

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            Toast.makeText(this, " openni enumerateDevices 0 devices", Toast.LENGTH_LONG).show();
            return;
        }

        this.device = null;
        //Find device ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.device = Device.open();
                break;
            }
        }

        if (this.device == null) {
            Toast.makeText(this, " openni open devices failed: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

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
}
