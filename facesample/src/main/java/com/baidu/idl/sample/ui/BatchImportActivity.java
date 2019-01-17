package com.baidu.idl.sample.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.listener.OnImportListener;
import com.baidu.idl.sample.manager.ImportFileManager;
import com.baidu.idl.sample.utils.DensityUtil;
import com.baidu.idl.sample.utils.ToastUtils;
import com.baidu.idl.sample.view.ImportResultDialog;
import com.baidu.idl.sample.view.ProgressDialog;

/**
 * 批量导入界面
 * Created by litonghui on 2018/11/18.
 */

public class BatchImportActivity extends BaseActivity implements View.OnClickListener, OnImportListener,
        DialogInterface.OnDismissListener {
    private static final String TAG = BatchImportActivity.class.getSimpleName();

    private Button mButtonImport;

    private Context mContext;
    private volatile boolean mImporting;

    private ProgressDialog mProgressDialog;
    private ImportFileManager mImportFileManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_import);
        mContext = this;
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImportFileManager.release();
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.batch_import);
        mButtonImport = findViewById(R.id.button_import);
        DensityUtil.getAdaptationWH(127, 30, 400,
                mButtonImport, mContext);
        mButtonImport.setOnClickListener(this);
    }

    private void initData() {
        mImportFileManager = ImportFileManager.getInstance();
        mImportFileManager.setOnImportListener(this);
    }

    /**
     * 显示导入结果对话框
     * @param totalCount     总导入数量
     * @param successCount   成功导入数量
     * @param failureCount   失败导入数量
     */
    private void showResultDialog(int totalCount, int successCount, int failureCount) {
        ImportResultDialog dialog = new ImportResultDialog(mContext);
        dialog.setCancelable(true);
        dialog.show();
        dialog.setResult(totalCount, successCount, failureCount);
    }

    @Override
    public void onClick(View view) {
        // 点击“批量导入”按钮
        if (view == mButtonImport) {
            if (!mImporting) {
                mButtonImport.setText("停止导入");
                mImporting = true;
                mImportFileManager.batchImport();
            }
//            else {
//                mImporting = false;
//                mButtonImport.setText("开始导入");
//                mImportFileManager.setImport(false);
//                mImportFileManager.release();
//            }
        }
    }

    // 开始导入，用于初始化导入进度条
    @Override
    public void startImport() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnDismissListener(this);
        mProgressDialog.show();
    }

    // 正在导入，显示导入进度
    @Override
    public void onImporting(final float progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    return;
                }
                mProgressDialog.setProgress(progress);
            }
        });
    }

    // 结束导入，显示导入结果对话框
    @Override
    public void endImport(final int totalCount, final int successCount, final int failureCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mButtonImport == null || mProgressDialog == null) {
                    return;
                }
                mButtonImport.setText("开始导入");
                mImporting = false;
                mProgressDialog.dismiss();
                showResultDialog(totalCount, successCount, failureCount);
            }
        });
    }

    // 提示导入错误信息
    @Override
    public void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImporting = false;
                mButtonImport.setText("开始导入");
                mImportFileManager.release();
                ToastUtils.toast(mContext, message);
            }
        });
    }

    // 关闭进度对话框
    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        // 当进度对话框关闭之后关闭导入线程
        mImporting = false;
        mButtonImport.setText("开始导入");
        mImportFileManager.setImport(false);
        mImportFileManager.release();
    }

    /**
     * 返回键
     * @param view
     */
    public void onBackClick(View view) {
        setResult(GlobalSet.IMPORT_RESULT_CODE);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            setResult(GlobalSet.IMPORT_RESULT_CODE);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
