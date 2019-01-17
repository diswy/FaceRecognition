package com.baidu.idl.sample.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;

import com.baidu.idl.sample.R;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.listener.OnDBFileListener;
import com.baidu.idl.sample.manager.DBFileManager;
import com.baidu.idl.sample.utils.ToastUtils;

/**
 * 导入/导出数据库
 * Created by v_liujialu01 on 2018/12/18.
 */

public class DBImportActivity extends BaseActivity implements
        View.OnClickListener, OnDBFileListener{
    private static final String TAG = "DBImportActivity";

    private DBFileManager mDBFileManager;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_import);
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.db_import);
        mContext = this;
        mDBFileManager = DBFileManager.getInstance();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mContext != null) {
            mContext = null;
        }

        if (mDBFileManager != null) {
            mDBFileManager = null;
        }
    }

    private void initListener() {
        mDBFileManager.setOnDBFileListener(this);
        findViewById(R.id.button_import).setOnClickListener(this);
        findViewById(R.id.button_output).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // 导入数据库
        if (view.getId() == R.id.button_import){
            mDBFileManager.copyDBFileToData();
        }

        // 导出数据库
        if (view.getId() == R.id.button_output) {
            mDBFileManager.copyDBFileToSDCard();
        }
    }

    @Override
    public void importDBSuccess() {
        ToastUtils.toast(mContext, "导入成功");
    }

    @Override
    public void outputDBSuccess() {
        ToastUtils.toast(mContext, "导出成功");
    }

    @Override
    public void showErrMsg() {
        ToastUtils.toast(mContext, "导入/出失败，可能是原目录下没有文件");
    }

    /**
     * 返回键
     * @param view
     */
    public void onBackClick(View view) {
        setResult(GlobalSet.DB_RESULT_CODE);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            setResult(GlobalSet.DB_RESULT_CODE);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
