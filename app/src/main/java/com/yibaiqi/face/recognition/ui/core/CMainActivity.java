package com.yibaiqi.face.recognition.ui.core;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.callback.ILivenessCallBack;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.model.LivenessModel;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.Utils;
import com.baidu.idl.sample.view.BinocularView;
import com.baidu.idl.sample.view.MonocularView;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;

public class CMainActivity extends BaseActivity implements ILivenessCallBack {

    private FrameLayout mCameraView;
    private MonocularView mMonocularView;
    private ImageView iv;


    @Override
    public int getLayoutRes() {
        return R.layout.activity_cmain;
    }

    @Override
    public void initView() {
        mCameraView = findViewById(R.id.layout_camera);
        iv = findViewById(R.id.test_iv);

        findViewById(R.id.test_btn).setOnClickListener(v -> {
            Bitmap bitmap = screenShotView(mCameraView);
            iv.setImageBitmap(bitmap);

        });

    }

    @Override
    public void initialize() {
        calculateCameraView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMonocularView.onResume();

    }

    @Override
    protected void onStop() {
        mMonocularView.onPause();
        super.onStop();
    }


    /**
     * 计算并适配显示图像容器的宽高
     */
    private void calculateCameraView() {
        String newPix;
        newPix = DensityUtil.calculateCameraView(mContext);
        String[] newPixs = newPix.split(" ");
        int newWidth = Integer.parseInt(newPixs[0]);
        int newHeight = Integer.parseInt(newPixs[1]);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(newWidth, newHeight);
        mMonocularView = new MonocularView(mContext);
//        mMonocularView.setImageView(mImageTrack);
        mMonocularView.setLivenessCallBack(this);
        mCameraView.addView(mMonocularView, layoutParams);
    }

    @Override
    public void onTip(int code, String msg) {

    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

    @Override
    public void onCallback(int code, LivenessModel livenessModel) {
        System.out.println("-code= " + code);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (code == 0) {
                    Feature feature = livenessModel.getFeature();
//                    mSimilariryTv.setText(String.format("相似度: %s", livenessModel.getFeatureScore()));
//                    mNickNameTv.setText(String.format("%s，你好!", feature.getUserName()));

                    String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                            + "/" + feature.getCropImageName();
                    Bitmap bitmap = Utils.getBitmapFromFile(imgPath);

                    System.out.println("----姓名:" + feature.getUserName());
                    System.out.println("-add:" + imgPath);
                }

            }
        });
    }


    /**
     * 使用View的缓存功能，截取指定区域的View
     */
    private Bitmap screenShotView(View view) {
        //开启缓存功能
        view.setDrawingCacheEnabled(true);
        //创建缓存
        view.buildDrawingCache();
        //获取缓存Bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        return bitmap;
    }
}
