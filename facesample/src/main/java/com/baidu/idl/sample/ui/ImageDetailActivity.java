package com.baidu.idl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.utils.FileUtils;
import com.bumptech.glide.Glide;

/**
 * 图片详情页
 * Created by v_liujialu01 on 2018/12/17.
 */

public class ImageDetailActivity extends BaseActivity {
    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        mImageView = findViewById(R.id.image_detail);
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String picName = intent.getStringExtra("pic_name");
            String imgPath = FileUtils.getFacePicDirectory().getAbsolutePath()
                    + "/" + picName;
            Glide.with(this).load(imgPath).into(mImageView);
        }
    }
}
