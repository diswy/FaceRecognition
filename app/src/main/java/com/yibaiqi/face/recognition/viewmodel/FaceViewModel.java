package com.yibaiqi.face.recognition.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

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
import com.baidu.idl.sample.MyConfig;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.common.FaceEnvironment;
import com.baidu.idl.sample.common.GlobalSet;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.manager.FaceLiveness;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.ARGBImg;
import com.baidu.idl.sample.utils.FeatureUtils;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.ImageUtils;
import com.baidu.idl.sample.utils.ToastUtils;
import com.baidu.idl.sample.view.MonocularView;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.yibaiqi.face.recognition.App;
import com.yibaiqi.face.recognition.Key;
import com.yibaiqi.face.recognition.repository.FaceRepository;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.tools.EBQValue;
import com.yibaiqi.face.recognition.tools.FileUtil;
import com.yibaiqi.face.recognition.tools.Mac;
import com.yibaiqi.face.recognition.ui.core.CMainActivity;
import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.DbUserOption;
import com.yibaiqi.face.recognition.vo.DeviceName;
import com.yibaiqi.face.recognition.vo.ExData;
import com.yibaiqi.face.recognition.vo.ImportUser;
import com.yibaiqi.face.recognition.vo.LocalUser;
import com.yibaiqi.face.recognition.vo.MyRecord;
import com.yibaiqi.face.recognition.vo.OSSConfig;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Remote;
import com.yibaiqi.face.recognition.vo.RemoteRecord;
import com.yibaiqi.face.recognition.vo.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.baidu.idl.sample.common.GlobalSet.LICENSE_ONLINE;
import static com.yibaiqi.face.recognition.Key.SETTING_BD_FACE;
import static com.yibaiqi.face.recognition.ui.core.CMainActivity.MONOCULAR_PAUSE;
import static com.yibaiqi.face.recognition.ui.core.CMainActivity.MONOCULAR_RESUME;

/**
 * 人脸识别
 * Created by @author xiaofu on 2019/4/23.
 */
public class FaceViewModel extends ViewModel {
    private int errorCount = 0;
    private final FaceRepository faceRepository;
    private final App app;
    private MutableLiveData<Boolean> initSuccess = new MutableLiveData<>();

    private MutableLiveData<Boolean> isRegister = new MutableLiveData<>();

    public MutableLiveData<Boolean> getIsRegister() {
        return isRegister;
    }

    @Inject
    FaceViewModel(FaceRepository faceRepository, App app) {
        this.faceRepository = faceRepository;
        this.app = app;
        isRegister.postValue(false);
    }

    private Handler handler;

    public void initHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     *
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

    public String getRongToken() {
        return faceRepository.getCache().getAsString("im_token");
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
                MyConfig myConfig = new MyConfig();
                ACache cache = ACache.get(app);
                Object object = cache.getAsObject(SETTING_BD_FACE);
                if (object != null) {
                    if (object instanceof MyConfig) {
                        myConfig = (MyConfig) object;
                    }
                }

                FaceSDKManager.getInstance().initModel(app, myConfig);
                // 初始化数据库
                DBManager.getInstance().init(app);
                // 加载feature 内存
                int i = FaceSDKManager.getInstance().setFeature();
                Log.d("ebq", "人脸库：系统初始化，当前系统人脸库数量为：" + i);
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
        Disposable d = faceRepository.bindDevice2(faceAuth.getDeviceId(app))
                .subscribeOn(Schedulers.io())
                .subscribe(s -> Log.d("ebq", "设备绑定：绑定设备成功！"),
                        throwable -> Log.d("ebq", "设备绑定：绑定设备出错:" + throwable.getMessage()));
    }

    public LiveData<Resource<BaseResponse<DeviceName>>> getDevice() {
        return faceRepository.getDevice();
    }

    //--------数据库相关
    private MediatorLiveData<List<DbOption>> mLiveData = new MediatorLiveData<>();
    private MediatorLiveData<List<MyRecord>> mLiveRecordData = new MediatorLiveData<>();

