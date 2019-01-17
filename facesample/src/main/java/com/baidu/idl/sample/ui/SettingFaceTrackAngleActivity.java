package com.baidu.idl.sample.ui;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.view.CustomRadioGroup;

import static com.baidu.idl.sample.common.GlobalSet.TYPE_TRACK_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_TRACK_NINETY_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_TRACK_ONE_HUNDERED_EIGHTY_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_TRACK_TWO_HUNDERED_SEVENTY_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_TRACK_ZERO_ANGLE;


/**
 * @Time: 2018/12/4
 * @Author: v_chaixiaogang
 * @Description: 人脸检测角度设置页面
 */
public class SettingFaceTrackAngleActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener {

    private CustomRadioGroup rgTrackAngle;
    private RadioButton rbZeroAngle;
    private RadioButton rbNinetyAngle;
    private RadioButton rbOneHundredEightyAngle;
    private RadioButton rbTwoHundredSeventyAngle;
    private Button btConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_track_angle_set);
        initView();
        int trackAngle = PreferencesUtil.getInt(TYPE_TRACK_ANGLE, TYPE_TRACK_ZERO_ANGLE);
        defaultTrackAngle(trackAngle);
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.setting_track_angle);
        rgTrackAngle = findViewById(R.id.rg_track_angle);
        rbZeroAngle = findViewById(R.id.rb_track_zero_angle);
        rbNinetyAngle = findViewById(R.id.rb_track_nine_angle);
        rbOneHundredEightyAngle = findViewById(R.id.rb_track_one_hundred_eighty);
        rbTwoHundredSeventyAngle = findViewById(R.id.rb_track_two_hundred_seventy);
        btConfirm = findViewById(R.id.confirm_btn);
        btConfirm.setOnClickListener(this);
        btConfirm.setOnTouchListener(this);
        rgTrackAngle.setOnCheckedChangeListener(new CustomRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomRadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_track_zero_angle) {
                    PreferencesUtil.putInt(TYPE_TRACK_ANGLE, TYPE_TRACK_ZERO_ANGLE);

                } else if (checkedId == R.id.rb_track_nine_angle) {
                    PreferencesUtil.putInt(TYPE_TRACK_ANGLE, TYPE_TRACK_NINETY_ANGLE);

                } else if (checkedId == R.id.rb_track_one_hundred_eighty) {
                    PreferencesUtil.putInt(TYPE_TRACK_ANGLE, TYPE_TRACK_ONE_HUNDERED_EIGHTY_ANGLE);

                } else if (checkedId == R.id.rb_track_two_hundred_seventy) {
                    PreferencesUtil.putInt(TYPE_TRACK_ANGLE, TYPE_TRACK_TWO_HUNDERED_SEVENTY_ANGLE);
                }
            }
        });
    }

    private void defaultTrackAngle(int angle) {
        if (angle == TYPE_TRACK_ZERO_ANGLE) {
            rbZeroAngle.setChecked(true);
        } else if (angle == TYPE_TRACK_NINETY_ANGLE) {
            rbNinetyAngle.setChecked(true);
        } else if (angle == TYPE_TRACK_ONE_HUNDERED_EIGHTY_ANGLE) {
            rbOneHundredEightyAngle.setChecked(true);
        } else if (angle == TYPE_TRACK_TWO_HUNDERED_SEVENTY_ANGLE) {
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
