package com.yibaiqi.face.recognition.ui.core;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.idl.sample.ui.MainActivity;
import com.google.gson.Gson;
import com.seeku.android.Manager;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.db.AppDatabase;
import com.yibaiqi.face.recognition.di.DaggerActivityComponent;
import com.yibaiqi.face.recognition.entity.User;
import com.yibaiqi.face.recognition.ui.ConfigActivity;
import com.yibaiqi.face.recognition.ui.SynthActivity;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.FaceViewModel;
import com.yibaiqi.face.recognition.viewmodel.RongViewModel;
import com.yibaiqi.face.recognition.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.rong.imlib.RongIMClient;

public class SplashActivity extends BaseActivity {

    @Inject
    AppDatabase database;
    @Inject
    AppExecutors appExecutors;

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

        UserViewModel model = ViewModelProviders.of(this, App.getInstance().factory)
                .get(UserViewModel.class);
        System.out.println("------>>>SplashActivity" + model);
        model.say();
//        model.getMo().observe(this, movieResource -> {
//            System.out.println("--->>>这里执行了");
//        });
        model.getMo2().observe(this, movieResource -> {
            String ss = new Gson().toJson(movieResource);
            System.out.println("--->>>这里执行了 2 这是真的" + ss);
        });


        findViewById(R.id.btnOn).setOnClickListener(v -> new Manager(getApplicationContext()).setGateIO(true));
        findViewById(R.id.btnOff).setOnClickListener(v -> new Manager(getApplicationContext()).setGateIO(false));

        findViewById(R.id.speak).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, SynthActivity.class));
        });
        findViewById(R.id.jump).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, ConfigActivity.class));
        });
        findViewById(R.id.jump_main).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        });

        findViewById(R.id.insert).setOnClickListener(v -> {
            if (pos == 7) {
                pos = 0;
            } else {
                pos++;
            }

        });

        findViewById(R.id.delete).setOnClickListener(v -> {
            appExecutors.diskIO().execute(() -> {
                database.userDao().deleteAll();
                System.out.println("------删除成功");
            });
        });

        findViewById(R.id.query_all).setOnClickListener(v -> {
            appExecutors.diskIO().execute(() -> {
                List<User> dbList = database.userDao().getAll();
                if (dbList == null || dbList.isEmpty()) {
                    System.out.println("------此表为空，无数据");
                } else {
                    for (User user : dbList) {
                        System.out.println("------查找所有用户" + user.toString());
                    }
                }
            });

        });
        findViewById(R.id.query_by_id).setOnClickListener(v -> {
            appExecutors.diskIO().execute(() -> {
                try {
                    User user = database.userDao().findById(Integer.parseInt(et.getText().toString()));
                    if (user == null) {
                        System.out.println("------查不到该用户");
                    } else {
                        System.out.println("------查找到该用户：" + user.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("------异常：" + e.getMessage());
                }
            });
        });


        //-------------------融云
        RongViewModel serverViewModel = ViewModelProviders.of(this)
                .get(RongViewModel.class);
        serverViewModel.getConnectStatus().observe(this, isConnect -> {
            System.out.println("--->>>>>>" + isConnect);
        });
        serverViewModel.getRongMessage().observe(this, msg -> {
            Toast.makeText(this, msg.toString(), Toast.LENGTH_LONG).show();
        });

        //user1
        serverViewModel.connect("SVpWxysVtwqdsqaS5sXcjsoxYNNn6TblrJ/u3gGjc1wH8B0+muXSTIkLrAb5gIHNqXVoOD7LiznX2ypobFQZ9g==");
        //user2
//        serverViewModel.connect("p4o/K1eHyS5DDWf3r16nCHQQxjv13DQkqZKYBm2cHzMC9g+G9YHutgyqJu0bBlWGGBj7gwF73fg=");

//        serverViewModel.registerMessage();

        RongIMClient.setOnReceiveMessageListener((message, left) -> {
//            rongMessage.setValue(message);
            System.out.println("--->>>>>>消息：" + message);
            System.out.println("--->>>>>>未拉取：" + left);
            return false;
        });

//        serverViewModel.sendTestMsg();

    }

    //-----------------------------------------------------
    private void initFaceEngine() {
        FaceViewModel faceModel = ViewModelProviders.of(this, App.getInstance().factory).get(FaceViewModel.class);
        faceModel.initBDFaceEngine("QY8C-NXN5-9XH7-8VCC");// 测试写死

//        faceModel.registerDevice().observe(this, resource -> {
//            if (resource == null)
//                return;
//            switch (resource.status) {
//                case SUCCESS:
//                    if (resource.data != null) {
//                        faceModel.initBDFaceEngine(resource.data.getData().getSerialNumber());
//                    }
//                    break;
//                case ERROR:
//                    break;
//                case LOADING:
//                    break;
//            }
//        });

        faceModel.getInitStatus().observe(this, status -> {
            if (status != null && status) {
                startActivity(new Intent(SplashActivity.this, CMainActivity.class));
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        });

    }

}
