package com.yibaiqi.face.recognition.ui.core;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.idl.sample.ui.MainActivity;
import com.seeku.android.Manager;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.db.AppDatabase;
import com.yibaiqi.face.recognition.di.DaggerActivityComponent;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.tools.EBQValue;
import com.yibaiqi.face.recognition.ui.SynthActivity;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.FaceViewModel;
import com.yibaiqi.face.recognition.viewmodel.RongViewModel;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.ExData;
import com.yibaiqi.face.recognition.vo.OSSConfig;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.rong.imlib.RongIMClient;

public class SplashActivity extends BaseActivity {

    @Inject
    AppDatabase database;
    @Inject
    AppExecutors appExecutors;
    @Inject
    ACache cache;

    private EditText et;
    private int pos = 0;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    public void initView() {
        et = findViewById(R.id.et_user_id);
    }

    @Override
    public void initialize() {
        DaggerActivityComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .build()
                .inject(this);

        initFaceEngine();

        findViewById(R.id.btnOn).setOnClickListener(v -> new Manager(getApplicationContext()).setGateIO(true));
        findViewById(R.id.btnOff).setOnClickListener(v -> new Manager(getApplicationContext()).setGateIO(false));

        findViewById(R.id.speak).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, SynthActivity.class));
        });
        findViewById(R.id.jump_main).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        });

        //-------------------融云
//        RongViewModel serverViewModel = ViewModelProviders.of(this)
//                .get(RongViewModel.class);
//        serverViewModel.getConnectStatus().observe(this, isConnect -> {
//            System.out.println("--->>>>>>" + isConnect);
//        });
//        serverViewModel.getRongMessage().observe(this, msg -> {
//            Toast.makeText(this, msg.toString(), Toast.LENGTH_LONG).show();
//        });
//
//        //user1
//        serverViewModel.connect("SVpWxysVtwqdsqaS5sXcjsoxYNNn6TblrJ/u3gGjc1wH8B0+muXSTIkLrAb5gIHNqXVoOD7LiznX2ypobFQZ9g==");
//        //user2
////        serverViewModel.connect("p4o/K1eHyS5DDWf3r16nCHQQxjv13DQkqZKYBm2cHzMC9g+G9YHutgyqJu0bBlWGGBj7gwF73fg=");
//
////        serverViewModel.registerMessage();
//
//        RongIMClient.setOnReceiveMessageListener((message, left) -> {
////            rongMessage.setValue(message);
//            System.out.println("--->>>>>>消息：" + message);
//            System.out.println("--->>>>>>未拉取：" + left);
//            return false;
//        });

//        serverViewModel.sendTestMsg();
    }

    //-----------------------------------------------------
    private void initFaceEngine() {
        FaceViewModel faceModel = ViewModelProviders.of(this, App.getInstance().factory).get(FaceViewModel.class);
//        faceModel.initBDFaceEngine("QY8C-NXN5-9XH7-8VCC");// 测试写死

        faceModel.registerDevice().observe(this, resource -> {
            if (resource == null)
                return;
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null && resource.data.getData() != null) {
                        cache.put("token", resource.data.getData().getToken());
                        cache.put("im_token", resource.data.getData().getImToken());
                        System.out.println("----->>>token=" + resource.data.getData().getToken());

                        OSSConfig ossConfig = resource.data.getData().getOssConfig();
                        if (ossConfig != null) {
                            cache.put("oss_config", ossConfig);
                        }

                        ExData exData = resource.data.getData().getData();
                        if (exData != null && exData.getUsers() != null) {
                            List<DbOption> list = new ArrayList<>();

                            if (exData.getUsers().getAdd() != null) {

                                for (DbOption item : exData.getUsers().getAdd()) {
                                    if (!TextUtils.isEmpty(item.getFace_image())){
                                        DbOption mData = new DbOption(item.getUser_key(), item.getReal_name(), item.getFace_image(), 0);
                                        list.add(mData);
                                    }
                                }

                            }

                            if (exData.getUsers().getDelete() != null) {
                                for (DbOption item : exData.getUsers().getDelete()) {
                                    if (!TextUtils.isEmpty(item.getFace_image())){
                                        DbOption mData = new DbOption(item.getUser_key(), item.getReal_name(), "", 1);
                                        list.add(mData);
                                    }
                                }
                            }
                            faceModel.insert(list);
                        }

//                        faceModel.initBDFaceEngine("QY8C-NXN5-9XH7-8VCC");// 测试写死
                        faceModel.initBDFaceEngine(resource.data.getData().getSerialNumber());// 测试写死
                    }
                    break;
                case ERROR:
                    break;
                case LOADING:
                    break;
            }
        });

        faceModel.getInitStatus().observe(this, status -> {
            if (status != null && status) {
                faceModel.bindDevice();
                startActivity(new Intent(SplashActivity.this, CMainActivity.class));
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
//                this.finish();
            }
        });

    }

}
