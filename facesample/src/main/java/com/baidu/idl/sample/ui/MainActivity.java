package com.baidu.idl.sample.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.utils.ToastUtils;

/**
 * Created by litonghui on 2018/11/16.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private View mLayoutCamera;
    private View mLayoutHandle;
    private TextView mOperationtTv;
    private TextView mStatusTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLicence();
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCamera();
    }

    private void initView() {

        mLayoutHandle = findViewById(R.id.layout_handle_demo);
        mLayoutCamera = findViewById(R.id.layout_camera);

        mOperationtTv = findViewById(R.id.tv_camera_operation);
        mStatusTv = findViewById(R.id.tv_camera_status);

        String strOperation = String.format("请在“<font color=\"#7D94FF\">%s</font>”" +
                        "中选择外部连接的摄像头，否则无法进行演示操作。",
                getResources().getString(R.string.camera_setting));
        mOperationtTv.setText(Html.fromHtml(strOperation));

        findViewById(R.id.layout_register).setOnClickListener(this);
        findViewById(R.id.layout_manager).setOnClickListener(this);
        findViewById(R.id.layout_pass).setOnClickListener(this);
        findViewById(R.id.layout_settings).setOnClickListener(this);
    }

    private void initLicence() {
        if (GlobalSet.FACE_AUTH_STATUS != 0) {
            startActivity(new Intent(this, LicenseActivity.class));
        }
    }

    private void showCamera() {
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.NO
                || GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB
                || GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR
                ) {
            int number = Camera.getNumberOfCameras();
            if (number == 0) {
                mLayoutCamera.setVisibility(View.VISIBLE);
                mLayoutHandle.setVisibility(View.GONE);
            } else {
                mLayoutCamera.setVisibility(View.GONE);
                mLayoutHandle.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        {
            Intent intent = null;
            if (id == R.id.layout_register && GlobalSet.FACE_AUTH_STATUS == 0) {
                if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB_DEPTH) {
                    if (GlobalSet.getStructuredLightValue() == GlobalSet.STRUCTURED_LIGHT.OBI_ASTRA_PRO) {
                        intent = new Intent(this, OrbbecProRegisterActivity.class);
                    } else if (GlobalSet.getStructuredLightValue() == GlobalSet.STRUCTURED_LIGHT.OBI_ASTRA_MINI) {
                        intent = new Intent(this, OrbbecMiniRegisterActivity.class);
                    } else if (GlobalSet.getStructuredLightValue() == GlobalSet.STRUCTURED_LIGHT.HUAJIE_AMY_MINI) {
                        intent = new Intent(this, IminectRegisterActivity.class);
                    }
                } else {
                    intent = new Intent(this, RegisterActivity.class);
                }
            } else if (id == R.id.layout_manager && GlobalSet.FACE_AUTH_STATUS == 0) {
                intent = new Intent(this, UserActivity.class);
            } else if (id == R.id.layout_pass && GlobalSet.FACE_AUTH_STATUS == 0) {
                if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGB_DEPTH) {
                    if (GlobalSet.getStructuredLightValue() == GlobalSet.STRUCTURED_LIGHT.OBI_ASTRA_PRO) {
                        intent = new Intent(this, OrbbecProPassActivity.class);
                    } else if (GlobalSet.getStructuredLightValue() == GlobalSet.STRUCTURED_LIGHT.OBI_ASTRA_MINI) {
                        intent = new Intent(this, OrbbecMiniPassActivity.class);
                    } else if (GlobalSet.getStructuredLightValue() == GlobalSet.STRUCTURED_LIGHT.HUAJIE_AMY_MINI) {
                        intent = new Intent(this, IminectPassActivity.class);
                    }
                } else {
                    intent = new Intent(this, PassActivity.class);
                }
            } else if (id == R.id.layout_settings) {
                intent = new Intent(this, SettingActivity.class);
            }
            if (intent != null) {
                startActivity(intent);
            } else {
                ToastUtils.toast(this, R.string.license_activate);
            }
        }
    }
}
