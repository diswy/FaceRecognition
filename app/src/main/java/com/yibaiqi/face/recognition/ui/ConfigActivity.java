package com.yibaiqi.face.recognition.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.viewmodel.UserViewModel;


public class ConfigActivity extends AppCompatActivity {

//    @Inject
//    People a;
//    @Inject
//    People b;
//    @Inject
//    People c;

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

//        System.out.println("--->>>ConfigActivity a:"+a);
//        System.out.println("--->>>ConfigActivity b:" + b);
//        System.out.println("--->>>ConfigActivity c:" + c);
    }
}
