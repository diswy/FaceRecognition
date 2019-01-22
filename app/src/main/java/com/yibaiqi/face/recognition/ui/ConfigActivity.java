package com.yibaiqi.face.recognition.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.viewmodel.UserViewModel;


public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);


        UserViewModel model = ViewModelProviders.of(this,App.getInstance().factory)
                .get(UserViewModel.class);
        System.out.println("------>>>SplashActivity"+model);
        model.say();

//        DaggerActivityComponent.builder()
//                .appComponent(App.getInstance().getAppComponent())
//                .build()
//                .inject(this);

    }
}
