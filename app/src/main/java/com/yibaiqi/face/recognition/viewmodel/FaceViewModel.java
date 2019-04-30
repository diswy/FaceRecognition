package com.yibaiqi.face.recognition.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.facesdk.callback.AuthCallback;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.ui.LicenseActivity;
import com.baidu.idl.sample.ui.MainActivity;
import com.baidu.idl.sample.utils.ToastUtils;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.repository.FaceRepository;
import com.yibaiqi.face.recognition.tools.Mac;
import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Resource;

import java.util.Locale;

import javax.inject.Inject;

import static com.baidu.idl.sample.common.GlobalSet.LICENSE_ONLINE;

/**
 * 人脸识别
 * Created by @author xiaofu on 2019/4/23.
 */
public class FaceViewModel extends ViewModel {

    private final FaceRepository faceRepository;
    private final App app;
    private MutableLiveData<Boolean> initSuccess = new MutableLiveData<>();

    @Inject
    FaceViewModel(FaceRepository faceRepository, App app) {
        this.faceRepository = faceRepository;
        this.app = app;
    }

    public LiveData<Boolean> getInitStatus() {
        return initSuccess;
    }

    /**
     * 获取注册KEY
     */
    public LiveData<Resource<BaseResponse<RegisterDevice>>> registerDevice() {
        FaceAuth faceAuth = new FaceAuth();
        String devId = faceAuth.getDeviceId(app);
        String mac = Mac.getMac(app);
        System.out.println("--->>>设备指纹，来自百度工具获取：" + devId);
        System.out.println("--->>>设备MAC:" + mac);
        return faceRepository.registerDevice(devId, mac);
    }

    public void initBDFaceEngine() {
        initLicenseOnLine("QY8C-NXN5-9XH7-8VCC");

//        if (GlobalSet.FACE_AUTH_STATUS != 0) {// 未鉴权
//            initLicenseOnLine("QY8C-NXN5-9XH7-8VCC");
//        }
    }

    //--------私有方法
    private void initLicenseOnLine(final String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        FaceAuth faceAuth = new FaceAuth();
        // 建议3288板子flagsThreads设置2,3399板子设置4
        faceAuth.setAnakinThreadsConfigure(2, 0);
        faceAuth.initLicenseOnLine(app, key, (code, response, licenseKey) -> {
            if (code == 0) {// 初始化成功
                // 初始化人脸
                FaceSDKManager.getInstance().initModel(app);
                // 初始化数据库
                DBManager.getInstance().init(app);
                // 加载feature 内存
                FaceSDKManager.getInstance().setFeature();
                GlobalSet.setLicenseOnLineKey(key);
                GlobalSet.setLicenseStatus(LICENSE_ONLINE);

                GlobalSet.FACE_AUTH_STATUS = 0;
                System.out.println("___zhelizhixingle");
                initSuccess.postValue(true);
            } else {
                String format = "请联系管理员，错误代码：%d  %s";
                ToastUtils.toast(app, String.format(Locale.CHINA, format, code, response));
                initSuccess.postValue(false);
            }
        });
    }


}