    public void insert(List<DbOption> list) {
        faceRepository.insert(list);
    }

    public void insert(MyRecord data) {
        faceRepository.insert(data);
    }

    public void update(List<DbOption> list) {
        faceRepository.update(list);
    }

    public void delete(List<DbOption> list) {
        faceRepository.delete(list);
    }

    public void userInsert(List<LocalUser> list) {
        faceRepository.userInsert(list);
    }

    public void userDel(List<LocalUser> list) {
        faceRepository.userDel(list);
    }

    public LocalUser getUserByKey(String key){
        return faceRepository.getUserByKey(key);
    }


    public void observerData(LifecycleOwner owner) {
        mLiveData.observe(owner, list -> {
            if (list != null) {
                Log.i("ebq", "数据更新:来源->数据库,待执行任务总共：" + list.size() + "条");
            }

            if (list != null && list.size() > 0) {
                // 本地用户表
                List<LocalUser> insertOrUpdateUsers = new ArrayList<>();
                List<LocalUser> delUsers = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getType_flag() == 2) {// 更新本地数据库
                        LocalUser localUser = new LocalUser(list.get(i).getUser_key(), list.get(i).getFull_name());
                        insertOrUpdateUsers.add(localUser);
                    }
                    if (list.get(i).getType_flag() == 3) {// 删除本地数据库
                        LocalUser localUser = new LocalUser(list.get(i).getUser_key(), list.get(i).getFull_name());
                        delUsers.add(localUser);
                    }
                }
                userInsert(insertOrUpdateUsers);
                userDel(delUsers);

                updateTaskDelay(list);
            }

            if (list != null && list.size() == 0) {
                batchDownload();
            }

        });

        updateData();// 第一次进来执行一次就好了
    }

    public void updateTaskDelay(List<DbOption> taskList) {
        Log.w("ebq", "任务：不休眠直接轮询执行任务，剩余任务数量：" + taskList.size());
        List<DbOption> tempList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getStatus() == 1) {
                tempList.add(taskList.get(i));
            }
        }

        if (tempList.size() > 0) {
            Log.i("ebq", "数据更新:来源->数据库,需要删除的数据有" + tempList.size() + "条");
            Random random = new Random();
            int pos = random.nextInt(tempList.size());
            DbOption item = tempList.get(pos);

            delFaceFeature(item.getUser_key(), item.getReal_name(), item);
            return;
        }

        Log.i("ebq", "数据更新:来源->数据库,需要新增的数据：" + taskList.size() + "条");
        // 生成随机数轮询，即使某个task失败，也可以让别的任务继续执行
        Random random = new Random();
        int pos = random.nextInt(taskList.size());
        DbOption item = taskList.get(pos);
        // 理论上先执行完所有的删除任务才会执行新增，保险起见就先这样留着吧
        switch (item.getStatus()) {
            case 0:// 新增
                downloadAndRegister(item.getUser_key(), item.getReal_name(), item.getFace_image(), item);
                break;
            case 1:// 删除
                delFaceFeature(item.getUser_key(), item.getReal_name(), item);
                break;
        }

    }

    public void observerRecordData(Context context, LifecycleOwner owner) {
        mLiveRecordData.observe(owner, list -> {
            if (list != null) {
                Log.i("ebq", "记录:来源->数据库,待执行任务总共：" + list.size() + "条");
            }

            if (list != null && list.size() > 0) {
                // 生成随机数轮询，即使某个task失败，也可以让别的任务继续执行
                Random random = new Random();
                int pos = random.nextInt(list.size());

                MyRecord myRecord = list.get(pos);
                uploadOSSAndRecord(context, owner, myRecord);
            }
        });
        updateRecordData();// 这里应该只执行一次
    }

    // 可以人为刷新数据，用于失败后依旧通知刷新
    private void updateData() {
        handler.removeMessages(CMainActivity.TASK);
        handler.sendEmptyMessageDelayed(CMainActivity.TASK, 3000L);
    }

    public void updateTaskData() {
        mLiveData.addSource(faceRepository.observeAll(), new Observer<List<DbOption>>() {
            @Override
            public void onChanged(@Nullable List<DbOption> list) {
                mLiveData.setValue(list);
            }
        });
    }

    private void updateRecordData() {
        handler.removeMessages(CMainActivity.UPLOAD);
        handler.sendEmptyMessageDelayed(CMainActivity.UPLOAD, 3000L);
    }

    public void updateUploadData() {
        mLiveData.addSource(faceRepository.observeRecordAll(), new Observer<List<MyRecord>>() {
            @Override
            public void onChanged(@Nullable List<MyRecord> myRecords) {
                mLiveRecordData.setValue(myRecords);
            }
        });
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
//        OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider("http://xueyiguan.10130422.com/sts-server/sts.php");
        OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider("https://www.yzx110.com/sts-server/sts.php");
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        mOss = new OSSClient(App.getInstance(), "http://" + endpoint, credentialProvider, conf);
    }

    public void asyncPutImage(String objectName, String localFile, MyRecord myRecord) {
        Log.w("ebq", "上传记录：图片路径" + localFile);
        if (mOss == null) {
            Log.w("ebq", "上传记录：OSS服务为空");
            return;
        }

        if (objectName.equals("")) {
            Log.w("ebq", "上传记录：文件名为空");
            faceRepository.delete(myRecord);
            return;
        }

        final File file = new File(localFile);
        if (!file.exists()) {
            Log.w("ebq", "上传记录：文件不存在");
            faceRepository.delete(myRecord);
            return;
        }
        if (mOssConfig == null) {
            Log.w("ebq", "上传记录：OSS配置为空");
            faceRepository.delete(myRecord);
            return;
        }
        Log.w("ebq", "桶名：" + mOssConfig.getBucketName());
        Log.w("ebq", "KEY:" + mOssConfig.getObjectPath() + objectName);

        Log.w("ebq", "上传记录：构建上传请求");
        PutObjectRequest put = new PutObjectRequest(mOssConfig.getBucketName(), mOssConfig.getObjectPath() + objectName, localFile);
        put.setCRC64(OSSRequest.CRC64Config.YES);
        OSSAsyncTask task = mOss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.w("ebq", "上传记录：图片上传成功");
                FileUtil.delFile(file);
                if (myOssListener != null) {
                    currentSuccess++;
                    myOssListener.uploadSuccess();
                }
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                faceRepository.delete(myRecord);
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
                    info = serviceException.toString();
                    System.out.println("---->>>>>" + info);
                }
                Log.w("ebq", "上传记录：图片上传失败：理由：" + info);
            }
        });

    }

    //-----------------download

    public void initDownload(Context context) {
        FileDownloader.setup(context);
    }

    //---------组合事务
    private List<ImportUser> importUserList = new ArrayList<>();
    private int batchSuccess = 0;
    private int batchError = 0;

    private void downloadAndRegister(String userKey, String userName, String picUrl, DbOption data) {
        File file = new File(EBQValue.REGISTER_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        if (TextUtils.isEmpty(picUrl)) {
            errorCount++;
            faceRepository.delete(data);
            Log.e("ebq", "错误：图片地址为空，累计发生错误数量：" + errorCount);
            return;
        }

        ImportUser item = new ImportUser(userKey, userName, picUrl);
        importUserList.add(item);
        faceRepository.delete(data);

//        Log.i("ebq", "图片下载:开始下载图片：用户名：" + userName);
//        FileDownloader.getImpl().create(picUrl)
//                .setPath(EBQValue.REGISTER_PATH + userKey + ".jpg")
//                .setListener(new FileDownloadLargeFileListener() {
//                    @Override
//                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
//
//                    }
//
//                    @Override
//                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
//
//                    }
//
//                    @Override
//                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
//
//                    }
//
//                    @Override
//                    protected void completed(BaseDownloadTask task) {
//                        // 图片下载完毕
//                        Log.e("ebq", "下载：图片下载成功,图片路径" + EBQValue.REGISTER_PATH + userKey + ".jpg");
//                        registerFace(userKey, userName, data);
//                    }
//
//                    @Override
//                    protected void error(BaseDownloadTask task, Throwable e) {
//                        // 出错了随机轮询另一个任务
//                        errorCount++;
//                        faceRepository.delete(data);
//                        Log.e("ebq", "错误：图片下载错误，累计发生错误数量：" + errorCount);
//                    }
//
//                    @Override
//                    protected void warn(BaseDownloadTask task) {
//
//                    }
//                }).start();
    }

    private final FileDownloadListener downloadListener = new FileDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {
        }

        @Override
        protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            Log.e("ybq", "下载完成：" + task.getPath());
            batchSuccess++;

            if (batchSuccess == (importUserList.size() - batchError)) {
                batchRegisterFace();
            }
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            Log.e("ybq", "下载发生错误：" + e.getMessage());
            batchError++;
        }

        @Override
        protected void warn(BaseDownloadTask task) {
        }
    };

    public void batchDownload() {
        if (importUserList.isEmpty())
            return;

        batchSuccess = 0;
        batchError = 0;

        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(downloadListener);
        final List<BaseDownloadTask> tasks = new ArrayList<>();
        for (int i = 0; i < importUserList.size(); i++) {
            File oldFile = new File(EBQValue.REGISTER_PATH + importUserList.get(i).getUserKey() + ".jpg");
            if (oldFile.exists()) {
                boolean delStatus = oldFile.delete();
                Log.e("ybq", "**事务**：存在老旧照片，是否删除：" + "位置：" + i + ":" + delStatus);
            }

            tasks.add(FileDownloader.getImpl().create(importUserList.get(i).getPicUrl())
                    .setPath(EBQValue.REGISTER_PATH + importUserList.get(i).getUserKey() + ".jpg")
                    .setTag(i + 1));
        }
        queueSet.disableCallbackProgressTimes();
        queueSet.setAutoRetryTimes(3);

        queueSet.downloadTogether(tasks);
        queueSet.start();
    }


    private interface MyOssListener {
        void uploadSuccess();
    }

    private int totalCount = 0;// 记录任务
    private int currentSuccess = 0;// 成功个数

    private MyOssListener myOssListener;

    private void uploadOSSAndRecord(Context context, LifecycleOwner owner, MyRecord myRecord) {
        totalCount = 0;// 保险起见，重置0
        currentSuccess = 0;

        if (myRecord.isHikStatus()) {// 海康截图成功
            ++totalCount;
        }
        if (myRecord.isFaceStatus()) {// 人脸识别截图成功
            ++totalCount;
        }

        if (totalCount == 0) {// 两张图片都出错了，理论上极低概率~但是依旧需要上传
            RemoteRecord mRecord = new RemoteRecord(myRecord.getUser_key(),
                    "", "", myRecord.getCreate_time());
            syncRecord(owner, mRecord, myRecord);
            return;
        }

        myOssListener = new MyOssListener() {
            @Override
            public void uploadSuccess() {
                System.out.println("-----------total=" + totalCount);
                System.out.println("-----------currentSuccess=" + currentSuccess);
                if (totalCount == currentSuccess) {// 图上传完成
                    String hikPath = "";
                    String facePath = "";
                    if (myRecord.isHikStatus()) {
                        hikPath = "https://" + mOssConfig.getBucketName() + ".oss-cn-hangzhou.aliyuncs.com/" + mOssConfig.getObjectPath() + myRecord.getFileName() + "_hik.jpg";
                    }
                    if (myRecord.isFaceStatus()) {
                        facePath = "https://" + mOssConfig.getBucketName() + ".oss-cn-hangzhou.aliyuncs.com/" + mOssConfig.getObjectPath() + myRecord.getFileName() + "_face.jpg";
                    }

                    RemoteRecord recorde = new RemoteRecord(myRecord.getUser_key(),
                            facePath, hikPath, myRecord.getCreate_time());
                    syncRecord(owner, recorde, myRecord);
                }
            }
        };

        if (myRecord.isHikStatus()) {
            Log.i("ebq", "记录：海康上传，图片路径：" + EBQValue.HIK_PATH + myRecord.getFileName() + "_hik.jpg");
            final File mFile = new File(EBQValue.HIK_PATH + myRecord.getFileName() + "_hik.jpg");
            if (mFile.exists()) {
                Log.w("ebq", "记录：海康上传，图片的确存在,开始执行图片压缩");
                Luban.with(context).load(mFile)
                        .setTargetDir(getPath())
                        .setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                                Log.w("ebq", "记录：海康，图片压缩开始");
                            }

                            @Override
                            public void onSuccess(File file) {
                                Log.w("ebq", "记录：海康，图片压缩成功：途径：" + file.getAbsolutePath());
                                FileUtil.delFile(mFile);
                                asyncPutImage(myRecord.getFileName() + "_hik.jpg", file.getAbsolutePath(), myRecord);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i("ebq", "记录：海康图片压缩失败，删除本条记录");
                                faceRepository.delete(myRecord);
                            }
                        }).launch();
            } else {
                Log.i("ebq", "记录：海康图片不存在，数据有误删除数据");
                faceRepository.delete(myRecord);
            }
        }
        if (myRecord.isFaceStatus()) {
            Log.i("ebq", "记录：送检图片，图片路径：" + EBQValue.CAPTURE_PATH + myRecord.getFileName() + "_face.jpg");
            asyncPutImage(myRecord.getFileName() + "_face.jpg", EBQValue.CAPTURE_PATH + myRecord.getFileName() + "_face.jpg", myRecord);
        }
    }

    //----------------人脸注册
