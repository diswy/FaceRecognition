package com.yibaiqi.face.recognition.ui.core;

import android.content.Intent;
import android.widget.TextView;

import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private TextView btnCamera;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_settings;
    }

    @Override
    public void initView() {
        btnCamera = findViewById(R.id.btn_camera_setting);
    }

    @Override
    public void initialize() {

    }

    @Override
    protected void initListener() {
        btnCamera.setOnClickListener(v -> {
            startActivity(new Intent(this, CameraSettingsActivity.class));
        });
    }
}
