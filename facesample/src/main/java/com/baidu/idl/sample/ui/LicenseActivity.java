package com.baidu.idl.sample.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.facesdk.callback.AuthCallback;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.utils.ToastUtils;

import static com.baidu.idl.sample.common.GlobalSet.LICENSE_APP;
import static com.baidu.idl.sample.common.GlobalSet.LICENSE_OFFLINE;
import static com.baidu.idl.sample.common.GlobalSet.LICENSE_ONLINE;

/**
 * Created by litonghui on 2018/11/16.
 */

public class LicenseActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private Context mContext;
    private FaceAuth faceAuth;
    private int lastKeyLen = 0;

    private EditText etKey;
    private TextView tvDevice;
    private TextView btAppAuth;
    private TextView btOnLineActive;
    private TextView btOffLineActive;
    private String deviceId;
    private TextView mTvReminder;
    private View mLayoutOnline;
    private TextView btnBack;
    private View mLayoutOffline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_activate);
        mContext = this;
        faceAuth = new FaceAuth();
        // 建议3288板子flagsThreads设置2,3399板子设置4
        faceAuth.setAnakinThreadsConfigure(2, 0);
        initView();
        ifUserLicense();
    }

    /**
     * 判断是否需要用户再次
     * 点击进行鉴权
     */
    private void ifUserLicense() {
        int status = GlobalSet.getLicenseStatus();
        switch (status) {
            case 0:
                return;
            case LICENSE_APP:
                initLicense();
                break;
            case LICENSE_ONLINE:
                String key = GlobalSet.getLicenseOnLineKey();
                initLicenseOnLine(key);
                break;
            case LICENSE_OFFLINE:
                initLicenseOffLine();
                break;
            default:
                break;
        }
    }

    private void initView() {
        etKey = findViewById(R.id.et_key);
        etKey.setTransformationMethod(new AllCapTransformationMethod(true));
        tvDevice = findViewById(R.id.tv_device);
        btAppAuth = findViewById(R.id.bt_application_auth);
        btOffLineActive = findViewById(R.id.bt_off_line_active);
        btOnLineActive = findViewById(R.id.bt_on_line_active);
        btOnLineActive.setOnClickListener(this);
        btOffLineActive.setOnClickListener(this);
//         btAppAuth.setOnClickListener(this);
//         btAppAuth.setOnTouchListener(this);
        btOffLineActive.setOnTouchListener(this);
        btOnLineActive.setOnTouchListener(this);
        btOnLineActive.setOnTouchListener(this);
        deviceId = faceAuth.getDeviceId(this);
        etKey.setText(PreferencesUtil.getString("activate_on_key", ""));
        // TODO 测试直接写死
        etKey.setText("ZUJN-TZD0-BPYP-BSSU");
        tvDevice.setText("设备指纹：" + deviceId);
        addLisenter();

        mTvReminder = findViewById(R.id.tv_active_reminder);
        mLayoutOnline = findViewById(R.id.layout_online_active);
        mLayoutOffline = findViewById(R.id.layout_offline_active);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        btnBack.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_application_auth) {
            mLayoutOffline.setVisibility(View.VISIBLE);
            mLayoutOnline.setVisibility(View.GONE);
            mTvReminder.setText(R.string.license_application_reminder);

            faceAuth.setActiveLog(FaceAuth.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE);
            initLicense();

        } else if (i == R.id.bt_on_line_active) {

            mLayoutOffline.setVisibility(View.GONE);
            mLayoutOnline.setVisibility(View.VISIBLE);
            mTvReminder.setText(R.string.license_online_reminder);
            String key = etKey.getText().toString().trim().toUpperCase();

            faceAuth.setActiveLog(FaceAuth.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE);
            initLicenseOnLine(key);

        } else if (i == R.id.bt_off_line_active) {

            mLayoutOffline.setVisibility(View.VISIBLE);
            mLayoutOnline.setVisibility(View.GONE);
            mTvReminder.setText(R.string.license_offline_reminder);

            faceAuth.setActiveLog(FaceAuth.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE);
            initLicenseOffLine();

        } else if (i == R.id.btn_back) {
            this.finish();
        }
    }

    // 离线鉴权
    private void initLicenseOffLine() {
        faceAuth.initLicenseOffLine(this, new AuthCallback() {
            @Override
            public void onResponse(final int code, final String response, String licenseKey) {
                if (code == 0) {
                    GlobalSet.FACE_AUTH_STATUS = 0;
                    // 初始化人脸
                    FaceSDKManager.getInstance().initModel(mContext);
                    // 初始化数据库
                    DBManager.getInstance().init(getApplicationContext());
                    // 加载feature 内存
                    FaceSDKManager.getInstance().setFeature();
                    GlobalSet.setLicenseStatus(LICENSE_OFFLINE);
                    finish();
                } else {
                    ToastUtils.toast(mContext, code + "  " + response);
                }
            }
        });
    }

    // 在线鉴权
    private void initLicenseOnLine(final String key) {
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(this, "序列号不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        faceAuth.initLicenseOnLine(this, key, new AuthCallback() {
            @Override
            public void onResponse(final int code, final String response, String licenseKey) {
                if (code == 0) {
                    GlobalSet.FACE_AUTH_STATUS = 0;
                    // 初始化人脸
                    FaceSDKManager.getInstance().initModel(mContext);
                    // 初始化数据库
                    DBManager.getInstance().init(getApplicationContext());
                    // 加载feature 内存
                    FaceSDKManager.getInstance().setFeature();
                    GlobalSet.setLicenseOnLineKey(key);
                    GlobalSet.setLicenseStatus(LICENSE_ONLINE);
                    finish();
                } else {
                    ToastUtils.toast(mContext, code + "  " + response);
                }
            }
        });
    }

    // 应用鉴权
    private void initLicense() {
        faceAuth.initLicense(this, "faceexample-face-android-1",
                "idl-license.faceexample-face-android-1", new Callback() {
                    @Override
                    public void onResponse(final int code, final String response) {
                        if (code == 0) {
                            GlobalSet.FACE_AUTH_STATUS = 0;
                            // 初始化人脸
                            FaceSDKManager.getInstance().initModel(mContext);
                            // 初始化数据库
                            DBManager.getInstance().init(getApplicationContext());
                            // 加载feature 内存
                            FaceSDKManager.getInstance().setFeature();
                            GlobalSet.setLicenseStatus(LICENSE_APP);
                            finish();
                        } else {
                            ToastUtils.toast(mContext, code + "  " + response);
                        }

                    }
                });
    }

    private void addLisenter() {
        etKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 19) {
                    etKey.setText(s.toString().substring(0, 19));
                    etKey.setSelection(etKey.getText().length());
                    lastKeyLen = s.length();
                    return;
                }
                if (s.toString().length() < lastKeyLen) {
                    lastKeyLen = s.length();
                    return;
                }
                String text = s.toString().trim();
                if (etKey.getSelectionStart() < text.length()) {
                    return;
                }
                if (text.length() == 4 || text.length() == 9 || text.length() == 14) {
                    etKey.setText(text + "-");
                    etKey.setSelection(etKey.getText().length());
                }

                lastKeyLen = s.length();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.bt_application_auth
                        || v.getId() == R.id.bt_off_line_active
                        || v.getId() == R.id.bt_on_line_active
                        || v.getId() == R.id.btn_back
                        ) {
                    ((TextView) v).setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (v.getId() == R.id.bt_application_auth
                        || v.getId() == R.id.bt_off_line_active
                        || v.getId() == R.id.bt_on_line_active
                        || v.getId() == R.id.btn_back
                        ) {
                    ((TextView) v).setTextColor(getResources().getColor(R.color.btnColor));
                }
                break;
        }
        return false;
    }

    public class AllCapTransformationMethod extends ReplacementTransformationMethod {

        private char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private boolean allUpper = false;

        public AllCapTransformationMethod(boolean needUpper) {
            this.allUpper = needUpper;
        }

        @Override
        protected char[] getOriginal() {
            if (allUpper) {
                return lower;
            } else {
                return upper;
            }
        }

        @Override
        protected char[] getReplacement() {
            if (allUpper) {
                return upper;
            } else {
                return lower;
            }
        }
    }
}