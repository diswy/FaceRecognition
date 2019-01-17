package com.baidu.idl.sample.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.baidu.idl.sample.R;

/**
 * 说明对话框
 * Created by v_liujialu01 on 2018/12/21.
 */

public class MessageDialog extends Dialog {
    private TextView mTextTitle;
    private TextView mTextMessage;

    public MessageDialog(@NonNull Context context) {
        super(context, R.style.ImportDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.dialog_message, null);
        setContentView(view);
        mTextTitle = findViewById(R.id.text_dialog_title);
        mTextMessage = findViewById(R.id.text_dialog_message);
    }

    public void setData(int titleId, int messageId) {
        if (mTextTitle != null) {
            mTextTitle.setText(titleId);
        }

        if (mTextMessage != null) {
            mTextMessage.setText(messageId);
        }
    }
}
