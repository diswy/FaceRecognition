package com.baidu.idl.sample.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.callback.IFaceRegistCalllBack;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.manager.FaceLiveness;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.ToastUtils;
import com.baidu.idl.sample.utils.Utils;
import com.baidu.idl.sample.view.BinocularView;
import com.baidu.idl.sample.view.MonocularView;

/**
 * Created by litonghui on 2018/11/17.
 */

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private View mLayoutInput;
    private EditText mNickView;

    private RelativeLayout mCameraView;
    private BinocularView mBinocularView;
    private MonocularView mMonocularView;
    private View registResultView;
    private TextView mTextBatchRegister;
    private Context mContext;
    private String mNickName;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            faceRegistCalllBack.onRegistCallBack(1, null, null);
        }
    };
    // 注册结果
    private IFaceRegistCalllBack faceRegistCalllBack = new IFaceRegistCalllBack() {

        @Override
        public void onRegistCallBack(int code, LivenessModel livenessModel, final Bitmap cropBitmap) {
            handler.removeCallbacks(runnable);
            // 停止摄像头采集
            registResultView.post(new Runnable() {
                @Override
                public void run() {
                    mLayoutInput.setVisibility(View.GONE);
                    mTextBatchRegister.setVisibility(View.GONE);
                    if (mBinocularView != null) {
                        mBinocularView.onPause();
                        mCameraView.removeView(mBinocularView);
                    }
                    if (mMonocularView != null) {
                        mMonocularView.onPause();
                        mCameraView.removeView(mMonocularView);
                    }
                    registResultView.setVisibility(View.VISIBLE);
                }
            });
            switch (code) {
                case 0: {
                    // 设置注册信息
                    registResultView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((ImageView) registResultView.findViewById(R.id.ic_right))
                                    .setBackground(getDrawable(R.mipmap.ic_success));
                            if (cropBitmap != null) {
                                ((ImageView) registResultView.findViewById(R.id.ic_portrait))
                                        .setImageBitmap(cropBitmap);
                            }
                            ((TextView) registResultView.findViewById(R.id.nick_name))
                                    .setText(mNickName);
                            ((TextView) registResultView.findViewById(R.id.result))
                                    .setVisibility(View.VISIBLE);
                            ((TextView) registResultView.findViewById(R.id.complete))
                                    .setText("确定");
                        }
                    });
                }
                    break;
                case 1: {
                    registResultView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((ImageView) registResultView.findViewById(R.id.ic_right))
                                    .setVisibility(View.INVISIBLE);
                            ((ImageView) registResultView.findViewById(R.id.ic_portrait))
                                    .setBackground(getDrawable(R.mipmap.ic_track));
                            ((TextView) registResultView.findViewById(R.id.nick_name))
                                    .setText("注册超时");
                            ((TextView) registResultView.findViewById(R.id.result))
                                    .setVisibility(View.GONE);
                            ((TextView) registResultView.findViewById(R.id.complete))
                                    .setText("确定");
                            registResultView.setVisibility(View.VISIBLE);
                        }
                    });
                }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = this;
        initView();
        setAction();
        FaceSDKManager.getInstance().getFaceLiveness()
                .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_REGIST);
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.face_regiseter);
        mLayoutInput = findViewById(R.id.layout_input);
        findViewById(R.id.go_btn).setOnClickListener(this);
        mNickView = findViewById(R.id.nick_name);

        mCameraView = findViewById(R.id.layout_camera);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView = new BinocularView(mContext);
            mCameraView.addView(mBinocularView, lp);
        } else {
            mMonocularView = new MonocularView(mContext);
            mCameraView.addView(mMonocularView, lp);
        }

        registResultView = findViewById(R.id.regist_result);
        mTextBatchRegister = findViewById(R.id.text_batch_register);
        mTextBatchRegister.setOnClickListener(this);
    }

    private void setAction() {
        // 注册人脸注册事件
        FaceSDKManager.getInstance().getFaceLiveness().addRegistCallBack(faceRegistCalllBack);

        // 设置完成事件
        registResultView.findViewById(R.id.complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });

        handler.postDelayed(runnable, 1000 * 30);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onResume();
        } else {
            mMonocularView.onResume();
        }
    }

    @Override
    protected void onStop() {
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onPause();
        } else {
            mMonocularView.onPause();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceSDKManager.getInstance().getFaceLiveness().removeRegistCallBack(faceRegistCalllBack);
        // 重置状态为默认状态
        FaceSDKManager.getInstance().getFaceLiveness()
                .setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_ONETON);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.go_btn) {
            Editable editable = mNickView.getText();
            if (editable != null && editable.length() > 0) {
                mNickName = mNickView.getText().toString();
                String nameResult = FaceApi.getInstance().isValidName(mNickName);
                if ("0".equals(nameResult)) {
                    // 设置注册时的昵称
                    FaceSDKManager.getInstance().getFaceLiveness().setRegistNickName(mNickName);
                    Utils.hideKeyboard((Activity) mContext);

                    mLayoutInput.setVisibility(View.GONE);
                    mCameraView.setVisibility(View.VISIBLE);
                } else {
                    ToastUtils.toast(mContext, nameResult);
                }
            }
        }

        if (id == R.id.text_batch_register) {    // 批量注册
            Intent intent = new Intent(mContext, BatchImportActivity.class);
            startActivity(intent);
        }
    }
}