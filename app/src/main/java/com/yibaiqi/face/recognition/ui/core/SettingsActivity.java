package com.yibaiqi.face.recognition.ui.core;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.sample.ui.SettingActivity;
import com.baidu.idl.sample.ui.UserActivity;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.FaceViewModel;
import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.DeviceName;
import com.yibaiqi.face.recognition.vo.Resource;

public class SettingsActivity extends BaseActivity {

    private FaceViewModel faceModel;

    private TextView tvLoc, tvType, tvDevice;

    private TextView btnCamera, btnDelay, btnFace, btnTest, btnFaceConfig;

    private ImageView btnBDSetting;


    @Override
    public int getLayoutRes() {
        return R.layout.activity_settings;
    }

    @Override
    public void initView() {
        btnCamera = findViewById(R.id.btn_camera_setting);
        btnDelay = findViewById(R.id.btn_delay_setting);
        btnFace = findViewById(R.id.btn_face_db);
        btnFaceConfig = findViewById(R.id.btn_face_config);
        btnTest = findViewById(R.id.btn_test);
        tvLoc = findViewById(R.id.tv_location);
        tvType = findViewById(R.id.tv_type);
        tvDevice = findViewById(R.id.tv_device);
        btnBDSetting = findViewById(R.id.btn_bd_settings);
    }

    @Override
    public void initialize() {
        faceModel = ViewModelProviders.of(this, App.getInstance().factory).get(FaceViewModel.class);

        faceModel.getDevice().observe(this, new Observer<Resource<BaseResponse<DeviceName>>>() {
            @Override
            public void onChanged(@Nullable Resource<BaseResponse<DeviceName>> resource) {
                if (resource == null)
                    return;
                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null && resource.data.getData() != null) {
                            DeviceName deviceName = resource.data.getData();
                            tvLoc.setText("设备位置：" + deviceName.getLocation_name());
                            tvType.setText("设备类型：" + deviceName.getType_info());
                            tvDevice.setText("设备MAC：" + deviceName.getDevice_name());
                        }
                        break;
                    case LOADING:
                        break;
                    case ERROR:
                        break;
                }
            }
        });

    }

    @Override
    protected void initListener() {
        btnCamera.setOnClickListener(v -> {
            startActivity(new Intent(this, CameraSettingsActivity.class));
        });

        btnDelay.setOnClickListener(v -> {
            startActivity(new Intent(this, DelaySettingActivity.class));
        });

        btnFace.setOnClickListener(v -> {
            startActivity(new Intent(this, UserActivity.class));
        });

        btnFaceConfig.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceConfigActivity.class));
        });

        btnTest.setOnClickListener(v -> {
            startActivity(new Intent(this, MFaceActivity.class));
        });

        btnBDSetting.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));
        });
    }
}
