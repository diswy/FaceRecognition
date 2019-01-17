package com.baidu.idl.sample.ui;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.view.CustomRadioGroup;

import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ONE_HUNDERED_EIGHTY_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ZERO_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_TPREVIEW_NINETY_ANGLE;

/**
 * @Time: 2018/12/4
 * @Author: v_chaixiaogang
 * @Description: 摄像头预览角度设置页面
 */
public class SettingCameraPreviewAngleActivity extends BaseActivity implements View.OnClickListener ,View.OnTouchListener {

    private CustomRadioGroup rgPreviewAngle;
    private RadioButton rbZeroAngle;
    private RadioButton rbNinetyAngle;
    private RadioButton rbOneHundredEightyAngle;
    private RadioButton rbTwoHundredSeventyAngle;
    private Button btConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview_angle);
        initView();
        int previewAngle = PreferencesUtil.getInt(TYPE_PREVIEW_ANGLE, TYPE_TPREVIEW_NINETY_ANGLE);
        defaultPreviewAngle(previewAngle);
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.setting_camera_preview);
        rgPreviewAngle = findViewById(R.id.rg_preview_angle);
        rbZeroAngle = findViewById(R.id.rb_preview_zero_angle);
        rbNinetyAngle = findViewById(R.id.rb_preview_nine_angle);
        rbOneHundredEightyAngle = findViewById(R.id.rb_preview_one_hundred_eighty);
        rbTwoHundredSeventyAngle = findViewById(R.id.rb_preview_two_hundred_seventy);
        btConfirm = findViewById(R.id.confirm_btn);
        btConfirm.setOnClickListener(this);
        btConfirm.setOnTouchListener(this);
        rgPreviewAngle.setOnCheckedChangeListener(new CustomRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomRadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_preview_zero_angle) {
                    PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_PREVIEW_ZERO_ANGLE);

                } else if (checkedId == R.id.rb_preview_nine_angle) {
                    PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_TPREVIEW_NINETY_ANGLE);

                } else if (checkedId == R.id.rb_preview_one_hundred_eighty) {
                    PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_PREVIEW_ONE_HUNDERED_EIGHTY_ANGLE);

                } else if (checkedId == R.id.rb_preview_two_hundred_seventy) {
                    PreferencesUtil.putInt(TYPE_PREVIEW_ANGLE, TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE);
                }
            }
        });
    }

    private void defaultPreviewAngle(int angle) {
        if (angle == TYPE_PREVIEW_ZERO_ANGLE) {
            rbZeroAngle.setChecked(true);
        } else if (angle == TYPE_TPREVIEW_NINETY_ANGLE) {
            rbNinetyAngle.setChecked(true);
        } else if (angle == TYPE_PREVIEW_ONE_HUNDERED_EIGHTY_ANGLE) {
            rbOneHundredEightyAngle.setChecked(true);
        } else if (angle == TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE) {
            rbTwoHundredSeventyAngle.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.confirm_btn) {
            finish();

        } else {
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (view.getId() == R.id.confirm_btn) {
                    ((Button) view).setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (view.getId() == R.id.confirm_btn) {
                    ((Button) view).setTextColor(getResources().getColor(R.color.btnColor));
                }
                break;
        }
        return false;
    }
}
