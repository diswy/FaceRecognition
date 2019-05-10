package com.yibaiqi.face.recognition.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.common.FaceEnvironment;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.manager.UserInfoManager;
import com.baidu.idl.sample.model.ARGBImg;
import com.baidu.idl.sample.utils.FeatureUtils;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.ImageUtils;
import com.baidu.idl.sample.utils.ToastUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.repository.FaceRepository;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.tools.EBQValue;
import com.yibaiqi.face.recognition.tools.Mac;
import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.DbUserOption;
import com.yibaiqi.face.recognition.vo.OSSConfig;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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
            } else {
                String format = "请联系管理员，错误代码：%d  %s";
                ToastUtils.toast(app, String.format(Locale.CHINA, format, code, response));
                initSuccess.postValue(false);
            }
        });


        FaceSDKManager.getInstance().setFaceModelInitListener((faceDetector, faceFeature, faceLive) -> {
            if (faceDetector && faceFeature && faceLive) {
                initSuccess.postValue(true);
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

    public void initOSS() {
        ACache cache = faceRepository.getCache();
        OSSConfig ossConfig = (OSSConfig) cache.getAsObject("oss_config");
        if (ossConfig == null) {
            return;
        }

        mOssConfig = ossConfig;
        String endpoint = ossConfig.getEndpoint();
        OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider("http://xueyiguan.10130422.com/sts-server/sts.php");
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        mOss = new OSSClient(App.getInstance(), "http://" + endpoint, credentialProvider, conf);
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

    //-----------------download

    public void initDownload(Context context) {
        FileDownloader.setup(context);
    }

    public void downloadPic(String picUrl, String fileName) {
        File file = new File(EBQValue.REGISTER_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        System.out.println("----------任务开始");
        FileDownloader.getImpl().create(picUrl)
                .setPath(EBQValue.REGISTER_PATH + "a.png")
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        System.out.println("----------图片下载成功！");
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        System.out.println("----------任务错误：" + e.getMessage());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    //----------------人脸注册
    private void registerFace(String userKey, String userName) {
        faceRepository.getAppExecutors().diskIO().execute(() -> {
            File picPath = new File(EBQValue.REGISTER_PATH, userKey + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(picPath.getAbsolutePath());
            ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
            FaceInfo[] faceInfos = new FaceInfo[1];

            FaceEnvironment environment = new FaceEnvironment();
            environment.detectInterval = environment.trackInterval = 0;
            FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);

            if (argbImg.width * argbImg.height <= 1000 * 1000) {
                byte[] bytes = new byte[512];
                float ret = -1;
                ret = FaceApi.getInstance().getFeature(argbImg, bytes,
                        FaceFeature.FeatureType.FEATURE_VIS, environment, faceInfos);
                FaceInfo faceInfo = faceInfos[0];
                if (ret == -1) {
                    //失败
                } else if (ret == 128) {
                    Bitmap cropBitmap = null;
                    String cropImgName = null;
                    // 人脸抠图
                    if (faceInfo != null) {
                        cropBitmap = ImageUtils.noBlackBoundImgCrop(faceInfo.landmarks,
                                argbImg.height, argbImg.width, argbImg.data);

                        if (cropBitmap == null) {
                            cropBitmap = bitmap;
                        }
                        cropImgName = "crop_" + userKey + ".jpg";
                    }
                    Feature feature = new Feature();
                    feature.setGroupId("0");
                    feature.setUserId(userKey);
                    feature.setFeature(bytes);
                    feature.setImageName(userKey + ".jpg");
                    feature.setUserName(userName);
                    feature.setCropImageName(cropImgName);

                    // 保存数据库
                    if (FaceApi.getInstance().featureAdd(feature)) {
                        // 保存图片到新目录中
                        File facePicDir = FileUtils.getFacePicDirectory();
                        // 保存抠图图片到新目录中
                        File faceCropDir = FileUtils.getFaceCropPicDirectory();

                        if (facePicDir != null) {
                            File savePicPath = new File(facePicDir, userKey + ".jpg");
                            if (FileUtils.saveFile(savePicPath, bitmap)) {
                                System.out.println("--->>>人脸注册成功->保存成功");
                            }
                        }

                        if (faceCropDir != null && cropBitmap != null) {
                            File saveCropPath = new File(faceCropDir, cropImgName);
                            if (FileUtils.saveFile(saveCropPath, cropBitmap)) {
                                if (cropBitmap != null && !cropBitmap.isRecycled()) {
                                    cropBitmap.recycle();
                                }
                            }
                        }
                    }
                }
            } else {
                // 失败，图片太大 超过了1000*1000
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }

        });
    }


    //--------------逻辑事务
    public void batchUpdate() {
        // 每次启动设备，也许会收到后台通知的操作
        ACache cache = faceRepository.getCache();
        DbUserOption exData = (DbUserOption) cache.getAsObject("task_update");
        if (exData == null) {
            // 空数据，没有收到通知，无需处理
            return;
        }

        if (exData.getAdd() != null && exData.getAdd().size() > 0) {
            // 数据库新增数据,需要注册人脸
            Disposable disposable = Flowable.fromIterable(exData.getAdd())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Consumer<DbOption>() {
                        @Override
                        public void accept(DbOption dbOption) throws Exception {
                            downloadAndRegister(dbOption.getUser_key(), dbOption.getReal_name(), dbOption.getFace_image(), false);
                        }
                    });
        }

        if (exData.getUpdate() != null && exData.getUpdate().size() > 0) {
            // 人脸库需要更新，先删除->再添加
            Disposable disposable = Flowable.fromIterable(exData.getAdd())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Consumer<DbOption>() {
                        @Override
                        public void accept(DbOption dbOption) throws Exception {
                            downloadAndRegister(dbOption.getUser_key(), dbOption.getReal_name(), dbOption.getFace_image(), true);
                        }
                    });
        }

        if (exData.getDelete() != null && exData.getDelete().size() > 0) {
            // 人脸库需要删除
            Disposable disposable = Flowable.fromIterable(exData.getAdd())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Consumer<DbOption>() {
                        @Override
                        public void accept(DbOption dbOption) throws Exception {
                            delFaceFeature(dbOption.getUser_key(), dbOption.getReal_name());
                        }
                    });
        }

        cache.remove("task_update");
    }

    //---------组合事务
    private void downloadAndRegister(String userKey, String userName, String picUrl, Boolean needDel) {
        if (needDel) {
            delFaceFeature(userKey, userName);
        }

        File file = new File(EBQValue.REGISTER_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        if (TextUtils.isEmpty(picUrl)) {
            return;
        }

        FileDownloader.getImpl().create(picUrl)
                .setPath(EBQValue.REGISTER_PATH + userKey + ".jpg")
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        // 图片下载完毕
                        registerFace(userKey, userName);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {

                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    private void delFaceFeature(String userKey, String name) {
        faceRepository.getAppExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Feature> featureList = DBManager.getInstance().queryFeatureByName(name);
                if (featureList != null && featureList.size() > 0) {
                    for (int i = 0; i < featureList.size(); i++) {
                        Feature feature = featureList.get(i);
                        if (feature.getUserId().equals(userKey)) {// 需要删除
                            List<Feature> delList = new ArrayList<>();
                            delList.add(feature);
                            UserInfoManager.getInstance().batchRemoveFeatureInfo(delList, new UserInfoManager.UserInfoListener(), 1);
                        }
                    }
                }
            }
        });
    }
}
