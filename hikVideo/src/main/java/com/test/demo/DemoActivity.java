/**
 * <p>DemoActivity Class</p>
 *
 * @author zhuzhenlei 2014-7-17
 * @version V1.0
 * @modificationHistory
 * @modify by user:
 * @modify by reason:
 */
package com.test.demo;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.INT_PTR;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_V30;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PLAYBACK_INFO;
import com.hikvision.netsdk.NET_DVR_PLAYCOND;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.NET_DVR_VOD_PARA;
import com.hikvision.netsdk.PTZCommand;
import com.hikvision.netsdk.PlaybackCallBack;
import com.hikvision.netsdk.PlaybackControlCommand;
import com.hikvision.netsdk.StdDataCallBack;
import com.hikvision.netsdk.VoiceDataCallBack;
import com.hikvision.netsdk.RealPlayCallBack;
import com.hikvision.netsdk.RealDataCallBack;

/**
 * <pre>
 *  ClassName  DemoActivity Class
 * </pre>
 *
 * @author zhuzhenlei
 * @version V1.0
 * @modificationHistory
 */
public class DemoActivity extends Activity implements Callback {
    private Button m_oLoginBtn = null;
    private Button m_oPreviewBtn = null;
    private Button m_oPlaybackBtn = null;
    private Button m_oParamCfgBtn = null;
    private Button m_oCaptureBtn = null;
    private Button m_oRecordBtn = null;
    private Button m_oTalkBtn = null;
    private Button m_oPTZBtn = null;
    private Button m_oOtherBtn = null;
    private EditText m_oIPAddr = null;
    private EditText m_oPort = null;
    private EditText m_oUser = null;
    private EditText m_oPsd = null;
    private SurfaceView m_osurfaceView = null;
    private final static int REQUEST_CODE = 1;

    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
    private StdDataCallBack cbf = null;

    private int m_iLogID = -1; // return by NET_DVR_Login_v30
    private int m_iPlayID = -1; // return by NET_DVR_RealPlay_V40
    private int m_iPlaybackID = -1; // return by NET_DVR_PlayBackByTime

    private int m_iStartChan = 0; // start channel number
    private int m_iChanNum = 0; // channel number
    private static PlaySurfaceView[] playView = new PlaySurfaceView[4];

    private final String TAG = "DemoActivity";

    private boolean m_bTalkOn = false;
    private boolean m_bPTZL = false;
    private boolean m_bMultiPlay = false;
    private boolean m_bSaveRealData = false;
    private boolean m_bStopPlayback = false;
    private boolean m_bPlaybackByName = false;
    private String m_retUrl = "";

    public static String accessToken = "";
    public static String areaDomain = "";
    public static String appkey = ""; // fill in with appkey
    public static String appSecret = ""; // fill in with appSecret

    public DemoActivity Demo;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashUtil crashUtil = CrashUtil.getInstance();
        crashUtil.init(this);

        setContentView(R.layout.main);

        if (!initeSdk()) {
            this.finish();
            return;
        }

        if (!initeActivity()) {
            this.finish();
            return;
        }

