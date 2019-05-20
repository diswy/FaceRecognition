package com.yibaiqi.face.recognition.ui.core;

import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.sample.MyConfig;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.Key;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.di.DaggerActivityComponent;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;

import javax.inject.Inject;

public class FaceConfigActivity extends BaseActivity {

    private EditText etMin, etMax, etTrack, etDetect, etNoFace, etPitch, etYaw, etRoll;
    private CheckBox cbBlur, cbIllumination, cbOcclusion;
    private TextView btnCommit;

    @Inject
    ACache cache;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_face_config;
    }

    @Override
    public void initView() {
        etMin = findViewById(R.id.et_min);
        etMax = findViewById(R.id.et_max);
        etTrack = findViewById(R.id.et_trackInterval);
        etDetect = findViewById(R.id.et_detectInterval);
        etNoFace = findViewById(R.id.et_noFaceSize);
        etPitch = findViewById(R.id.et_pitch);
        etYaw = findViewById(R.id.et_yaw);
        etRoll = findViewById(R.id.et_roll);

        cbBlur = findViewById(R.id.cb_isCheckBlur);
        cbIllumination = findViewById(R.id.cb_isIllumination);
        cbOcclusion = findViewById(R.id.cb_isOcclusion);

        btnCommit = findViewById(R.id.btn_commit);
    }

    @Override
    public void initialize() {
        DaggerActivityComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .build()
                .inject(this);
    }

    @Override
    protected void initListener() {
        btnCommit.setOnClickListener(v -> {
            MyConfig myConfig = new MyConfig();
            try {
                if (!TextUtils.isEmpty(etMin.getText().toString().trim())) {
                    myConfig.setMinFace(Integer.parseInt(etMin.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(etMax.getText().toString().trim())) {
                    myConfig.setMaxFaceSize(Integer.parseInt(etMax.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(etTrack.getText().toString().trim())) {
                    myConfig.setTrackInterval(Integer.parseInt(etTrack.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(etDetect.getText().toString().trim())) {
                    myConfig.setDetectInterval(Integer.parseInt(etDetect.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(etNoFace.getText().toString().trim())) {
                    myConfig.setNoFaceSize(Float.parseFloat(etNoFace.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(etPitch.getText().toString().trim())) {
                    myConfig.setPitch(Integer.parseInt(etPitch.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(etYaw.getText().toString().trim())) {
                    myConfig.setYaw(Integer.parseInt(etYaw.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (!TextUtils.isEmpty(etRoll.getText().toString().trim())) {
                    myConfig.setRoll(Integer.parseInt(etRoll.getText().toString().trim()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            myConfig.setCheckBlur(cbBlur.isChecked());
            myConfig.setIllumination(cbIllumination.isChecked());
            myConfig.setOcclusion(cbOcclusion.isChecked());

            cache.put(Key.SETTING_BD_FACE, myConfig);
            Toast.makeText(FaceConfigActivity.this, "设置成功，请退出应用，重启后生效", Toast.LENGTH_SHORT).show();
        });
    }
}
