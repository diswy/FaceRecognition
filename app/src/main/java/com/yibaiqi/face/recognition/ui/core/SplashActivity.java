package com.yibaiqi.face.recognition.ui.core;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.sample.ui.MainActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.db.AppDatabase;
import com.yibaiqi.face.recognition.di.DaggerActivityComponent;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.tools.GpioUtils;
import com.yibaiqi.face.recognition.tools.Mac;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;
import com.yibaiqi.face.recognition.viewmodel.FaceViewModel;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.ExData;
import com.yibaiqi.face.recognition.vo.OSSConfig;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

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

    }

    @Override
    public void initialize() {
        DaggerActivityComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .build()
                .inject(this);

        try {
            GpioUtils.upgradeRootPermissionForExport();
            if (GpioUtils.exportGpio(146)) {
                Log.e("ebq", "获取IO：成功");
                GpioUtils.upgradeRootPermissionForGpio(146);
                if (GpioUtils.setGpioDirection(146, 1)) {
                    Log.e("ebq", "设置IO为输入口成功");
                }
            } else {
                Log.e("ebq", "获取IO：失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        boolean a = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
//        Log.e("ebq", "存储卡可用状态：" + a);
//
//        ImageView iv = findViewById(R.id.iv_test);
//        iv.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.i("ebq", "图标宽：" + iv.getWidth() + " ;图标高：" + iv.getHeight());
//            }
//        });


//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//        Log.i("ebq", "屏幕宽：" + width + " ;屏幕高：" + height);

        final RxPermissions rxPermissions = new RxPermissions(this);
        Disposable d = rxPermissions.request(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    Log.i("ebq", "权限" + aBoolean);
                    if (aBoolean) {
                        initFaceEngine();
                    }
                });
    }

    private void initFaceEngine() {
        FaceViewModel faceModel = ViewModelProviders.of(this, App.getInstance().factory).get(FaceViewModel.class);

        faceModel.getInitStatus().observe(this, status -> {
            if (status != null && status) {
                faceModel.bindDevice();
                startActivity(new Intent(SplashActivity.this, CMainActivity.class));
                this.finish();
            }
        });

        faceModel.initBDFaceEngine("QY8C-NXN5-9XH7-8VCC");

        faceModel.registerDevice().observe(this, resource -> {
            if (resource == null)
                return;
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null && resource.data.getData() != null) {

                        cache.put("token", resource.data.getData().getToken());
                        cache.put("im_token", resource.data.getData().getImToken());

                        OSSConfig ossConfig = resource.data.getData().getOssConfig();
                        if (ossConfig != null) {
                            cache.put("oss_config", ossConfig);
                            Log.i("ebq", "基础配置：OSS配置成功");
                        }

                        ExData exData = resource.data.getData().getData();
                        if (exData != null && exData.getUsers() != null) {// 后台通知消息，也许有新操作
                            List<DbOption> list = new ArrayList<>();
                            int addCount = 0;
                            int delCount = 0;

                            if (exData.getUsers().getAdd() != null) {

                                for (DbOption item : exData.getUsers().getAdd()) {
                                    if (!TextUtils.isEmpty(item.getFace_image())) {
                                        DbOption mData = new DbOption(
                                                item.getData_key(),
                                                item.getUser_key(),
                                                item.getReal_name(),
                                                item.getFace_image(),
                                                0);// 新增用户
                                        list.add(mData);
                                        addCount++;
                                    }
                                }

                            }

                            if (exData.getUsers().getDelete() != null) {
                                for (DbOption item : exData.getUsers().getDelete()) {
                                    DbOption mData = new DbOption(
                                            item.getData_key(),
                                            item.getUser_key(),
                                            item.getReal_name(),
                                            "",
                                            1);
                                    list.add(mData);
                                    delCount++;
                                }
                            }

                            Log.i("ebq", "数据更新:来源->启动注册设备------新增数据:" + addCount + "条 ; 删除数据:" + delCount + "条");
                            faceModel.insert(list);
                        }
                        faceModel.initBDFaceEngine(resource.data.getData().getSerialNumber());
                    }
                    break;
                case ERROR:
                    Log.i("ebq", "失败" + resource.message);
                    break;
                case LOADING:
                    Log.i("ebq", "加载中");
                    break;
            }
        });

    }
}
