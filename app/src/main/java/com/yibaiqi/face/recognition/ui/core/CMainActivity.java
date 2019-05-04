package com.yibaiqi.face.recognition.ui.core;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.Utils;
import com.baidu.idl.sample.view.MonocularView;
import com.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.seeku.android.Manager;
import com.test.demo.PlaySurfaceView;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.tools.EBQValue;
import com.yibaiqi.face.recognition.tools.FileUtil;
import com.yibaiqi.face.recognition.tools.TimeFormat;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.FaceViewModel;

public class CMainActivity extends BaseActivity implements ILivenessCallBack, SurfaceHolder.Callback {

    private FrameLayout mCameraView;
    private MonocularView mMonocularView;
    private ImageView ivCapture;
    private ImageView ivDb;
    private FaceViewModel faceModel;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_cmain;
    }

    @Override
    public void initView() {
        mCameraView = findViewById(R.id.layout_camera);
        ivCapture = findViewById(R.id.iv_capture);
        ivDb = findViewById(R.id.iv_db);

        // 海康威视
        m_osurfaceView = findViewById(R.id.Sur_Player);

        findViewById(R.id.jietu).setOnClickListener(v -> {

        });
    }

    @Override
    public void initialize() {
        faceModel = ViewModelProviders.of(this, App.getInstance().factory).get(FaceViewModel.class);

        HCNetSDK.getInstance().NET_DVR_Init();
        calculateCameraView();
        // 海康威视
        m_osurfaceView.getHolder().addCallback(this);
        loginHik();
        previewHik();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    captureVideo(myBitmap, feature.getUserName());
                }
            } else {
                ivCapture.setImageBitmap(null);
                ivDb.setImageBitmap(null);
                System.out.println("---------未匹配到人脸");
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
        String strIP = "192.168.3.99";
        int nPort = 8000;
        String strUser = "admin";
        String strPsd = "qaz123456";
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort, strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            return -1;
        }

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
        return (System.currentTimeMillis() - tempTime) > 3000;
    }

    /**
     * 获取摄像头截图,保存送检图片
     */
    private void captureVideo(Bitmap bitmap, String fileName) {
        String formatTime = TimeFormat.FULL(System.currentTimeMillis());
        String realFileName = formatTime + fileName;
        if (HCNetSDKJNAInstance.getInstance().NET_DVR_CapturePictureBlock(m_iPlayID, EBQValue.HIK_PATH + realFileName + ".jpg", 0)) {
            System.out.println("--->>>截图成功");
        } else {
            System.out.println("--->>>截图失败");
        }
        FileUtil.saveBitmap(bitmap, realFileName);
    }


    private void openDoor() {
        new Manager(getApplicationContext()).setGateIO(true);
    }

    private void closeDoor() {
        new Manager(getApplicationContext()).setGateIO(false);
    }

}
