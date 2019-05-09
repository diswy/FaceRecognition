package com.yibaiqi.face.recognition.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
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
import com.yibaiqi.face.recognition.vo.OSSConfig;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Resource;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Observable;

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


    //-------------OSS
    private OSS mOss;
    private OSSConfig mOssConfig;

    public void initOSS(LifecycleOwner owner) {
        faceRepository.getOSSConfig().observe(owner, ossConfig -> {
            System.out.println("---->>>:" + ossConfig);
            System.out.println("--->>>这里执行了");

            if (ossConfig != null && ossConfig.data != null && ossConfig.data.getData() != null) {
                mOssConfig = ossConfig.data.getData();
//                String ak = ossConfig.data.getData().getAccessKeyId();
//                String sk = ossConfig.data.getData().getAccessKeySecret();
                String endpoint = ossConfig.data.getData().getEndpoint();
//                OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(ak, sk);
                OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider("http://xueyiguan.10130422.com/sts-server/sts.php");
                ClientConfiguration conf = new ClientConfiguration();
                conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
                mOss = new OSSClient(App.getInstance(), "http://" + endpoint, credentialProvider, conf);

                System.out.println("---->>>:" + endpoint);
                System.out.println("---->>>:" +mOssConfig.getBucketName() );

            }
        });
    }


    public void asyncPutImage(String objectName, String localFile) {
        if (mOss == null) {
            return;
        }

        if (objectName.equals("")) {
            return;
        }

        File file = new File(localFile);
        if (!file.exists()) {
            return;
        }
        if (mOssConfig == null) {
            return;
        }

        System.out.println("--->>>这里执行了");

        PutObjectRequest put = new PutObjectRequest(mOssConfig.getBucketName(), objectName, localFile);
        put.setCRC64(OSSRequest.CRC64Config.YES);
        OSSAsyncTask task = mOss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                System.out.println("-----图片上传成功！");
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientException != null) {
                    // 本地异常如网络异常等
                    clientException.printStackTrace();
                    info = clientException.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    System.out.println("---->>>>>" + serviceException.getErrorCode());
                    System.out.println("---->>>>>" + serviceException.getRequestId());
                    System.out.println("---->>>>>" + serviceException.getHostId());
                    System.out.println("---->>>>>" + serviceException.getRawMessage());
//                    Log.e("ErrorCode", serviceException.getErrorCode());
//                    Log.e("RequestId", serviceException.getRequestId());
//                    Log.e("HostId", serviceException.getHostId());
//                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                    System.out.println("---->>>>>" + info);
                }

            }
        });

    }


}
