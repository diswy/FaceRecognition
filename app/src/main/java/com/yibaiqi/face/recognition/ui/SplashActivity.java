package com.yibaiqi.face.recognition.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.widget.EditText;

import com.baidu.idl.sample.ui.MainActivity;
import com.google.gson.Gson;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.db.AppDatabase;
import com.yibaiqi.face.recognition.di.DaggerActivityComponent;
import com.yibaiqi.face.recognition.entity.User;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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


        final List<User> lists = new ArrayList<>();
        lists.add(new User("马里奥", 18));
        lists.add(new User("舞力全开", 28));
        lists.add(new User("空洞骑士", 38));
        lists.add(new User("火影忍者", 48));
        lists.add(new User("海贼王", 58));
        lists.add(new User("死神", 68));
        lists.add(new User("死亡细胞", 78));
        lists.add(new User("最终幻想", 98));


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

            appExecutors.diskIO().execute(() -> {
                database.userDao().insert(lists.get(pos));
                System.out.println("------插入成功");
            });
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
    }
}
