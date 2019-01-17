package com.baidu.idl.sample.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.idl.sample.R;

/**
 * 进度条对话框
 * Created by v_liujialu01 on 2018/12/5.
 */

public class ProgressDialog extends Dialog {
    private TextView mTextProgress;
    private ProgressBar mProgressBar;

    public ProgressDialog(@NonNull Context context) {
        super(context, R.style.ImportDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.dialog_progress, null);
        setContentView(view);
        mTextProgress = findViewById(R.id.text_progress);
        mProgressBar = findViewById(R.id.progress_bar);
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(float progress) {
        mTextProgress.setText((int) (progress * 100) + "%");
        mProgressBar.setProgress((int) (progress * 100));
    }
}
