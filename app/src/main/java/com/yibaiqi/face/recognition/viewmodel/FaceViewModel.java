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
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Resource;

import java.util.List;
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

    /**
     * 人脸识别引擎初始化状态
     */
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

    public void initBDFaceEngine(String key) {
        initLicenseOnLine(key);
    }

    public void addRecord() {

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
            System.out.println("------------code = " + code);
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
                initSuccess.postValue(true);
            } else {
                String format = "请联系管理员，错误代码：%d  %s";
                ToastUtils.toast(app, String.format(Locale.CHINA, format, code, response));
                initSuccess.postValue(false);
            }
        });
    }

    // 绑定设备
    public void bindDevice() {
        FaceAuth faceAuth = new FaceAuth();
        faceRepository.bindDevice(faceAuth.getDeviceId(app));
    }

    //--------数据库相关
    public void insert(List<DbOption> list) {
        faceRepository.insert(list);
    }

    public void update(List<DbOption> list) {
        faceRepository.update(list);
    }

    public void delete(List<DbOption> list) {
        faceRepository.delete(list);
    }

    //--------摄像头
    public boolean isCameraEnable() {
        return faceRepository.isCameraEnable();
    }

    public String getCameraIp() {
        return faceRepository.getCameraIp();
    }

    public String getCameraAccount() {
        return faceRepository.getCameraAccount();
    }

    public String getCameraPwd() {
        return faceRepository.getCameraPwd();
    }

    public int getCameraPort() {
        return faceRepository.getCameraPort();
    }

}
