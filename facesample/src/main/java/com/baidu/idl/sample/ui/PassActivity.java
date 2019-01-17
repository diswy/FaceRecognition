package com.baidu.idl.sample.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.baidu.idl.sample.R;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.Utils;
import com.baidu.idl.sample.view.BinocularView;
import com.baidu.idl.sample.view.CircleImageView;
import com.baidu.idl.sample.view.CirclePercentView;
import com.baidu.idl.sample.view.MonocularView;

import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_ANGLE;
import static com.baidu.idl.sample.common.GlobalSet.TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE;

/**
 * Created by litonghui on 2018/11/17.
 */

public class PassActivity extends BaseActivity implements ILivenessCallBack, View.OnClickListener {

    private Context mContext;

    private RelativeLayout mCameraView;
    private BinocularView mBinocularView;
    private MonocularView mMonocularView;

    private CircleImageView mImage;
    private TextView mNickNameTv;
    private TextView mSimilariryTv;
    private TextView mNumTv;
    private TextView mDetectTv;
    private TextView mFeatureTv;
    private TextView mLiveTv;
    private TextView mAllTv;

    private Bitmap mBitmap;
    private String mUserName;

    private CirclePercentView mRgbCircleView;
    private CirclePercentView mNirCircleView;
    private CirclePercentView mDepthCircleView;

    private RelativeLayout mLayoutInfo;
    private LinearLayout mLinearTime;
    private LinearLayout mLinearUp;
    private ImageView mImageTrack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);
        mContext = this;
        initView();
        initData();
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.pass_1_n);
        mCameraView = findViewById(R.id.layout_camera);
        mImageTrack = findViewById(R.id.image_track);
        // 计算并适配显示图像容器的宽高
        calculateCameraView();
        mImage = findViewById(R.id.image);
        mNickNameTv = findViewById(R.id.tv_nick_name);
        mSimilariryTv = findViewById(R.id.tv_similarity);
        mNumTv = findViewById(R.id.tv_num);
        mDetectTv = findViewById(R.id.tv_detect);
        mFeatureTv = findViewById(R.id.tv_feature);
        mLiveTv = findViewById(R.id.tv_live);
        mAllTv = findViewById(R.id.tv_all);

        mRgbCircleView = findViewById(R.id.circle_rgb_live);
        mNirCircleView = findViewById(R.id.circle_nir_live);
        mDepthCircleView = findViewById(R.id.circle_depth_live);

        mLayoutInfo = findViewById(R.id.layout_info);
        mLinearTime = findViewById(R.id.linear_time);
        mLinearUp = findViewById(R.id.linear_up);
        RelativeLayout relativeDown = findViewById(R.id.relative_down);
        RelativeLayout relativeUp = findViewById(R.id.relative_up);
        relativeDown.setOnClickListener(this);
        relativeUp.setOnClickListener(this);
    }

    /**
     * 计算并适配显示图像容器的宽高
     */
    private void calculateCameraView() {
        String newPix;
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            newPix = DensityUtil.calculateCameraOrbView(mContext);
        } else {
            newPix = DensityUtil.calculateCameraView(mContext);
        }
        String[] newPixs = newPix.split(" ");
        int newWidth = Integer.parseInt(newPixs[0]);
        int newHeight = Integer.parseInt(newPixs[1]);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(newWidth, newHeight);

        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView = new BinocularView(mContext);
            mBinocularView.setImageView(mImageTrack);
            mBinocularView.setLivenessCallBack(this);
            mCameraView.addView(mBinocularView, layoutParams);
        } else {
            mMonocularView = new MonocularView(mContext);
            mMonocularView.setImageView(mImageTrack);
            mMonocularView.setLivenessCallBack(this);
            mCameraView.addView(mMonocularView, layoutParams);
        }
    }

    private void initData() {
        int num = FaceSDKManager.getInstance().setFeature();
        mNumTv.setText(String.format("底库人脸数: %s 个", num));
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
    public void onTip(int code, String msg) {

    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

    @Override
    public void onCallback(final int code, final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectTv.setText(String.format("人脸检测耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getRgbDetectDuration()));
                mFeatureTv.setText(String.format("特征提取耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getFeatureDuration()));
                mLiveTv.setText(String.format("活体检测耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getLiveDuration()));
                mAllTv.setText(String.format("1:N人脸检索耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getCheckDuration()));

                mRgbCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getRgbLivenessScore());

                mNirCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getIrLivenessScore());

                mDepthCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getDepthLivenessScore());

                if (livenessModel == null) {
                    mLayoutInfo.setVisibility(View.INVISIBLE);

                } else {
                    mLayoutInfo.setVisibility(View.VISIBLE);
                    if (code == 0) {
                        Feature feature = livenessModel.getFeature();
                        mSimilariryTv.setText(String.format("相似度: %s", livenessModel.getFeatureScore()));
                        mNickNameTv.setText(String.format("%s，你好!", feature.getUserName()));

                        if (!TextUtils.isEmpty(mUserName) && feature.getUserName().equals(mUserName)) {
                            mImage.setImageBitmap(mBitmap);
                        } else {
                            String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                                    + "/" + feature.getCropImageName();
                            Bitmap bitmap = Utils.getBitmapFromFile(imgPath);
                            mImage.setImageBitmap(bitmap);
                            mBitmap = bitmap;
                            mUserName = feature.getUserName();
                        }
                    } else {
                        mSimilariryTv.setText("未匹配到相似人脸");
                        mNickNameTv.setText("陌生访客");
                        mImage.setImageResource(R.mipmap.preview_image_angle);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.relative_down) {
            mLinearTime.setVisibility(View.GONE);
            mLinearUp.setVisibility(View.VISIBLE);
        }

        if (view.getId() == R.id.relative_up) {
            mLinearTime.setVisibility(View.VISIBLE);
            mLinearUp.setVisibility(View.GONE);
        }
    }
}
