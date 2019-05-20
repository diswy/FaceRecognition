package com.yibaiqi.face.recognition.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;
import com.yibaiqi.face.recognition.App;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends AppCompatActivity {

    protected Context mContext;
    protected final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().addActivity(this);

        mContext = this;
        setContentView(getLayoutRes());
        initView();
        initialize();
        initListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.clear();
    }

    public abstract int getLayoutRes();

    public abstract void initView();

    public abstract void initialize();

    protected void initListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
