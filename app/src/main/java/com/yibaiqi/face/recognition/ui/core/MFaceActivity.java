package com.yibaiqi.face.recognition.ui.core;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;

import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ONE_HUNDERED_EIGHTY_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ZERO_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_TPREVIEW_NINETY_ANGLE;

public class MFaceActivity extends BaseActivity {

    private RadioGroup rg;
    private RadioButton rb0, rb90, rb180, rb270;
    private TextView btn;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_mface;
    }

    @Override
    public void initView() {
        rg = findViewById(R.id.btn_rg);
        rb0 = findViewById(R.id.btn_rb0);
        rb90 = findViewById(R.id.btn_rb90);
        rb180 = findViewById(R.id.btn_rb180);
        rb270 = findViewById(R.id.btn_rb270);
        btn = findViewById(R.id.btn_commit);
    }

    @Override
    public void initialize() {
        int previewAngle = PreferencesUtil.getInt(TYPE_PREVIEW_ANGLE, TYPE_TPREVIEW_NINETY_ANGLE);
        defaultPreviewAngle(previewAngle);
    }

    private void defaultPreviewAngle(int angle) {
        if (angle == TYPE_PREVIEW_ZERO_ANGLE) {
            rb0.setChecked(true);
        } else if (angle == TYPE_TPREVIEW_NINETY_ANGLE) {
            rb90.setChecked(true);
        } else if (angle == TYPE_PREVIEW_ONE_HUNDERED_EIGHTY_ANGLE) {
            rb180.setChecked(true);
        } else if (angle == TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE) {
            rb270.setChecked(true);
        }
    }

    @Override
    protected void initListener() {

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_rb0:
                        PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_PREVIEW_ZERO_ANGLE);
                        break;
                    case R.id.btn_rb90:
                        PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_TPREVIEW_NINETY_ANGLE);
                        break;
                    case R.id.btn_rb180:
                        PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_PREVIEW_ONE_HUNDERED_EIGHTY_ANGLE);
                        break;
                    case R.id.btn_rb270:
                        PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE);
                        break;
                }
            }
        });
    }
}
