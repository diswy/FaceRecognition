package com.yibaiqi.face.recognition.ui.core;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.Utils;
import com.baidu.idl.sample.view.MonocularView;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.seeku.android.Manager;
import com.test.demo.PlaySurfaceView;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.control.InitConfig;
import com.yibaiqi.face.recognition.control.MySyntherizer;
import com.yibaiqi.face.recognition.control.NonBlockSyntherizer;
import com.yibaiqi.face.recognition.tools.EBQValue;
import com.yibaiqi.face.recognition.tools.FileUtil;
import com.yibaiqi.face.recognition.tools.OfflineResource;
import com.yibaiqi.face.recognition.tools.TimeFormat;
import com.yibaiqi.face.recognition.tools.listener.UiMessageListener;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.FaceViewModel;
import com.yibaiqi.face.recognition.viewmodel.RongViewModel;
import com.yibaiqi.face.recognition.vo.MyRecord;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rong.imlib.RongIMClient;

import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ZERO_ANGLE;
import static com.yibaiqi.face.recognition.tools.listener.MainHandlerConstant.INIT_SUCCESS;
import static com.yibaiqi.face.recognition.tools.listener.MainHandlerConstant.PRINT;
import static com.yibaiqi.face.recognition.tools.listener.MainHandlerConstant.UI_CHANGE_INPUT_TEXT_SELECTION;
import static com.yibaiqi.face.recognition.tools.listener.MainHandlerConstant.UI_CHANGE_SYNTHES_TEXT_SELECTION;

public class CMainActivity extends BaseActivity implements ILivenessCallBack, SurfaceHolder.Callback {
    public static final int TASK = 999;
    public static final int UPLOAD = 888;
    public static final int REFRESH = 777;

    private FrameLayout mCameraView;
    private MonocularView mMonocularView;
    private ImageView ivCapture;
    private ImageView ivDb;
    private ImageView btnSetting;
    private FaceViewModel faceModel;

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
                    faceModel.update(CMainActivity.this);
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
        // 固定设备，闸机头默认需要设置0度
        PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_PREVIEW_ZERO_ANGLE);

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

        faceModel.observerRecordData(this, this);




        findViewById(R.id.test_btn_speak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDoor();
//                say("晋宝宝呀，是个大坏蛋哟，哈哈哈。你说是不是");
//                faceModel.syncRecord(CMainActivity.this, null);
            }
        });

        findViewById(R.id.test_btn_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDoor();
//                List<DbOption> list = new ArrayList<>();
//                faceModel.insert(list);
            }
        });

        findViewById(R.id.test_btn_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //-------------------融云
        RongViewModel serverViewModel = ViewModelProviders.of(this)
                .get(RongViewModel.class);
        serverViewModel.getConnectStatus().observe(this, isConnect -> {
            System.out.println(">>>>>>融云连接：状态" + isConnect);
        });

        serverViewModel.connect(faceModel.getRongToken());

        RongIMClient.setOnReceiveMessageListener((message, left) -> {
            System.out.println(">>>>>>融云：多次执行的那个：消息=" + message);
            System.out.println(">>>>>>融云：多次执行的那个：未拉取=" + left);
            mHandler.sendEmptyMessage(REFRESH);
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
        if (faceModel.isCameraEnable() && !isCameraSuccess) {
            loginHik();
            previewHik();
        }

        mMonocularView.onResume();
    }

    @Override
    protected void onStop() {
        mMonocularView.onPause();
        super.onStop();
    }


    /**
     * 计算并适配显示图像容器的宽高
     */
    private void calculateCameraView() {
        String newPix;
        newPix = DensityUtil.calculateCameraView(mContext);
        String[] newPixs = newPix.split(" ");
        int newWidth = Integer.parseInt(newPixs[0]);
        int newHeight = Integer.parseInt(newPixs[1]);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(newWidth, newHeight);
        mMonocularView = new MonocularView(mContext);
        mMonocularView.setLivenessCallBack(this);
        mCameraView.removeAllViews();
        mCameraView.addView(mMonocularView, layoutParams);
    }

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
                    say(feature.getUserName() + "验证成功");
                    openDoor();
                    captureVideo(feature.getUserId(), myBitmap);
                }
            } else {
                ivCapture.setImageBitmap(null);
                ivDb.setImageBitmap(null);
            }

        });
    }


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
                if (m_iPlayID < 0) {
                    startSinglePreview();
                } else {
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


    private String tempName = "";
    private long tempTime = 0L;

    private boolean canSavePic() {
        if (tempTime == 0L) {
            tempTime = System.currentTimeMillis();
            return true;
        }

        if ((System.currentTimeMillis() - tempTime) > 5000) {
            tempTime = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取摄像头截图,保存送检图片
     */
    private void captureVideo(String userKey, Bitmap bitmap) {
        boolean hikvisonCapture = false;
        boolean faceCapture = false;
        String formatTime = TimeFormat.FULL(System.currentTimeMillis());
        String realFileName = formatTime + userKey;// 时间+学生KEY
        if (HCNetSDKJNAInstance.getInstance().NET_DVR_CapturePictureBlock(m_iPlayID, EBQValue.HIK_PATH + realFileName + "_hik.jpg", 0)) {
            System.out.println("--->>>截图成功");
            hikvisonCapture = true;
        } else {
            System.out.println("--->>>截图失败");
        }

        File f = FileUtil.saveBitmap(bitmap, realFileName + "_face");
        if (f != null) {
            faceCapture = true;
            System.out.println("--->>>保存成功" + realFileName);
//            faceModel.asyncPutImage(realFileName, f.getAbsolutePath());
        }

        MyRecord record = new MyRecord(formatTime, userKey, realFileName,
                hikvisonCapture, faceCapture);
        faceModel.insert(record);
    }


    //--------------开关门
    private void openDoor() {
        new Manager(getApplicationContext()).setGateIO(true);
    }

    private void closeDoor() {
//        new Manager(getApplicationContext()).setGateIO(false);
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

        int what = msg.what;
        switch (what) {
            case PRINT:
                break;
            case UI_CHANGE_INPUT_TEXT_SELECTION:
                break;
            case UI_CHANGE_SYNTHES_TEXT_SELECTION:
                break;
            default:
                break;
        }
    }

    private void say(String s) {
        if (synthesizer != null && isVoiceInit) {
            synthesizer.speak(s);
        }
    }

}