//    private void registerFace(String userKey, String userName, DbOption data) {
//
//        ACache cache = ACache.get(app);
//        String sDelay = cache.getAsString(Key.KEY_DELAY_REGISTER);
//        int delay = 3000;
//        try {
//            if (sDelay != null) {
//                delay = Integer.parseInt(sDelay);
//            }
//
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//            delay = 3000;
//        }
//        Disposable disposable = Flowable.just(data)
//                .delay(delay, TimeUnit.MILLISECONDS)
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .subscribe(new Consumer<DbOption>() {
//                    @Override
//                    public void accept(DbOption dbOption) throws Exception {
//                        File picPath = new File(EBQValue.REGISTER_PATH, userKey + ".jpg");
//                        Bitmap bitmap = BitmapFactory.decodeFile(picPath.getAbsolutePath());
//                        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
//                        FaceInfo[] faceInfos = new FaceInfo[1];
//
//                        FaceEnvironment environment = new FaceEnvironment();
//                        environment.detectInterval = environment.trackInterval = 0;
//                        FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);
//
//                        if (argbImg.width * argbImg.height <= 1000 * 1000) {
//                            byte[] bytes = new byte[512];
//                            float ret = -1;
//                            ret = FaceApi.getInstance().getFeature(argbImg, bytes,
//                                    FaceFeature.FeatureType.FEATURE_VIS, environment, faceInfos);
//                            FaceInfo faceInfo = faceInfos[0];
//                            if (ret == -1) {
//                                //失败
//                                Log.e("ebq", "人脸注册：失败！！！=-1");
//                            } else if (ret == 128) {
//                                Bitmap cropBitmap = null;
//                                String cropImgName = null;
//                                // 人脸抠图
//                                if (faceInfo != null) {
//                                    cropBitmap = ImageUtils.noBlackBoundImgCrop(faceInfo.landmarks,
//                                            argbImg.height, argbImg.width, argbImg.data);
//
//                                    if (cropBitmap == null) {
//                                        cropBitmap = bitmap;
//                                    }
//                                    cropImgName = "crop_" + userKey + ".jpg";
//                                }
//                                Feature feature = new Feature();
//                                feature.setGroupId("0");
//                                feature.setUserId(userKey);
//                                feature.setFeature(bytes);
//                                feature.setImageName(userKey + ".jpg");
//                                feature.setUserName(userName);
//                                feature.setCropImageName(cropImgName);
//
//                                // 保存数据库
//                                if (FaceApi.getInstance().featureAdd(feature)) {
//                                    Log.e("ebq", "人脸注册：添加成功！！！");
//                                    // 保存图片到新目录中
//                                    File facePicDir = FileUtils.getFacePicDirectory();
//                                    // 保存抠图图片到新目录中
//                                    File faceCropDir = FileUtils.getFaceCropPicDirectory();
//
//                                    if (facePicDir != null) {
//                                        File savePicPath = new File(facePicDir, userKey + ".jpg");
//                                        if (FileUtils.saveFile(savePicPath, bitmap)) {
//                                            Log.i("ebq", "人脸库：人脸注册成功");
//                                        }
//                                    }
//
//                                    if (faceCropDir != null && cropBitmap != null) {
//                                        File saveCropPath = new File(faceCropDir, cropImgName);
//                                        if (FileUtils.saveFile(saveCropPath, cropBitmap)) {
//                                            if (cropBitmap != null && !cropBitmap.isRecycled()) {
//                                                cropBitmap.recycle();
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    Log.e("ebq", "人脸注册：添加失败！！！");
//                                }
//                            }
//                        } else {
//                            Log.e("ebq", "人脸注册：失败！！！图片太大超过了1000*1000");
//                            // 失败，图片太大 超过了1000*1000
//                        }
//
//                        if (bitmap != null) {
//                            if (!bitmap.isRecycled()) {
//                                bitmap.recycle();
//                            }
//                        }
//
//                        // 还原检测参数配置
//                        environment.detectInterval = 200;
//                        environment.trackInterval = 1000;
//                        FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);
//
//                        FaceSDKManager.getInstance().getFeatureLRUCache().clear();
//                        int i = FaceSDKManager.getInstance().setFeature();
//
//                        File oldFile = new File(EBQValue.REGISTER_PATH + userKey + ".jpg");
//                        if (oldFile.exists()) {
//                            boolean delStatus = oldFile.delete();
//                            Log.e("ebq", "**事务**：注册到人脸库，不管失败与否，没啥用了删了吧，是否删除成功：" + delStatus);
//                        }
//                        Log.i("ebq", "人脸库：新增之后----当前人脸库数据：" + i + "条");
//                        // 不管成功与否，反正此任务都需要被删除了不然留着没有用
//                        faceRepository.delete(data);
//
//                        handler.sendEmptyMessage(MONOCULAR_RESUME);
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        File oldFile = new File(EBQValue.REGISTER_PATH + userKey + ".jpg");
//                        if (oldFile.exists()) {
//                            boolean delStatus = oldFile.delete();
//                            Log.e("ebq", "**事务**：注册到人脸库，不管失败与否，没啥用了删了吧，是否删除成功：" + delStatus);
//                        }
//                        faceRepository.delete(data);
//
//                        Log.e("ebq", "**Rx事务**：发生了错误：" + throwable.getMessage());
//
//                        handler.sendEmptyMessage(MONOCULAR_RESUME);
//                    }
//                });
//
//    }

    private void batchRegisterFace() {
        if (importUserList.isEmpty())
            return;

        handler.sendEmptyMessage(MONOCULAR_PAUSE);
        isRegister.postValue(true);
        ACache cache = ACache.get(app);
        String sDelay = cache.getAsString(Key.KEY_DELAY_REGISTER);
        int delay = 3000;
        try {
            if (sDelay != null) {
                delay = Integer.parseInt(sDelay);
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            delay = 3000;
        }
        Disposable disposable = Flowable.just(importUserList)
                .delay(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<ImportUser>>() {
                    @Override
                    public void accept(List<ImportUser> importUsers) throws Exception {

                        FaceEnvironment environment = new FaceEnvironment();
                        environment.detectInterval = environment.trackInterval = 0;

                        for (int i = 0; i < importUserList.size(); i++) {
                            File picPath = new File(EBQValue.REGISTER_PATH, importUserList.get(i).getUserKey() + ".jpg");
                            Bitmap bitmap = BitmapFactory.decodeFile(picPath.getAbsolutePath());
                            ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                            FaceInfo[] faceInfos = new FaceInfo[1];

                            FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);

                            if (argbImg.width * argbImg.height <= 1000 * 1000) {
                                byte[] bytes = new byte[512];
                                float ret = -1;
                                ret = FaceApi.getInstance().getFeature(argbImg, bytes,
                                        FaceFeature.FeatureType.FEATURE_VIS, environment, faceInfos);
                                FaceInfo faceInfo = faceInfos[0];
                                if (ret == -1) {
                                    //失败
                                    Log.e("ebq", "人脸注册：失败！！！=-1");
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
                                        cropImgName = "crop_" + importUserList.get(i).getUserKey() + ".jpg";
                                    }
                                    Feature feature = new Feature();
                                    feature.setGroupId("0");
                                    feature.setUserId(importUserList.get(i).getUserKey());
                                    feature.setFeature(bytes);
                                    feature.setImageName(importUserList.get(i).getUserKey() + ".jpg");
                                    feature.setUserName(importUserList.get(i).getUserName());
                                    feature.setCropImageName(cropImgName);

                                    // 保存数据库
                                    if (FaceApi.getInstance().featureAdd(feature)) {
                                        Log.e("ebq", "人脸注册：添加成功！！！");
                                        // 保存图片到新目录中
                                        File facePicDir = FileUtils.getFacePicDirectory();
                                        // 保存抠图图片到新目录中
                                        File faceCropDir = FileUtils.getFaceCropPicDirectory();

                                        if (facePicDir != null) {
                                            File savePicPath = new File(facePicDir, importUserList.get(i).getUserKey() + ".jpg");
                                            if (FileUtils.saveFile(savePicPath, bitmap)) {
                                                Log.i("ebq", "人脸库：人脸注册成功");
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
                                    } else {
                                        Log.e("ebq", "人脸注册：添加失败！！！");
                                    }
                                }
                            } else {
                                Log.e("ebq", "人脸注册：失败！！！图片太大超过了1000*1000");
                                // 失败，图片太大 超过了1000*1000
                            }

                            if (bitmap != null) {
                                if (!bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }
                            }

                            File oldFile = new File(EBQValue.REGISTER_PATH + importUserList.get(i).getUserKey() + ".jpg");
                            if (oldFile.exists()) {
                                boolean delStatus = oldFile.delete();
                                Log.e("ebq", "**事务**：注册到人脸库，不管失败与否，没啥用了删了吧，是否删除成功：" + delStatus);
                            }

                        }

                        // 还原检测参数配置
                        environment.detectInterval = 200;
                        environment.trackInterval = 1000;
                        FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);

                        FaceSDKManager.getInstance().getFeatureLRUCache().clear();
                        int i = FaceSDKManager.getInstance().setFeature();
                        Log.i("ebq", "人脸库：新增之后----当前人脸库数据：" + i + "条");


                        importUserList.clear();
                        isRegister.postValue(false);
                        handler.sendEmptyMessage(MONOCULAR_RESUME);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        for (int i = 0; i < importUserList.size(); i++) {
                            File oldFile = new File(EBQValue.REGISTER_PATH + importUserList.get(i).getUserKey() + ".jpg");
                            if (oldFile.exists()) {
                                boolean delStatus = oldFile.delete();
                                Log.e("ebq", "**事务**：注册到人脸库，失败了，没啥用删了吧，是否删除成功：" + delStatus);
                            }
                        }
                        Log.e("ebq", "**Rx事务**：发生了错误：" + throwable.getMessage());

                        importUserList.clear();
                        isRegister.postValue(false);
                        handler.sendEmptyMessage(MONOCULAR_RESUME);
                    }
                });

    }

    // 人脸库中移除
    private void delFaceFeature(String userKey, String name, DbOption data) {

        faceRepository.getAppExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Feature> featureList = DBManager.getInstance().queryFeatureByName(name);
                if (featureList != null && featureList.size() > 0) {
                    for (int i = 0; i < featureList.size(); i++) {
                        Feature feature = featureList.get(i);
                        if (feature.getUserId().equals(userKey)) {// 需要删除
                            FaceApi.getInstance().featureDelete(feature);
                            FaceSDKManager.getInstance().getFeatureLRUCache().clear();
                        }
                    }
                }

                int i = FaceSDKManager.getInstance().setFeature();
                Log.i("ebq", "人脸库：删除之后----当前人脸库数据：" + i + "条");
                faceRepository.delete(data);
            }
        });
    }

    // 上传记录
    public void syncRecord(LifecycleOwner owner, RemoteRecord remoteRecord, MyRecord myRecord) {
        Log.w("ebq", "上传记录：开始上传记录");
        List<RemoteRecord> list = new ArrayList<>();
        list.add(remoteRecord);
        Remote remote = new Remote(list);
        faceRepository.syncRecord(remote).observe(owner, new Observer<Resource<BaseResponse<Object>>>() {
            @Override
            public void onChanged(@Nullable Resource<BaseResponse<Object>> data) {
                if (data != null) {
                    switch (data.status) {
                        case SUCCESS:
                            Log.w("ebq", "上传记录：上传记录成功");
                            faceRepository.delete(myRecord);
                            break;
                        case ERROR:
                            faceRepository.delete(myRecord);
                            break;
                        case LOADING:
                            break;
                    }
                }
            }
        });
    }

    // 更新
    public void update(LifecycleOwner owner) {
        faceRepository.requestData().observe(owner, new Observer<Resource<BaseResponse<ExData>>>() {
            @Override
            public void onChanged(@Nullable Resource<BaseResponse<ExData>> data) {
                if (data != null) {
                    switch (data.status) {
                        case LOADING:
                            break;
                        case ERROR:
                            break;
                        case SUCCESS:
                            if (data.data != null
                                    && data.data.getData() != null
                                    && data.data.getData().getUsers() != null) {

                                DbUserOption mData = data.data.getData().getUsers();
                                List<DbOption> list = new ArrayList<>();
                                int addCount = 0;
                                int delCount = 0;

                                if (mData.getAdd() != null) {

                                    for (DbOption item : mData.getAdd()) {
                                        if (!TextUtils.isEmpty(item.getFace_image())) {
                                            DbOption mDbOption = new DbOption(
                                                    item.getData_key(),
                                                    item.getUser_key(),
                                                    item.getReal_name(),
                                                    item.getFace_image(),
                                                    0,
                                                    item.getFull_name(),
                                                    item.getType_flag());// 新增用户
                                            list.add(mDbOption);
                                            addCount++;
                                        }
                                    }

                                }

                                if (mData.getDelete() != null) {

                                    for (DbOption item : mData.getDelete()) {
                                        DbOption mDbOption = new DbOption(
                                                item.getData_key(),
                                                item.getUser_key(),
                                                item.getReal_name(),
                                                "",
                                                1,
                                                item.getFull_name(),
                                                item.getType_flag());
                                        list.add(mDbOption);
                                        delCount++;
                                    }

                                }
                                Log.i("ebq", "数据更新:来源->融云更新------新增数据:" + addCount + "条 ; 删除数据:" + delCount + "条");
                                insert(list);
                            }
                            break;
                    }
                }
            }
        });
    }

    private String getPath() {
        String path = EBQValue.HIK_PATH;
        File f = new File(path);
        if (f.mkdirs()) {
            Log.w("ebq", "海康压缩图片路径：" + path);
            return path;
        }
        return path;
    }
}
