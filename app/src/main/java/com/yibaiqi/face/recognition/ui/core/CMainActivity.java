package com.yibaiqi.face.recognition.ui.core;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.ui.MainActivity;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.Utils;
import com.baidu.idl.sample.view.MonocularView;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.example.yfaceapi.GPIOManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.test.demo.PlaySurfaceView;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.Key;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.control.InitConfig;
import com.yibaiqi.face.recognition.control.MySyntherizer;
import com.yibaiqi.face.recognition.control.NonBlockSyntherizer;
import com.yibaiqi.face.recognition.di.DaggerActivityComponent;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.tools.EBQValue;
import com.yibaiqi.face.recognition.tools.FileUtil;
import com.yibaiqi.face.recognition.tools.GpioUtils;
import com.yibaiqi.face.recognition.tools.OfflineResource;
import com.yibaiqi.face.recognition.tools.TimeFormat;
import com.yibaiqi.face.recognition.tools.listener.UiMessageListener;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.FaceViewModel;
import com.yibaiqi.face.recognition.viewmodel.RongViewModel;
import com.yibaiqi.face.recognition.vo.LocalUser;
import com.yibaiqi.face.recognition.vo.MyRecord;
import com.yibaiqi.face.recognition.vo.SettingContent;
import com.yibaiqi.face.recognition.vo.TimeZone;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.rong.imlib.RongIMClient;

import static com.yibaiqi.face.recognition.tools.listener.MainHandlerConstant.INIT_SUCCESS;
import static com.yibaiqi.face.recognition.tools.listener.MainHandlerConstant.PRINT;

public class CMainActivity extends BaseActivity implements SurfaceHolder.Callback {

    @Inject
    ACache cache;

    private String tempKey;

    public static final int TASK = 999;
    public static final int UPLOAD = 888;
    public static final int REFRESH = 777;
    public static final int MONOCULAR_RESUME = 555;
    public static final int MONOCULAR_PAUSE = 444;
    public static final int VIDEO = 333;
    public static final int OPEN_DOOR = 222;
    public static final int CLOSE_DOOR = 111;

    private int delay = 300;
    private int delayFace = 5000;

    private FrameLayout mCameraView;
    private FrameLayout mHint;
    private MonocularView mMonocularView;
    private ImageView ivCapture;
    private ImageView ivDb;
    private ImageView btnSetting;
    private FaceViewModel faceModel;
    private boolean needPreview;

    private int delayBD = 500;