        m_oIPAddr.setText("192.168.3.99");
        m_oPort.setText("8000");
        m_oUser.setText("admin");
        m_oPsd.setText("qaz123456");
    }

    // @Override
    public void surfaceCreated(SurfaceHolder holder) {
        m_osurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        Log.i(TAG, "surface is created");

        //valid just when single channel preview
        if (-1 == m_iPlayID && -1 == m_iPlaybackID) {
            return;
        }
        playView[0].m_hHolder = holder;
        Surface surface = holder.getSurface();
        if (true == surface.isValid()) {
            if (m_iPlayID != -1) {
                if (-1 == HCNetSDK.getInstance().NET_DVR_RealPlaySurfaceChanged(m_iPlayID, 0, holder)) {
                    Log.e(TAG, "Player setVideoWindow failed!");
                }
            } else {
                if (-1 == HCNetSDK.getInstance().NET_DVR_PlayBackSurfaceChanged(m_iPlaybackID, 0, holder)) {
                    Log.e(TAG, "Player setVideoWindow failed!");
                }
            }

        }
    }

    // @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surface changed");
    }

    // @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "Player setVideoWindow release!");
        if (-1 == m_iPlayID && -1 == m_iPlaybackID) {
            return;
        }
        if (true == holder.getSurface().isValid()) {
            if (m_iPlayID != -1) {
                if (-1 == HCNetSDK.getInstance().NET_DVR_RealPlaySurfaceChanged(m_iPlayID, 0, null)) {
                    Log.e(TAG, "Player setVideoWindow failed!");
                }
            } else {
                if (-1 == HCNetSDK.getInstance().NET_DVR_PlayBackSurfaceChanged(m_iPlaybackID, 0, null)) {
                    Log.e(TAG, "Player setVideoWindow failed!");
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("m_iPlayID", m_iPlayID);
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        m_iPlayID = savedInstanceState.getInt("m_iPlayID");
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }

    /**
     * @fn initeSdk
     * @author zhuzhenlei
     * @brief SDK init
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return true - success;false - fail
     */
    private boolean initeSdk() {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/", true);
        return true;
    }

    // GUI init
    private boolean initeActivity() {
        findViews();
        m_osurfaceView.getHolder().addCallback(this);
        setListeners();
        return true;
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

    // get controller instance
    private void findViews() {
        m_oLoginBtn = (Button) findViewById(R.id.btn_Login);
        m_oPreviewBtn = (Button) findViewById(R.id.btn_Preview);
        m_oPlaybackBtn = (Button) findViewById(R.id.btn_Playback);
        m_oParamCfgBtn = (Button) findViewById(R.id.btn_ParamCfg);
        m_oCaptureBtn = (Button) findViewById(R.id.btn_Capture);
        m_oRecordBtn = (Button) findViewById(R.id.btn_Record);
        m_oTalkBtn = (Button) findViewById(R.id.btn_Talk);
        m_oPTZBtn = (Button) findViewById(R.id.btn_PTZ);
        m_oOtherBtn = (Button) findViewById(R.id.btn_OTHER);
        m_oIPAddr = (EditText) findViewById(R.id.EDT_IPAddr);
        m_oPort = (EditText) findViewById(R.id.EDT_Port);
        m_oUser = (EditText) findViewById(R.id.EDT_User);
        m_oPsd = (EditText) findViewById(R.id.EDT_Psd);
        m_osurfaceView = (SurfaceView) findViewById(R.id.Sur_Player);
    }

    // listen
    private void setListeners() {
        m_oLoginBtn.setOnClickListener(Login_Listener);
        m_oPreviewBtn.setOnClickListener(Preview_Listener);
        m_oPlaybackBtn.setOnClickListener(Playback_Listener);
        m_oParamCfgBtn.setOnClickListener(ParamCfg_Listener);
        m_oCaptureBtn.setOnClickListener(Capture_Listener);
        m_oRecordBtn.setOnClickListener(Record_Listener);
        m_oTalkBtn.setOnClickListener(Talk_Listener);
        m_oOtherBtn.setOnClickListener(OtherFunc_Listener);
        m_oPTZBtn.setOnTouchListener(PTZ_Listener);
    }

    // ptz listener
    private Button.OnTouchListener PTZ_Listener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                if (m_iLogID < 0) {
                    Log.e(TAG, "please login on a device first");
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (m_bPTZL == false) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, m_iStartChan, PTZCommand.PAN_LEFT, 0)) {
                            Log.e(TAG, "start PAN_LEFT failed with error code: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "start PAN_LEFT succ");
                        }
                    } else {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, m_iStartChan, PTZCommand.PAN_RIGHT, 0)) {
                            Log.e(TAG, "start PAN_RIGHT failed with error code: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "start PAN_RIGHT succ");
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (m_bPTZL == false) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, m_iStartChan, PTZCommand.PAN_LEFT, 1)) {
                            Log.e(TAG, "stop PAN_LEFT failed with error code: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "stop PAN_LEFT succ");
                        }
                        m_bPTZL = true;
                        m_oPTZBtn.setText("PTZ(R)");
                    } else {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, m_iStartChan, PTZCommand.PAN_RIGHT, 1)) {
                            Log.e(TAG, "stop PAN_RIGHT failed with error code: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "stop PAN_RIGHT succ");
                        }
                        m_bPTZL = false;
                        m_oPTZBtn.setText("PTZ(L)");
                    }
                }
                return true;
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
                return false;
            }
        }
    };


    // otherFUnc listener
    private Button.OnClickListener OtherFunc_Listener = new OnClickListener() {
        public void onClick(View v) {
            //PTZTest.TEST_PTZ(m_iPlayID, m_iLogID, m_iStartChan);
            // ConfigTest.Test_ScreenConfig(m_iLogID, m_iStartChan);
            // PTZTest.TEST_PTZ(m_iPlayID, m_iLogID, m_iStartChan);

            /*
             * try { //PictureTest.PicUpload(m_iLogID); } catch
             * (InterruptedException e) { // TODO Auto-generated catch block
             * e.printStackTrace(); }
             */

            //PictureTest.BaseMap(m_iLogID);
            //DecodeTest.PicPreview(m_iLogID);
            //ManageTest.TEST_Manage(m_iLogID);
            // AlarmTest.Test_SetupAlarm(m_iLogID);
            //OtherFunction.TEST_OtherFunc(m_iPlayID, m_iLogID, m_iStartChan);
            JNATest.TEST_Config(m_iPlayID, m_iLogID, m_iStartChan);
            //JNATest.TEST_EzvizConfig(m_iPlayID, m_iLogID, m_iStartChan);
            //ConfigTest.TEST_Config(m_iPlayID, m_iLogID, m_iStartChan);
            // HttpTest.Test_HTTP();
            //ScreenTest.TEST_Screen(m_iLogID);

            //get_ddns_Info(appkey, appSecret);  //get ddns info by using appkey and app secret
            //get_ddns_Info_HC();     	       	   
        }
    };

    // Test Activity result
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == 1 && data != null) {
                m_retUrl = data.getStringExtra("Info");
                Log.e(TAG, "m_retUrl: " + m_retUrl);

                accessToken = m_retUrl.substring(m_retUrl.indexOf("access_token") + 13, m_retUrl.indexOf("access_token") + 77);
                Log.e(TAG, "accessToken: " + accessToken);
                areaDomain = m_retUrl.substring(m_retUrl.indexOf("areaDomain") + 11);
                Log.e(TAG, "areaDomain: " + areaDomain);
            } else {
                Log.e(TAG, "resultCode!= 1");
            }

            Demo = new DemoActivity();
            new Thread(new Runnable() {                                        //inner class - new thread to get device list
                @Override
                public void run() {
                    Demo.get_device_ip();
                }
            }).start();
        }
    }


    // Talk listener
    private Button.OnClickListener Talk_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                int iDeviceType = 1;
                if (m_iChanNum == 1) {
                    iDeviceType = 1;
                } else {
                    iDeviceType = 0;
                }
                if (m_bTalkOn == false) {
                    if (VoiceTalk.TEST_VoiceTalk(m_iLogID, m_iStartChan, iDeviceType, 1)) {
                        m_bTalkOn = true;
                        m_oTalkBtn.setText("Stop");
                    }
                } else {
                    if (VoiceTalk.TEST_VoiceTalk(m_iLogID, m_iStartChan, iDeviceType, 0)) {
                        m_bTalkOn = false;
                        m_oTalkBtn.setText("Talk");
                    }
                }
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };
    // record listener
    private Button.OnClickListener Record_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            if (!m_bSaveRealData) {
                if (!HCNetSDKJNAInstance.getInstance().NET_DVR_SaveRealData_V30(m_iPlayID, 0x2, "/sdcard/test.mp4")) {
                    System.out.println("NET_DVR_SaveRealData_V30 failed! error: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    return;
                } else {
                    System.out.println("NET_DVR_SaveRealData_V30 succ!");
                }
                m_bSaveRealData = true;
            } else {
                if (!HCNetSDK.getInstance().NET_DVR_StopSaveRealData(m_iPlayID)) {
                    System.out.println("NET_DVR_StopSaveRealData failed! error: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                } else {
                    System.out.println("NET_DVR_StopSaveRealData succ!");
                }
                m_bSaveRealData = false;
            }
        }
    };

    // capture listener
    private Button.OnClickListener Capture_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                if (m_iPlayID < 0 && m_iPlaybackID < 0) {
                    Log.e(TAG, "please start preview first");
                    return;
                }
                if (m_iPlayID >= 0) {
//            		HCNetSDKJNAInstance.getInstance().NET_DVR_SetCapturePictureMode(0x1);
                    if (HCNetSDKJNAInstance.getInstance().NET_DVR_CapturePictureBlock(m_iPlayID, Environment.getExternalStorageDirectory() + "/xiaofu_test.jpg", 0)) {

                        Log.i(TAG, "NET_DVR_CapturePictureBlock Succ!");
                    } else {
                        Log.e(TAG, "NET_DVR_CapturePictureBlock fail! Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    }
                } else {
                    if (HCNetSDKJNAInstance.getInstance().NET_DVR_PlayBackCaptureFile(m_iPlaybackID, "/sdcard/capfile.bmp")) {
                        Log.i(TAG, "NET_DVR_PlayBackCaptureFile succ");
                    } else {
                        Log.e(TAG, "NET_DVR_PlayBackCaptureFile fail " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    }
                }
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };

    private Button.OnClickListener Playback_Listener = new Button.OnClickListener() {

        public void onClick(View v) {
            try {
                if (m_iLogID < 0) {
                    Log.e(TAG, "please login on a device first");
                    return;
                }
                if (m_iPlaybackID < 0) {
                    if (m_iPlayID >= 0) {
                        Log.i(TAG, "Please stop preview first");
                        return;
                    }

                    ChangeSingleSurFace(true);

                    if (!m_bPlaybackByName) {
                        NET_DVR_TIME timeStart = new NET_DVR_TIME();
                        NET_DVR_TIME timeStop = new NET_DVR_TIME();

                        timeStart.dwYear = 2018;
                        timeStart.dwMonth = 1;
                        timeStart.dwDay = 1;
                        timeStop.dwYear = 2018;
                        timeStop.dwMonth = 8;
                        timeStop.dwDay = 31;

                        NET_DVR_VOD_PARA vodParma = new NET_DVR_VOD_PARA();
                        vodParma.struBeginTime = timeStart;
                        vodParma.struEndTime = timeStop;
                        vodParma.byStreamType = 0;
                        vodParma.struIDInfo.dwChannel = m_iStartChan;
                        vodParma.hWnd = playView[0].getHolder().getSurface();
                        m_iPlaybackID = HCNetSDK.getInstance().NET_DVR_PlayBackByTime_V40(m_iLogID, vodParma);
                   	    /*reverse playback
                   	 	NET_DVR_PLAYCOND playcond = new NET_DVR_PLAYCOND();
                   	 	playcond.dwChannel = m_iStartChan;
                   	 	playcond.struStartTime = timeStart;
                   	 	playcond.struStopTime = timeStop;
                   	 	playcond.byDrawFrame = 0;
                   	 	playcond.byStreamType = 1;                  	 
                       
                        m_iPlaybackID = HCNetSDK.getInstance().NET_DVR_PlayBackReverseByTime_V40(m_iLogID, playView[0].getHolder().getSurface(), playcond);  
                        */
                    } else {
                        m_iPlaybackID = HCNetSDK.getInstance().NET_DVR_PlayBackByName(m_iLogID, new String("ch0001_00000000300000000"), playView[0].getHolder().getSurface());
                    }

                    if (m_iPlaybackID >= 0) {
                        NET_DVR_PLAYBACK_INFO struPlaybackInfo = null;
                        if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYSTART, null, 0, null)) {
                            Log.e(TAG, "net sdk playback start failed!");
                            return;
                        }
                        m_bStopPlayback = false;
                        m_oPlaybackBtn.setText("Stop");

                       /*Test code 
                   	   if(HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYPAUSE, null, 0, struPlaybackInfo))
                   	   {
                   		   Log.i(TAG, "NET_DVR_PlayBackControl_V40 pause succ"); 
                   	   }
                   	   if(HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYRESTART, null, 0, struPlaybackInfo))
                   	   {
                   		   Log.i(TAG, "NET_DVR_PLAYRESTART restart succ"); 
                   	   }
                   	   if(HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYFAST, null, 0, struPlaybackInfo))
                   	   {
                   		   Log.i(TAG, "NET_DVR_PLAYFAST succ"); 
                   	   }
                   	   if(HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYSLOW, null, 0, struPlaybackInfo))
                   	   {
                   		   Log.i(TAG, "NET_DVR_PLAYSLOW succ"); 
                   	   }        
                   	   if(HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYSTARTAUDIO, null, 0, struPlaybackInfo))
                   	   {
                   		   Log.i(TAG, "NET_DVR_PLAYSTARTAUDIO succ"); 
                   	   }
                   	   byte[] lpInBuf = new byte[60];
                   	   lpInBuf[0] = 5;	
                   	   if(HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYAUDIOVOLUME, lpInBuf, 4, struPlaybackInfo))
                   	   {
                   		   Log.i(TAG, "NET_DVR_PLAYAUDIOVOLUME succ"); 
                   	   }
                   	   else
                   	   {
                     	 Log.e(TAG, "NET_DVR_PLAYAUDIOVOLUME fail");
                   	   }                      
                   	   if(HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYSTOPAUDIO, null, 0, struPlaybackInfo))
                   	   {
                   		   Log.i(TAG, "NET_DVR_PLAYSTOPAUDIO succ"); 
                   	   }
					   */	

                        /*Test code ---getPlaybackPos
                        Thread thread11 = new Thread() 
                        {
                            public void run() 
                            {
                                int nProgress = -1;
                                while (true) 
                                {
                                    nProgress = HCNetSDK.getInstance().NET_DVR_GetPlayBackPos(m_iPlaybackID);
                                    System.out.println("NET_DVR_GetPlayBackPos:" + nProgress);
                                    if (nProgress < 0 || nProgress >= 100) 
                                    {
                                        break;
                                    }
                                    try 
                                    {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) 
                                    { // TODO
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        thread11.start();
                        */
                    } else {
                        Log.i(TAG, "NET_DVR_PlayBackByName failed, error code: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    }
                } else {
                    m_bStopPlayback = true;
                    if (!HCNetSDK.getInstance().NET_DVR_StopPlayBack(m_iPlaybackID)) {
                        Log.e(TAG, "net sdk stop playback failed");
                    } // player stop play
                    m_oPlaybackBtn.setText("Playback");
                    m_iPlaybackID = -1;

                    ChangeSingleSurFace(false);
                }
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };


    // login listener
    private Button.OnClickListener Login_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                if (m_iLogID < 0) {
                    // login on the device
                    m_iLogID = loginDevice();
                    if (m_iLogID < 0) {
                        Log.e(TAG, "This device logins failed!");
                        return;
                    } else {
                        Log.i(TAG, "m_iLogID=" + m_iLogID);
                    }
                    // get instance of exception callback and set
                    ExceptionCallBack oexceptionCbf = getExceptiongCbf();
                    if (oexceptionCbf == null) {
                        Log.e(TAG, "ExceptionCallBack object is failed!");
                        return;
                    }

                    if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(oexceptionCbf)) {
                        Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
                        return;
                    }

                    m_oLoginBtn.setText("Logout");
                    Log.i(TAG, "Login sucess");
                } else {
                    // whether we have logout
                    if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
                        Log.e(TAG, " NET_DVR_Logout is failed!");
                        //if (!HCNetSDKJNAInstance.getInstance().NET_DVR_DeleteOpenEzvizUser(m_iLogID)) {
                        //		Log.e(TAG, " NET_DVR_DeleteOpenEzvizUser is failed!");
                        return;
                    }
                    m_oLoginBtn.setText("Login");
                    m_iLogID = -1;
                }

            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };

    // Preview listener
    private Button.OnClickListener Preview_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(DemoActivity.this
                                        .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                if (m_iLogID < 0) {
                    Log.e(TAG, "please login on device first");
                    return;
                }

                if (m_iPlaybackID >= 0) {
                    Log.i(TAG, "Please stop palyback first");
                    return;
                }

                if (m_iChanNum > 1)// preview more than a channel
                {
                    if (!m_bMultiPlay) {
                        startMultiPreview();
                        m_bMultiPlay = true;
                        m_oPreviewBtn.setText("Stop");
                    } else {
                        stopMultiPreview();
                        m_bMultiPlay = false;
                        m_oPreviewBtn.setText("Preview");
                    }
                } else // preivew a channel
                {
                    if (m_iPlayID < 0) {
                        startSinglePreview();
                    } else {
                        stopSinglePreview();
                        m_oPreviewBtn.setText("Preview");
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
    };

    /**
     * @fn getStdDataPlayerCbf
     * @author
     * @brief get realplay callback instance
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return callback instance
     */
//    private static StdDataCallBack cbf = null;
//    private StdDataCallBack getStdDataPlayerCbf() {
//    	
//    	StdDataCallBack cbf = new StdDataCallBack(){ 
//            public void fStdDataCallback(int iRealHandle, int iDataType,
//                    byte[] pDataBuffer, int iDataSize) {

//            }
//        };
//        return cbf;
//    };


    // configuration listener
    private Button.OnClickListener ParamCfg_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                paramCfg(m_iLogID);
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };

    private void startSinglePreview() {
        if (m_iPlaybackID >= 0) {
            Log.i(TAG, "Please stop palyback first");
            return;
        }

        Log.i(TAG, "m_iStartChan:" + m_iStartChan);

        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = m_iStartChan;
        previewInfo.dwStreamType = 0; // main stream
        previewInfo.bBlocked = 1;
        previewInfo.hHwnd = playView[0].m_hHolder;

        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID, previewInfo, null);
        if (m_iPlayID < 0) {
            Log.e(TAG, "NET_DVR_RealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        
        /* open sound                
        boolean bRet = HCNetSDKJNAInstance.getInstance().NET_DVR_OpenSound(m_iPlayID);
        if(bRet)
        {
        	Log.e(TAG, "NET_DVR_OpenSound Succ!");
        }
        */
        
        /* capture picture
        if(HCNetSDKJNAInstance.getInstance().NET_DVR_CapturePicture(m_iPlayID, "/mnt/sdcard/capture_01.dmp"))
        {
        	Log.e(TAG, "NET_DVR_CapturePicture Succ!");
        }
        else
        {
        	Log.e(TAG, "NET_DVR_CapturePicture fail! Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        }     
        */
        
        /* set volume
        short volume = 55;
        if(HCNetSDKJNAInstance.getInstance().NET_DVR_Volume(m_iPlayID, volume))
        {
        	Log.e(TAG, "NET_DVR_Volume Succ!");
        }
        */
        
        /* std data call back
        if(cbf == null)
        {
            cbf = new StdDataCallBack()
            {
                public void fStdDataCallback(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize) 
                {
                    //handle stream data
                }
                	
            };
         }
                
         if(!HCNetSDK.getInstance().NET_DVR_SetStandardDataCallBack(m_iPlayID, cbf))
         {
             Log.e(TAG, "NET_DVR_SetStandardDataCallBack is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
                
         }
         else
         {
        	 Log.i(TAG, "NET_DVR_SetStandardDataCallBack sucess");                      
         }
         */

        m_oPreviewBtn.setText("Stop");
    }

    private void startMultiPreview() {
        //one by one
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

    /**
     * @fn stopSinglePreview
     * @author zhuzhenlei
     * @brief stop preview
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return NULL
     */
    private void stopSinglePreview() {
        if (m_iPlayID < 0) {
            Log.e(TAG, "m_iPlayID < 0");
            return;
        }

        if (HCNetSDKJNAInstance.getInstance().NET_DVR_CloseSound()) {
            Log.e(TAG, "NET_DVR_CloseSound Succ!");
        }

        // net sdk stop preview
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            Log.e(TAG, "StopRealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        Log.i(TAG, "NET_DVR_StopRealPlay succ");
        m_iPlayID = -1;
    }

    /**
     * @fn loginNormalDevice
     * @author zhuzhenlei
     * @brief login on device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    private int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        String strIP = m_oIPAddr.getText().toString();
        int nPort = Integer.parseInt(m_oPort.getText().toString());
        String strUser = m_oUser.getText().toString();
        String strPsd = m_oPsd.getText().toString();

        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort, strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
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
        Log.i(TAG, "NET_DVR_Login is Successful!");
        return iLogID;
    }

    public static void Test_XMLAbility(int iUserID) {
        byte[] arrayOutBuf = new byte[64 * 1024];
        INT_PTR intPtr = new INT_PTR();
        String strInput = new String("<AlarmHostAbility version=\"2.0\"></AlarmHostAbility>");
        byte[] arrayInBuf = new byte[8 * 1024];
        arrayInBuf = strInput.getBytes();
        if (!HCNetSDK.getInstance().NET_DVR_GetXMLAbility(iUserID, HCNetSDK.DEVICE_ABILITY_INFO, arrayInBuf, strInput.length(), arrayOutBuf, 64 * 1024, intPtr)) {
            System.out.println("get DEVICE_ABILITY_INFO faild!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            System.out.println("get DEVICE_ABILITY_INFO succ!");
        }
    }

    /**
     * @fn loginEzvizDevice
     * @author liuyu6
     * @brief login on ezviz device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    private int loginEzvizDevice() {
        return -1;
        /*
         * NET_DVR_OPEN_EZVIZ_USER_LOGIN_INFO struLoginInfo = new
         * NET_DVR_OPEN_EZVIZ_USER_LOGIN_INFO(); NET_DVR_DEVICEINFO_V30
         * struDeviceInfo = new NET_DVR_DEVICEINFO_V30();
         *
         * //String strInput = new String("pbsgp.p2papi.ezviz7.com"); String
         * strInput = new String("open.ys7.com"); //String strInput = new
         * String("pbdev.ys7.com"); //String strInput = new
         * String("183.136.184.67"); byte[] byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sEzvizServerAddress, 0,
         * byInput.length);
         *
         * struLoginInfo.wPort = 443;
         *
         * strInput = new
         * String("at.43anfq0q9k8zt06vd0ppalfhc4bj177p-3k4ovrh4vu-105zgp6-jgt8edqst"
         * ); byInput = strInput.getBytes(); System.arraycopy(byInput, 0,
         * struLoginInfo.sAccessToken, 0, byInput.length);
         *
         * //strInput = new String("67a7daedd4654dc5be329f2289914859");
         * //byInput = strInput.getBytes(); //System.arraycopy(byInput, 0,
         * struLoginInfo.sSessionID, 0, byInput.length);
         *
         * //strInput = new String("ae1b9af9dcac4caeb88da6dbbf2dd8d5"); strInput
         * = new String("com.hik.visualintercom"); byInput =
         * strInput.getBytes(); System.arraycopy(byInput, 0,
         * struLoginInfo.sAppID, 0, byInput.length);
         *
         * //strInput = new String("78313dadecd92bd11623638d57aa5128"); strInput
         * = new String("226f102a99ad0e078504d380b9ddf760"); byInput =
         * strInput.getBytes(); System.arraycopy(byInput, 0,
         * struLoginInfo.sFeatureCode, 0, byInput.length);
         *
         * //strInput = new
         * String("https://pbopen.ys7.com:443/api/device/transmission");
         * strInput = new String("/api/device/transmission"); byInput =
         * strInput.getBytes(); System.arraycopy(byInput, 0, struLoginInfo.sUrl,
         * 0, byInput.length);
         *
         * strInput = new String("520247131"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sDeviceID, 0,
         * byInput.length);
         *
         * strInput = new String("2"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sClientType, 0,
         * byInput.length);
         *
         * strInput = new String("UNKNOWN"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sNetType, 0,
         * byInput.length);
         *
         * strInput = new String("5.0.1"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sOsVersion, 0,
         * byInput.length);
         *
         * strInput = new String("v.5.1.5.30"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sSdkVersion, 0,
         * byInput.length);
         *
         * int iUserID = -1;
         *
         * iUserID =
         * HCNetSDK.getInstance().NET_DVR_CreateOpenEzvizUser(struLoginInfo,
         * struDeviceInfo);
         *
         * if (-1 == iUserID) { System.out.println("NET_DVR_CreateOpenEzvizUser"
         * + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError()); return
         * -1; } else {
         * System.out.println("NET_DVR_CreateOpenEzvizUser success"); }
         *
         * Test_XMLAbility(iUserID); Test_XMLAbility(iUserID);
         * Test_XMLAbility(iUserID);
         *
         * return iUserID;
         */
    }

    /**
     * @fn loginDevice
     * @author zhangqing
     * @brief login on device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    private int loginDevice() {
        int iLogID = -1;
        iLogID = loginNormalDevice();
        // iLogID = JNATest.TEST_EzvizLogin();
        // iLogID = loginEzvizDevice();
        return iLogID;
    }


    private void paramCfg(final int iUserID) {
        // whether have logined on
        if (iUserID < 0) {
            Log.e(TAG, "iUserID < 0");
            return;
        }

        NET_DVR_COMPRESSIONCFG_V30 struCompress = new NET_DVR_COMPRESSIONCFG_V30();
        if (!HCNetSDK.getInstance().NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30, m_iStartChan, struCompress)) {
            Log.e(TAG, "NET_DVR_GET_COMPRESSCFG_V30 failed with error code:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.i(TAG, "NET_DVR_GET_COMPRESSCFG_V30 succ");
        }

        // set substream resolution to cif
        struCompress.struNetPara.byResolution = 1;
        if (!HCNetSDK.getInstance().NET_DVR_SetDVRConfig(iUserID, HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30, m_iStartChan, struCompress)) {
            Log.e(TAG, "NET_DVR_SET_COMPRESSCFG_V30 failed with error code:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.i(TAG, "NET_DVR_SET_COMPRESSCFG_V30 succ");
        }
    }

    /**
     * @fn getExceptiongCbf
     * @author zhuzhenlei
     * @brief process exception
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return exception instance
     */
    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                System.out.println("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }

    /**
     * @fn Cleanup
     * @author zhuzhenlei
     * @brief cleanup
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return NULL
     */
    public void Cleanup() {
        // release net SDK resource
        HCNetSDK.getInstance().NET_DVR_Cleanup();
    }

    public void get_access_token(String appKey, String appSecret) {
        Log.e(TAG, "get_access_token in");

        if (appKey == "" || appSecret == "") {
            Log.e(TAG, "appKey or appSecret is null");
            return;
        }

        try {
            String url = "https://open.ezvizlife.com/api/lapp/token/get";
            URL getDeviceUrl = new URL(url);
            /*Set Http Request Header*/
            HttpURLConnection connection = (HttpURLConnection) getDeviceUrl.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Host", "isgpopen.ezvizlife.com");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            PrintWriter PostParam = new PrintWriter(connection.getOutputStream());
            String sendParam = "appKey=" + appKey + "&appSecret=" + appSecret;
            PostParam.print(sendParam);
            PostParam.flush();

            BufferedReader inBuf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JSONObject RetValue = new JSONObject(new String(inBuf.readLine().getBytes(), "utf-8"));
            int RetCode = Integer.parseInt(RetValue.getString("code"));
            if (RetCode != 200) {
                Log.e(TAG, "Get DDNS Info fail! Err code: " + RetCode);
                return;
            } else {
                JSONObject DetailInfo = RetValue.getJSONObject("data");
                accessToken = DetailInfo.getString("accessToken");
                Log.e(TAG, "accessToken: " + accessToken);
                areaDomain = DetailInfo.getString("areaDomain");
                Log.e(TAG, "areaDomain: " + areaDomain);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getKey() {
        return appkey;
    }

    public String getSecret() {
        return appSecret;
    }

    void get_device_ip() {
        String deviceSerial = "711563208" /*m_oIPAddr.getText().toString()*/;  //IP text instead of deviceSerial
        if (deviceSerial == null) {
            Log.e(TAG, "deviceSerial is null ");
            return;
        }

        try {
            String url = areaDomain + "/api/lapp/ddns/get";
            URL getDeviceUrl = new URL(url);
            /*Set Http Request Header*/
            HttpURLConnection connection = (HttpURLConnection) getDeviceUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Host", "isgpopen.ezvizlife.com");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            PrintWriter PostParam = new PrintWriter(connection.getOutputStream());
            String sendParam = "accessToken=" + accessToken + "&deviceSerial=" + deviceSerial;
//            String sendParam = "accessToken=" + accessToken + "&domain=" + areaDomain;  
            System.out.println(sendParam);
            PostParam.print(sendParam);
            PostParam.flush();

            BufferedReader inBuf = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            JSONObject RetValue = new JSONObject(new String(inBuf.readLine().getBytes(), "utf-8"));
            Log.e(TAG, "RetValue = " + RetValue);
            return;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject get_ddns_Info(String appkey, String appSecret) {
        try {
            if (m_retUrl != "") {
                Log.e(TAG, "m_retUrl != null ");
                accessToken = m_retUrl.substring(m_retUrl.indexOf("access_token") + 13, m_retUrl.indexOf("access_token") + 77);
                Log.e(TAG, "accessToken: " + accessToken);
                areaDomain = m_retUrl.substring(m_retUrl.indexOf("areaDomain") + 11);
                Log.e(TAG, "areaDomain: " + areaDomain);
            } else {
                Demo = new DemoActivity();
                new Thread(new Runnable() {                                        //inner class - new thread to get device list
                    @Override
                    public void run() {
                        Demo.get_access_token(Demo.getKey(), Demo.getSecret());
                        Demo.get_device_ip();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void get_ddns_Info_HC() {
        Intent intent = new Intent(DemoActivity.this, TestActivity.class);     //skip to HC page
        startActivityForResult(intent, REQUEST_CODE);  //get ddns info by using HC
    }

}