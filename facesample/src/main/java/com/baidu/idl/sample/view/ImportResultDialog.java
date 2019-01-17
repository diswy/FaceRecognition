package com.baidu.idl.sample.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.baidu.idl.sample.R;

/**
 * 导入结果弹窗
 * Created by v_liujialu01 on 2018/12/6.
 */

public class ImportResultDialog extends Dialog {
    private TextView mTextResult;

    public ImportResultDialog(@NonNull Context context) {
        super(context, R.style.ImportDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.dialog_result, null);
        setContentView(view);
        mTextResult = findViewById(R.id.text_result);
    }

    public void setResult(int totalCount, int successCount, int failureCount) {
        mTextResult.setText("任务总数：" + totalCount
                + "\n导入成功：" + successCount
                + "\n导入失败：" + failureCount);
    }
}