    private boolean isCameraSuccess = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TASK:
                    faceModel.updateTaskData();
                    break;
                case UPLOAD:
                    faceModel.updateUploadData();
                    break;
                case REFRESH:
                    Log.i("ebq", "融云::::收到后端消息，自认为过滤了因此请求网络，更新数据");
                    faceModel.update(CMainActivity.this);
                    break;
                case MONOCULAR_RESUME:
                    Log.w("ebq", "百度：resume");
                    mMonocularView.onResume();
                    break;
                case MONOCULAR_PAUSE:
                    if (mMonocularView != null) {
                        Log.w("ebq", "百度：有任务暂停");
                        mMonocularView.onBDPreviewPause();
                    }
                    break;
                case VIDEO:
                    if (needPreview) {
                        previewHik();
                    }
                    break;
                case CLOSE_DOOR:
                    closeDoor();
                    break;
            }
        }
    };

    @Override
    public int getLayoutRes() {
        return R.layout.activity_cmain;
    }

    @Override
    public void initView() {
        DaggerActivityComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .build()
                .inject(this);

        String bdDelay = cache.getAsString(Key.KEY_DELAY_BD);


        String sDelay = cache.getAsString(Key.KEY_DELAY);
        String sDelay2 = cache.getAsString(Key.KEY_DELAY_FACE);
        try {
            if (sDelay != null) {
                delay = Integer.parseInt(sDelay);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            if (sDelay2 != null) {
                delayFace = Integer.parseInt(sDelay2);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            if (bdDelay != null) {
                delayBD = Integer.parseInt(bdDelay);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // 新机器开闸方式
        String openType = cache.getAsString(Key.KEY_OPEN_TYPE);
        if (openType == null || openType.equals("type_1")) {
            openTypeMode = 1;
        } else if (openType.equals("type_2")) {
            openTypeMode = 2;
        }

        // 固定设备，闸机头默认需要设置0度
        mHint = findViewById(R.id.ff_hint);
        mCameraView = findViewById(R.id.layout_camera);
        ivCapture = findViewById(R.id.iv_capture);
        ivDb = findViewById(R.id.iv_db);
        btnSetting = findViewById(R.id.btn_setting);
        // 海康威视
        m_osurfaceView = findViewById(R.id.Sur_Player);
    }

    @Override
    public void initialize() {
        faceModel = ViewModelProviders.of(this, App.getInstance().factory).get(FaceViewModel.class);

        faceModel.initHandler(mHandler);

        // 初始化语音合成服务
        initialTts();

        // 初始化OSS服务
        faceModel.initOSS();

        // 初始化下载服务
        faceModel.initDownload(this);

        // 初始化人脸识别
        calculateCameraView();

        // 初始化海康威视SDK
        HCNetSDK.getInstance().NET_DVR_Init();
        // 海康威视监视，1px*1px 为了实现截图
        m_osurfaceView.getHolder().addCallback(this);


        // 处理人脸库收到的通知
        faceModel.observerData(this);
        // 处理记录
        faceModel.observerRecordData(this, this);

        faceModel.getIsRegister().observe(this, aBoolean -> {
            if (aBoolean == null)
                return;

            if (aBoolean) {
                mHint.setVisibility(View.VISIBLE);
            } else {
                mHint.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.test_btn_speak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CMainActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.test_btn_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mMonocularView.onBDPreviewPause();
//                Log.w("ebq-bd", "的确执行了释放");

//                mMonocularView.onPause();

//                FaceSDKManager.getInstance().getFaceLiveness().release();
//                FaceSDKManager.getInstance().getFaceLiveness()

//                GpioUtils.writeGpioValue(146, "0");
//                tempKey = String.valueOf(System.currentTimeMillis());
//                List<DbOption> list = new ArrayList<>();
//
//                for (int i = 0; i < 3; i++) {
//
//                    DbOption mData = new DbOption(
//                            tempKey + i,
//                            "12345678",
//                            "张三",
////                        "https://yizhixiao.oss-cn-hangzhou.aliyuncs.com/2019-05-09%2022%3A25%3A04fff",
//                            "https://yizhixiao.oss-cn-hangzhou.aliyuncs.com/Face/400.jpg",
//                            0);// 新增用户
//                    list.add(mData);
//                }
//                faceModel.insert(list);
//                Log.i("ebq", "数据更新:来源人工测试新增");
            }
        });

        findViewById(R.id.test_btn_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonocularView.onBDPreviewResume();

//                GpioUtils.writeGpioValue(146, "1");
//                List<DbOption> list = new ArrayList<>();
//                DbOption mData = new DbOption(
//                        tempKey,
//                        "12345678",
//                        "张三",
//                        "",
//                        1);
//                list.add(mData);
//                faceModel.insert(list);
//                Log.i("ebq", "数据更新:来源人工测试删除");
            }
        });
        int index = 146;
        findViewById(R.id.test_btn_1).setOnClickListener(v -> {
            GpioUtils.upgradeRootPermissionForExport();
            if (GpioUtils.exportGpio(146)) {
                Log.e("ebq", "获取IO：成功");
                GpioUtils.upgradeRootPermissionForGpio(146);
                String status = GpioUtils.getGpioDirection(146);
                if ("".equals(status))
                    Toast.makeText(this, "无效的GPIO", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "有效的GPIO", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("ebq", "获取IO：失败");
            }
        });
        findViewById(R.id.test_btn_2).setOnClickListener(v -> {
            Toast.makeText(this, "当前io的类型 = " + GpioUtils.getGpioDirection(index), Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.test_btn_3).setOnClickListener(v -> {
            if (GpioUtils.setGpioDirection(index, 1))
                Toast.makeText(this, "成功设置该io为输入口", Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.test_btn_4).setOnClickListener(v -> {
            if (GpioUtils.setGpioDirection(index, 0))
                Toast.makeText(this, "成功设置该io为输出口", Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.test_btn_5).setOnClickListener(v -> {
            Toast.makeText(this, "当前io口的电平 = " + GpioUtils.getGpioValue(index), Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.test_btn_6).setOnClickListener(v -> {
            if (GpioUtils.writeGpioValue(index, "1"))
                Toast.makeText(this, "成功设置该io高电平", Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.test_btn_7).setOnClickListener(v -> {
            if (GpioUtils.writeGpioValue(index, "0"))
                Toast.makeText(this, "成功设置该io低电平", Toast.LENGTH_LONG).show();
        });

        //-------------------融云
        RongViewModel serverViewModel = ViewModelProviders.of(this)
                .get(RongViewModel.class);
        // 根据token 连接融云
        serverViewModel.connect(faceModel.getRongToken());
        serverViewModel.getConnectStatus().observe(this, isConnect -> {
            Log.e("ebq", "融云连接状态：" + isConnect);
        });

        RongIMClient.setOnReceiveMessageListener((message, left) -> {
            Log.i("ebq", "融云:收到消息，剩余未拉取消息数量=" + left + "条");
            mHandler.removeMessages(REFRESH);
            mHandler.sendEmptyMessageDelayed(REFRESH, 3000);
            return false;
        });
    }

    @Override
    protected void initListener() {
        btnSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMonocularView.onResume();

        if (faceModel.isCameraEnable() && !isCameraSuccess) {
            needPreview = true;
            loginHik();

            mHandler.removeMessages(VIDEO);
            mHandler.sendEmptyMessageDelayed(VIDEO, 5000);
        }

    }

    @Override
    protected void onStop() {
        if (mMonocularView != null) {
            mMonocularView.onBDPreviewPause();
        }
        super.onStop();
    }


    /**
     * 计算并适配显示图像容器的宽高
     */
    private void calculateCameraView() {
        // 重置状态为默认状态
//        FaceSDKManager.getInstance().getFaceLiveness()
//                .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_ONETON);

        String newPix;
        newPix = DensityUtil.calculateCameraView(mContext);
        String[] newPixs = newPix.split(" ");
        int newWidth = Integer.parseInt(newPixs[0]);
        int newHeight = Integer.parseInt(newPixs[1]);
        Log.w("ebq", "采集摄像头：宽：" + newWidth);
        Log.w("ebq", "采集摄像头：高：" + newHeight);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(newWidth, newHeight);
        mMonocularView = new MonocularView(mContext);
        mMonocularView.setLivenessCallBack(mILivenessCallBack);
        mCameraView.removeAllViews();

        mCameraView.addView(mMonocularView, layoutParams);

    }

    private ILivenessCallBack mILivenessCallBack = new ILivenessCallBack() {
        @Override
        public void onTip(int code, String msg) {

        }

        @Override
        public void onCanvasRectCallback(LivenessModel livenessModel) {

        }

        @Override
        public void onCallback(int code, LivenessModel livenessModel) {
            System.out.println("-code= " + code);
            runOnUiThread(() -> {
                if (code == 0) {
                    Feature feature = livenessModel.getFeature();
//                    mSimilariryTv.setText(String.format("相似度: %s", livenessModel.getFeatureScore()));
//                    mNickNameTv.setText(String.format("%s，你好!", feature.getUserName()));

                    String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                            + "/" + feature.getCropImageName();
                    Bitmap bitmap = Utils.getBitmapFromFile(imgPath);
                    ivDb.setImageBitmap(bitmap);
                    System.out.println("----姓名:" + feature.getUserName());
                    System.out.println("-add:" + imgPath);

                    // 显示送检图片
                    Bitmap myBitmap = mMonocularView.getMyBitmap();
                    ivCapture.setImageBitmap(myBitmap);

                    if (canSavePic()) {
                        queryUser(feature.getUserId(), myBitmap);
                    }
                } else {
                    ivCapture.setImageBitmap(null);
                    ivDb.setImageBitmap(null);
                }

            });
        }
    };


    //------------------海康摄像头
    private SurfaceView m_osurfaceView = null;
    private int m_iLogID = -1; // return by NET_DVR_Login_v30
    private int m_iPlayID = -1; // return by NET_DVR_RealPlay_V40
    private int m_iPlaybackID = -1; // return by NET_DVR_PlayBackByTime
    private static PlaySurfaceView[] playView = new PlaySurfaceView[4];
    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
    private int m_iStartChan = 0; // start channel number
    private int m_iChanNum = 0; // channel number
    private boolean m_bMultiPlay = false;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        m_osurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        //valid just when single channel preview
        if (-1 == m_iPlayID && -1 == m_iPlaybackID) {
            return;
        }
        playView[0].m_hHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void loginHik() {
        try {
            if (m_iLogID < 0) {
                m_iLogID = loginDevice();
                if (m_iLogID < 0) {
                    return;
                }
                ExceptionCallBack oexceptionCbf = getExceptiongCbf();
                if (oexceptionCbf == null) {
                    return;
                }

                if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(oexceptionCbf)) {
                    return;
                }
            } else {
                if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
                    return;
                }
                m_iLogID = -1;
            }

        } catch (Exception err) {

        }
    }

    private int loginDevice() {
        int iLogID = -1;
        iLogID = loginNormalDevice();
        return iLogID;
    }

    private int loginNormalDevice() {
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        String strIP = faceModel.getCameraIp();
        int nPort = faceModel.getCameraPort();
        String strUser = faceModel.getCameraAccount();
        String strPsd = faceModel.getCameraPwd();
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort, strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            isCameraSuccess = false;
            Toast.makeText(this, "监控摄像头登录失败，请检查您的设置后重新尝试", Toast.LENGTH_SHORT).show();
            return -1;
        }
        Toast.makeText(this, "监控摄像头初始化成功", Toast.LENGTH_SHORT).show();
        isCameraSuccess = true;
        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;

        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byIPChanNum + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256;
        }

        if (m_iChanNum > 1) {
            ChangeSingleSurFace(false);
        } else {
            ChangeSingleSurFace(true);
        }
        return iLogID;
    }

    private void ChangeSingleSurFace(boolean bSingle) {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        for (int i = 0; i < 4; i++) {
            if (playView[i] == null) {
                playView[i] = new PlaySurfaceView(this);
                playView[i].setParam(metric.widthPixels);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = playView[i].getM_iHeight() - (i / 2)
                        * playView[i].getM_iHeight();
                params.leftMargin = (i % 2) * playView[i].getM_iWidth();
                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                addContentView(playView[i], params);
                playView[i].setVisibility(View.INVISIBLE);
            }
        }

        if (bSingle) {
            for (int i = 0; i < 4; ++i) {
                playView[i].setVisibility(View.INVISIBLE);
            }
            playView[0].setParam(metric.widthPixels * 2);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = playView[3].getM_iHeight() - (3 / 2)
                    * playView[3].getM_iHeight();
//            params.bottomMargin = 0;
            params.leftMargin = 0;
            // params.
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            playView[0].setLayoutParams(params);
            playView[0].setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < 4; ++i) {
                playView[i].setVisibility(View.VISIBLE);
            }

            playView[0].setParam(metric.widthPixels);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = playView[0].getM_iHeight() - (0 / 2)
                    * playView[0].getM_iHeight();
            params.leftMargin = (0 % 2) * playView[0].getM_iWidth();
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            playView[0].setLayoutParams(params);
        }
    }

    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                System.out.println("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }

    private void previewHik() {
        try {
            if (m_iLogID < 0) {
                return;
            }
            if (m_iPlaybackID >= 0) {
                return;
            }
            if (m_iChanNum > 1) {
                if (!m_bMultiPlay) {
                    startMultiPreview();
                    m_bMultiPlay = true;
                } else {
                    stopMultiPreview();
                    m_bMultiPlay = false;
                }
            } else {// preivew a channel
                Log.v("ebq", "摄像头：频道数量1");
                if (m_iPlayID < 0) {
                    startSinglePreview();
                    Log.v("ebq", "摄像头：开始预览");
                } else {
                    Log.v("ebq", "摄像头：停止预览");
                    stopSinglePreview();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startMultiPreview() {
        for (int i = 0; i < 4; i++) {
            playView[i].startPreview(m_iLogID, m_iStartChan + i);
        }
    }

    private void stopMultiPreview() {
        int i = 0;
        for (i = 0; i < 4; i++) {
            playView[i].stopPreview();
        }
        m_iPlayID = -1;
    }

    private void startSinglePreview() {
        if (m_iPlaybackID >= 0) {
            return;
        }

        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = m_iStartChan;
        previewInfo.dwStreamType = 0; // main stream
        previewInfo.bBlocked = 1;
        previewInfo.hHwnd = playView[0].m_hHolder;

        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID, previewInfo, null);
    }

    private void stopSinglePreview() {
        if (m_iPlayID < 0) {
            return;
        }
        // net sdk stop preview
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            return;
        }
        m_iPlayID = -1;
    }


    private long tempTime = 0L;

    private boolean canSavePic() {
        if (tempTime == 0L) {
            tempTime = System.currentTimeMillis();
            return true;
        }

        if ((System.currentTimeMillis() - tempTime) > delayFace) {
            tempTime = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取摄像头截图,保存送检图片
     */
    private void captureVideo(String userKey, Bitmap bitmap, int errorType, boolean isOpen) {
        boolean hikvisonCapture = false;
        boolean faceCapture = false;
        String formatTime = TimeFormat.FULL(System.currentTimeMillis());
        String realFileName = formatTime + userKey;// 时间+学生KEY
        realFileName = realFileName.replace(":", "-");
        File mFile = new File(EBQValue.HIK_PATH);
        if (!mFile.exists()) {
            mFile.mkdirs();
        }
        if (HCNetSDKJNAInstance.getInstance().NET_DVR_CapturePictureBlock(m_iPlayID, EBQValue.HIK_PATH + realFileName + "_hik.jpg", 0)) {
            Log.w("ebq", "记录：海康威视截图成功。图片地址：" + EBQValue.HIK_PATH + realFileName + "_hik.jpg");
            hikvisonCapture = true;
        } else {
            Log.w("ebq", "记录：海康威视截图失败");
        }

        File f = FileUtil.saveBitmap(bitmap, realFileName + "_face");
        if (f != null) {
            Log.w("ebq", "记录：人脸识别送检图片地址：" + EBQValue.CAPTURE_PATH + realFileName + "_face.jpg");
            Log.w("ebq", "记录：人脸识别送检图片地址：文件读取地址：" + f.getAbsolutePath());
            faceCapture = true;
        } else {
            Log.w("ebq", "记录：送检图片保存失败");
        }

        MyRecord record = new MyRecord(formatTime, userKey, realFileName,
                hikvisonCapture, faceCapture, errorType, isOpen);
        faceModel.insert(record);
    }


    //--------------开关门

    private int openTypeMode = 1;

    private void openDoor() {
        if (openTypeMode == 1) {// 老模式
            if (GpioUtils.setGpioDirection(146, 0)) {
                Log.e("ebq", "设置IO为输出口成功");
                mHandler.sendEmptyMessageDelayed(CLOSE_DOOR, delay);
            }
        } else {// 新模式
            GPIOManager.getInstance(this).pullUpRelay();
            mHandler.sendEmptyMessageDelayed(CLOSE_DOOR, delay);
        }
    }

    private void closeDoor() {
        if (openTypeMode == 1) {// 老模式
            if (GpioUtils.setGpioDirection(146, 1)) {
                Log.e("ebq", "设置IO为输入口成功");
            }
        } else {
            GPIOManager.getInstance(this).pullDownRelay();
        }
    }

    //-------------语音合成
    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;
    private Boolean isVoiceInit;
    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handle(msg);
        }
    };

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;
    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    private void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(ttsMode, params, listener);
        synthesizer = new NonBlockSyntherizer(this, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
        }
        return offlineResource;
    }

    protected void handle(Message msg) {
        switch (msg.what) {
            case INIT_SUCCESS:
                isVoiceInit = true;
                msg.what = PRINT;
                break;
            default:
                break;
        }
    }

    private void say(String s) {
        Disposable d = Flowable.just(s)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (synthesizer != null && isVoiceInit && s != null) {
                            synthesizer.speak(s);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synthesizer.release();
    }


    private String getCurrentTime() {
        Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料
        t.setToNow(); // 取得系统时间。
        int hour = t.hour;    // 0-23
        if (hour >= 0 && hour <= 9)
            return "早上好";

        if (hour >= 9 && hour <= 12)
            return "上午好";

        if (hour >= 12 && hour <= 13)
            return "中午好";

        if (hour >= 13 && hour <= 18)
            return "下午好";

        if (hour >= 18)
            return "晚上好";

        return "您好";
    }

    //---工具
    private boolean inTimeZone(String start2, String end2) {


        String today = TimeFormat.YMD(System.currentTimeMillis());

        long mStart = TimeFormat.formatTime2(today + start2);
        long mEnd = TimeFormat.formatTime2(today + end2);
        long mCurrent = System.currentTimeMillis();


        Log.wtf("用户判断", "开始时间：" + today + start2);
        Log.wtf("用户判断", "结束时间：" + today + end2);

        Log.wtf("用户判断", "Bool：" + (mCurrent > mStart && mCurrent < mEnd));
        return mCurrent > mStart && mCurrent < mEnd;
    }

    private void queryUser(String id, final Bitmap bitmap) {
        Disposable d = Flowable.just(id)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(s -> {
                    LocalUser user = faceModel.getUserByKey(s);
                    Log.i("用户判断", "APP_TYPE=" + user.getApp_type());
                    if (user.getApp_type() == 3) {// 老师 只有固定时间判断
                        Log.i("用户判断", "是老师");
                        gudingshijian(user.getApp_type(), user, id, bitmap, false);
                    } else {// 学生
                        Log.i("用户判断", "是学生");
                        isIntrude(user, id, bitmap);
                    }

//
//                    Log.i("用户判断", "1");
//                    if (user.getApp_type() == 3) {//
//                        Log.i("用户判断", "2");
//                        if (settingFlag.equals("1")) {// 允许
//                            Log.i("用户判断", "3");
//                            List<SettingContent> list = faceModel.getDao().getSettingContent();
//                            boolean flag = false;// false非法 true正常
//                            for (int i = 0; i < list.size(); i++) {
//                                if (list.get(i).getApp_types().contains("3")) {// 包含
//                                    String timeDetails = list.get(i).getTime_detail();
//                                    List<TimeZone> timeZoneList = new Gson().fromJson(timeDetails, new TypeToken<List<TimeZone>>() {
//                                    }.getType());
//                                    boolean innerFlag = false;// 默认不开门
//                                    for (int j = 0; j < timeZoneList.size(); j++) {
//
//                                        if (inTimeZone(list.get(i).getStart_time(), timeZoneList.get(j).getStart_time(), timeZoneList.get(j).getEnd_time())) {
//                                            // 需要开门
//                                            innerFlag = true;
//                                            break;
//                                        }
//                                    }
//
//                                    flag = innerFlag;
//                                    break;
//                                }
//                            }
//
//                            if (flag) {
//                                Log.i("用户判断", "4");
//                                openDoor();
//                                say(user.getReal_name() + "" + getCurrentTime());
//                                captureVideo(id, bitmap);
//                            } else {
//                                Log.i("用户判断", "5");
//                                say(user.getReal_name() + ",当前禁止通行");
//                                faceModel.uploadError(user.getUser_key());
//                            }
//
//                        } else if (settingFlag.equals("2")) {// 不允许
//                            Log.i("用户判断", "6");
//                            say(user.getReal_name() + ",当前禁止通行");
//                            faceModel.uploadError(user.getUser_key());
//                        }
//                    } else {// 不为
//                        Log.i("用户判断", "7");
//                        if (user.isIs_intrude()) {// 入侵者
//                            Log.i("用户判断", "8");
//                            say(user.getReal_name() + ",非法闯入");
//                            faceModel.uploadError(user.getUser_key());
//                        } else {// 不是非法闯入
//                            Log.i("用户判断", "9");
//                            String current = TimeFormat.FULL2(System.currentTimeMillis());
//                            int count = faceModel.getDao().isLeaves(user.getUser_key(), current);
//                            if (count > 0 && user.isIs_traffic_error()) {// 不开门
//                                Log.i("用户判断", "10");
//
//                                if (user.isIs_class_course()) {// 判断课表
//                                    Log.i("用户判断", "11");
//                                    int classCount = faceModel.getDao().isCourse(user.getUser_key(), current);
//                                    if (classCount > 0) {
//                                        Log.i("用户判断", "12");
//                                        say(user.getReal_name() + ",当前禁止通行");
//                                        faceModel.uploadError(user.getUser_key());
//                                    } else {
//                                        Log.i("用户判断", "13");
//                                        if (settingFlag.equals("1")) {// 允许
//                                            Log.i("用户判断", "14");
//                                            List<SettingContent> list = faceModel.getDao().getSettingContent();
//                                            boolean flag = false;// false非法 true正常
//                                            for (int i = 0; i < list.size(); i++) {
//                                                if (list.get(i).getApp_types().contains("1")) {// 包含学生
//                                                    String timeDetails = list.get(i).getTime_detail();
//                                                    List<TimeZone> timeZoneList = new Gson().fromJson(timeDetails, new TypeToken<List<TimeZone>>() {
//                                                    }.getType());
//                                                    boolean innerFlag = false;// 默认不开门
//                                                    for (int j = 0; j < timeZoneList.size(); j++) {
//
//                                                        if (inTimeZone(list.get(i).getStart_time(), timeZoneList.get(j).getStart_time(), timeZoneList.get(j).getEnd_time())) {
//                                                            // 需要开门
//                                                            innerFlag = true;
//                                                            break;
//                                                        }
//                                                    }
//
//                                                    flag = innerFlag;
//                                                    break;
//                                                }
//                                            }
//
//                                            if (flag) {
//                                                Log.i("用户判断", "15");
//                                                openDoor();
//                                                say(user.getReal_name() + getCurrentTime());
//                                                captureVideo(id, bitmap);
//                                            } else {
//                                                Log.i("用户判断", "16");
//                                                say(user.getReal_name() + ",当前禁止通行");
//                                                faceModel.uploadError(user.getUser_key());
//                                            }
//
//                                        } else if (settingFlag.equals("2")) {// 不允许
//                                            Log.i("用户判断", "17");
//                                            say(user.getReal_name() + ",当前禁止通行");
//                                            faceModel.uploadError(user.getUser_key());
//                                        }
//                                    }
//                                } else {// 走固定时间判断
//                                    Log.i("用户判断", "18");
//                                    if (settingFlag.equals("1")) {// 允许
//                                        Log.i("用户判断", "19");
//                                        List<SettingContent> list = faceModel.getDao().getSettingContent();
//                                        boolean flag = false;// false非法 true正常
//                                        for (int i = 0; i < list.size(); i++) {
//                                            if (list.get(i).getApp_types().contains("1")) {// 包含学生
//                                                String timeDetails = list.get(i).getTime_detail();
//                                                List<TimeZone> timeZoneList = new Gson().fromJson(timeDetails, new TypeToken<List<TimeZone>>() {
//                                                }.getType());
//                                                boolean innerFlag = false;// 默认不开门
//                                                for (int j = 0; j < timeZoneList.size(); j++) {
//
//                                                    if (inTimeZone(list.get(i).getStart_time(), timeZoneList.get(j).getStart_time(), timeZoneList.get(j).getEnd_time())) {
//                                                        // 需要开门
//                                                        innerFlag = true;
//                                                        break;
//                                                    }
//                                                }
//
//                                                flag = innerFlag;
//                                                break;
//                                            }
//                                        }
//
//                                        if (flag) {
//                                            Log.i("用户判断", "20");
//                                            openDoor();
//                                            say(user.getReal_name() + getCurrentTime());
//                                            captureVideo(id, bitmap);
//                                        } else {
//                                            Log.i("用户判断", "21");
//                                            say(user.getReal_name() + ",当前禁止通行");
//                                            faceModel.uploadError(user.getUser_key());
//                                        }
//
//                                    } else if (settingFlag.equals("2")) {// 不允许
//                                        Log.i("用户判断", "22");
//                                        say(user.getReal_name() + ",当前禁止通行");
//                                        faceModel.uploadError(user.getUser_key());
//                                    }
//                                }
//
//
//                            } else if (count > 0 && !user.isIs_traffic_error()) {
//                                Log.i("用户判断", "23");
//                                openDoor();
//                                say(user.getReal_name() + getCurrentTime());
//                                captureVideo(id, bitmap);
//                            }
//                            if (count == 0) {
//                                Log.i("用户判断", "是否需要判断课表");
//                                if (user.isIs_class_course()) {
//
//                                } else {
//                                }
//                            }
//                        }
//
//                    }
                }, throwable -> {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CMainActivity.this, "发生错误：" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                });
    }

    /**
     * 是否非法
     */
    private void isIntrude(LocalUser user, String id, final Bitmap bitmap) {
        if (user.isIs_intrude()) {// 非法
            Log.i("用户判断", "非法闯入");
            say(user.getReal_name() + "非法闯入");
            captureVideo(id, bitmap, 2, false);
        } else {// 正常->走请假判断
            isLeaves(user, id, bitmap);
        }
    }

    /**
     * 是否请假
     */
    private void isLeaves(LocalUser user, String id, final Bitmap bitmap) {
        Log.i("用户判断", "进入请假");
        String current = TimeFormat.FULL2(System.currentTimeMillis());
        int count = faceModel.getDao().isLeaves(user.getUser_key(), current);
        Log.i("用户判断", "请假数量：" + count);
        if (count > 0) {// 有请假，判断异常
            Log.i("用户判断", "有请假");
            isTraficError(user, id, bitmap);
        } else {// 无请假，执行课表
            exeClassCourse(user, id, bitmap);
        }
    }

    /**
     * 课表
     */
    private void exeClassCourse(LocalUser user, String id, final Bitmap bitmap) {
        if (user.isIs_class_course()) {// 需要
            String current = TimeFormat.FULL2(System.currentTimeMillis());
            int count = faceModel.getDao().isCourse(user.getUser_key(), current);
            if (count > 0) {// 上课时间禁止
                say(user.getReal_name() + "上课时间禁止通行");
                captureVideo(id, bitmap, 0, false);
            } else {
                gudingshijian(user.getApp_type(), user, id, bitmap, true);
            }
        } else {// 不需要
            gudingshijian(user.getApp_type(), user, id, bitmap, true);
        }
    }

    /**
     * 异常
     */
    private void isTraficError(LocalUser user, String id, final Bitmap bitmap) {
        if (user.isIs_traffic_error()) {// 异常
            Log.i("用户判断", "出现异常");

            faceModel.uploadError(user.getUser_key());// 上传异常
            String settingFlag = cache.getAsString("config_error_flag");
            if (settingFlag.equals("1")) {// 不开门
                Log.i("用户判断", "出现异常-没有开门");
                captureVideo(id, bitmap, 1, false);
            } else if (settingFlag.equals("2")) {// 开门
                Log.i("用户判断", "出现异常-开门");
                openDoor();
                captureVideo(id, bitmap, 1, true);
            }
            say(user.getReal_name() + "当前通行异常");

        } else {// 正常
            openDoor();
            say(user.getReal_name() + getCurrentTime());
            captureVideo(id, bitmap, 0, true);
        }
    }


    /**
     * 固定时间判断
     */
    private void gudingshijian(int type, LocalUser user, String id, final Bitmap bitmap, boolean isError) {
        Log.i("用户判断", "固定时间判断");

        String settingFlag = cache.getAsString("config_setting_traffic_flag");

        String search = "%" + type + "%";
        long current = System.currentTimeMillis();
        String data = TimeFormat.FULL2(current);
        List<SettingContent> list = faceModel.getDao().getSettingContent(data, search);
        Log.e("用户判断", "查找出来的结果：" + new Gson().toJson(list));
        boolean flag = false;// 是否存在在这个值
        for (int i = 0; i < list.size(); i++) {
            String timeDetails = list.get(i).getTime_detail();
            List<TimeZone> timeZoneList = new Gson().fromJson(timeDetails, new TypeToken<List<TimeZone>>() {
            }.getType());
            boolean innerFlag = false;// 默认不开门
            for (int j = 0; j < timeZoneList.size(); j++) {
                if (inTimeZone(timeZoneList.get(j).getStart_time(), timeZoneList.get(j).getEnd_time())) {
                    // 需要开门
                    innerFlag = true;
                    break;
                }
            }
            flag = innerFlag;
            break;
        }

        if (flag) {
            Log.i("用户判断", "当前在配置时间段");

            if (settingFlag.equals("1")) {// 默认不开门

                if (isError) {
                    isTraficError(user, id, bitmap);
                } else {
                    Log.i("用户判断", "当前在配置时间段，默认不开门，开门");
                    openDoor();
                    say(user.getReal_name() + getCurrentTime());
                    captureVideo(id, bitmap, 0, true);
                }

            } else {
                Log.i("用户判断", "当前在配置时间段，默认开门，不开门");
                say(user.getReal_name() + "当前禁止通行");
                captureVideo(id, bitmap, 0, false);
            }
        } else {
            Log.i("用户判断", "当前不在配置时间段");

            if (settingFlag.equals("1")) {// 默认不开门
                Log.i("用户判断", "当前在配置时间段，默认不开门，不开门");

                say(user.getReal_name() + "当前禁止通行");
                captureVideo(id, bitmap, 0, false);
            } else {
                Log.i("用户判断", "当前在配置时间段，默认开门，开门");

                if (isError) {
                    isTraficError(user, id, bitmap);
                } else {
                    openDoor();
                    say(user.getReal_name() + getCurrentTime());
                    captureVideo(id, bitmap, 0, true);
                }

            }
        }
    }
}
