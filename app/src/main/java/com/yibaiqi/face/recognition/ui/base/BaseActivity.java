package com.yibaiqi.face.recognition.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends AppCompatActivity {

    protected final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        initView();
        initialize();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.clear();
    }

    public abstract int getLayoutRes();

    public abstract void initView();

    public abstract void initialize();

}