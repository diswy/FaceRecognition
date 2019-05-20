package com.yibaiqi.face.recognition.ui.core;

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

public class DelaySettingActivity extends BaseActivity {

    private EditText etDelay, etDelayFace;
    private TextView btnCommit;

    @Inject
    ACache cache;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_delay_setting;
    }

    @Override
    public void initView() {
        etDelay = findViewById(R.id.et_delay);
        etDelayFace = findViewById(R.id.et_delay_face);
        btnCommit = findViewById(R.id.btn_commit);
    }

    @Override
    public void initialize() {
        DaggerActivityComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .build()
                .inject(this);

        String sDelay = cache.getAsString(Key.KEY_DELAY);
        String sDelay2 = cache.getAsString(Key.KEY_DELAY_FACE);
        if (sDelay != null) {
            etDelay.setText(sDelay);
        }

        if (sDelay2 != null) {
            etDelayFace.setText(sDelay2);
        }
    }

    @Override
    protected void initListener() {
        btnCommit.setOnClickListener(v -> {
            try {
                String etS = etDelay.getText().toString().trim();
                int delay = Integer.parseInt(etS);
                cache.put(Key.KEY_DELAY, etS);

                String etS2 = etDelayFace.getText().toString().trim();
                int delay2 = Integer.parseInt(etS);
                cache.put(Key.KEY_DELAY_FACE, etS2);
                Toast.makeText(this, "闸机延迟和识别间隔设置成功", Toast.LENGTH_SHORT).show();

                finish();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "设置失败，请检查输入", Toast.LENGTH_SHORT).show();
            }

        });
    }
}
