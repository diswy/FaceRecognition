package com.yibaiqi.face.recognition.ui.core;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

    private EditText etDelay, etDelayFace, etDelayRegister;
    private TextView btnCommit;
    private RadioGroup mOpenTypeRg;
    private RadioButton mType1, mType2;

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
        etDelayRegister = findViewById(R.id.et_delay_register);
        btnCommit = findViewById(R.id.btn_commit);
        mOpenTypeRg = findViewById(R.id.open_type_rg);
        mType1 = findViewById(R.id.open_type_1);
        mType2 = findViewById(R.id.open_type_2);
    }

    @Override
    public void initialize() {
        DaggerActivityComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .build()
                .inject(this);

        String sDelay = cache.getAsString(Key.KEY_DELAY);
        String sDelay2 = cache.getAsString(Key.KEY_DELAY_FACE);
        String sDelay3 = cache.getAsString(Key.KEY_DELAY_REGISTER);
        if (sDelay != null) {
            etDelay.setText(sDelay);
        }

        if (sDelay2 != null) {
            etDelayFace.setText(sDelay2);
        }

        if (sDelay3 != null) {
            etDelayRegister.setText(sDelay3);
        }
        String openType = cache.getAsString(Key.KEY_OPEN_TYPE);
        if (openType == null || openType.equals("type_1")) {
            mType1.setChecked(true);
        } else if (openType.equals("type_2")) {
            mType2.setChecked(true);
        }
    }

    @Override
    protected void initListener() {
        mOpenTypeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.open_type_1) {
                    cache.put(Key.KEY_OPEN_TYPE, "type_1");
                } else if (checkedId == R.id.open_type_2) {
                    cache.put(Key.KEY_OPEN_TYPE, "type_2");
                }
            }
        });


        btnCommit.setOnClickListener(v -> {
            try {
                String etS = etDelay.getText().toString().trim();
                int delay = Integer.parseInt(etS);
                cache.put(Key.KEY_DELAY, etS);

                String etS2 = etDelayFace.getText().toString().trim();
                int delay2 = Integer.parseInt(etS2);
                cache.put(Key.KEY_DELAY_FACE, etS2);

                String etS3 = etDelayRegister.getText().toString().trim();
                int delay3 = Integer.parseInt(etS3);
                cache.put(Key.KEY_DELAY_REGISTER, etS3);


                Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();

                finish();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "设置失败，请检查输入", Toast.LENGTH_SHORT).show();
            }

        });
    }
}
