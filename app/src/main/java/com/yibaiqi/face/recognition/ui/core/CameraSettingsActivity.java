package com.yibaiqi.face.recognition.ui.core;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.Key;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.di.DaggerActivityComponent;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;

import javax.inject.Inject;

public class CameraSettingsActivity extends BaseActivity {

    private EditText etIp;
    private EditText etPort;
    private EditText etAccount;
    private EditText etPwd;
    private TextView btnCommit;

    @Inject
    ACache cache;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_camera_settings;
    }

    @Override
    public void initView() {
        DaggerActivityComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .build()
                .inject(this);


        etIp = findViewById(R.id.et_ip);
        etPort = findViewById(R.id.et_port);
        etAccount = findViewById(R.id.et_account);
        etPwd = findViewById(R.id.et_pwd);
        btnCommit = findViewById(R.id.btn_commit);
    }

    @Override
    public void initialize() {
        String ip = cache.getAsString(Key.KEY_CAMERA_IP);
        String port = cache.getAsString(Key.KEY_CAMERA_IP);
        String account = cache.getAsString(Key.KEY_CAMERA_IP);
        String pwd = cache.getAsString(Key.KEY_CAMERA_IP);

        if (!TextUtils.isEmpty(ip)) {
            etIp.setText(cache.getAsString(Key.KEY_CAMERA_IP));
        }

        if (!TextUtils.isEmpty(port)) {
            etPort.setText(cache.getAsString(Key.KEY_CAMERA_PORT));
        }

        if (!TextUtils.isEmpty(account)) {
            etAccount.setText(cache.getAsString(Key.KEY_CAMERA_ACCOUNT));
        }

        if (!TextUtils.isEmpty(pwd)) {
            etPwd.setText(cache.getAsString(Key.KEY_CAMERA_PWD));
        }
    }

    @Override
    protected void initListener() {
        btnCommit.setOnClickListener(v -> {
            if (check()) {
                cache.put(Key.KEY_CAMERA_IP, etIp.getText().toString().trim());
                cache.put(Key.KEY_CAMERA_ACCOUNT, etAccount.getText().toString().trim());
                cache.put(Key.KEY_CAMERA_PORT, etPort.getText().toString().trim());
                cache.put(Key.KEY_CAMERA_PWD, etPwd.getText().toString().trim());
            }
        });
    }

    private boolean check() {
        if (TextUtils.isEmpty(etIp.getText().toString())) {
            Toast.makeText(this, "请设置完每一项后，再进行保存", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etAccount.getText().toString())) {
            Toast.makeText(this, "请设置完每一项后，再进行保存", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etPort.getText().toString())) {
            Toast.makeText(this, "请设置完每一项后，再进行保存", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etPort.getText().toString())) {
            Toast.makeText(this, "请设置完每一项后，再进行保存", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
