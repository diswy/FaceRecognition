package com.yibaiqi.face.recognition.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.facesdk.callback.AuthCallback;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.utils.ToastUtils;
import com.yibaiqi.face.recognition.R;
import com.yibaiqi.face.recognition.ui.base.BaseActivity;

import static com.baidu.idl.sample.common.GlobalSet.LICENSE_ONLINE;


public class ConfigActivity extends BaseActivity {

    private FaceAuth faceAuth;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_config;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initialize() {
        faceAuth = new FaceAuth();
        // 建议3288板子flagsThreads设置2,3399板子设置4
        faceAuth.setAnakinThreadsConfigure(2, 0);
        String key = "ZUJN-TZD0-BPYP-BSSU".trim().toUpperCase();// 测试华为手机
//        String key = "FFUY-H4NL-NKJR-VG4P".trim().toUpperCase();// 闸机头



        faceAuth.setActiveLog(FaceAuth.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE);
        initLicenseOnLine(key);
    }

    // 在线鉴权
    private void initLicenseOnLine(final String key) {
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(this, "序列号不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        faceAuth.initLicenseOnLine(this, key, (code, response, licenseKey) -> {
            if (code == 0) {
                GlobalSet.FACE_AUTH_STATUS = 0;
                // 初始化人脸
                FaceSDKManager.getInstance().initModel(mContext);
                // TODO 需調整
                String cacheKey = GlobalSet.getLicenseOnLineKey();
                System.out.println("--->>>缓存KEY："+cacheKey);

                // 初始化数据库
                DBManager.getInstance().init(getApplicationContext());
                // 加载feature 内存
                FaceSDKManager.getInstance().setFeature();
                GlobalSet.setLicenseOnLineKey(key);
                GlobalSet.setLicenseStatus(LICENSE_ONLINE);
                finish();
            } else {
                ToastUtils.toast(mContext, code + "  " + response);
                System.out.println("--->>>response："+response);
            }
        });
    }
}
