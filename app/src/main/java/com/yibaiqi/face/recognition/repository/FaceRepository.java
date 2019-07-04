package com.yibaiqi.face.recognition.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.yibaiqi.face.recognition.AppExecutors;
import com.yibaiqi.face.recognition.Key;
import com.yibaiqi.face.recognition.db.UserDao;
import com.yibaiqi.face.recognition.network.ApiResponse;
import com.yibaiqi.face.recognition.network.ApiService;
import com.yibaiqi.face.recognition.network.NetworkResource;
import com.yibaiqi.face.recognition.tools.ACache;
import com.yibaiqi.face.recognition.vo.BaseResponse;
import com.yibaiqi.face.recognition.vo.DbClassOptionContent;
import com.yibaiqi.face.recognition.vo.DbLeaveOptionContent;
import com.yibaiqi.face.recognition.vo.DbOption;
import com.yibaiqi.face.recognition.vo.DeviceName;
import com.yibaiqi.face.recognition.vo.ExData;
import com.yibaiqi.face.recognition.vo.GlobalConfig;
import com.yibaiqi.face.recognition.vo.Leaves;
import com.yibaiqi.face.recognition.vo.LocalUser;
import com.yibaiqi.face.recognition.vo.MyRecord;
import com.yibaiqi.face.recognition.vo.OSSKey;
import com.yibaiqi.face.recognition.vo.RegisterDevice;
import com.yibaiqi.face.recognition.vo.Remote;
import com.yibaiqi.face.recognition.vo.RemoteRecord;
import com.yibaiqi.face.recognition.vo.Resource;
import com.yibaiqi.face.recognition.vo.SettingContent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by @author xiaofu on 2019/4/23.
 */
@Singleton
public class FaceRepository {
    private final ApiService service;
    private final AppExecutors appExecutors;
    private final UserDao userDao;
    private final ACache mCache;

    @Inject
    FaceRepository(ApiService service, AppExecutors appExecutors, UserDao userDao, ACache aCache) {
        this.service = service;
        this.appExecutors = appExecutors;
        this.userDao = userDao;
        this.mCache = aCache;
    }

    public ACache getCache() {
        return mCache;
    }

    public AppExecutors getAppExecutors() {
        return appExecutors;
    }

    /**
     * 获取设备的密钥
     */
    public LiveData<Resource<BaseResponse<RegisterDevice>>> registerDevice(String devId, String devName) {
        return new NetworkResource<BaseResponse<RegisterDevice>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<RegisterDevice>>> createCall() {
                return service.registerDevice(devId, devName);
            }
        }.asLiveData();
    }

    /**
     * 绑定设备
     */
    public LiveData<Resource<BaseResponse<String>>> bindDevice(String devId) {
        return new NetworkResource<BaseResponse<String>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<String>>> createCall() {
                return service.bindDevice(mCache.getAsString("token"), devId);
            }
        }.asLiveData();
    }

    /**
     * 上报异常
     */
    public LiveData<Resource<BaseResponse<String>>> uploadError(String userKey){
        return new NetworkResource<BaseResponse<String>>(appExecutors){
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<String>>> createCall() {
                return service.bindDevice(mCache.getAsString("token"),userKey);
            }
        }.asLiveData();
    }

    /**
     * 绑定设备
     */
    public Flowable<String> bindDevice2(String devId) {
        return service.bindDevice2(mCache.getAsString("token"), devId);
    }

    /**
     * 上传记录
     */
    public LiveData<Resource<BaseResponse<Object>>> syncRecord(Remote remote) {
        return new NetworkResource<BaseResponse<Object>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<Object>>> createCall() {
                return service.addRecord(mCache.getAsString("token"), remote);
            }
        }.asLiveData();
    }

    /**
     * 获取数据
     */
    public LiveData<Resource<BaseResponse<ExData>>> requestData() {
        return new NetworkResource<BaseResponse<ExData>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<ExData>>> createCall() {
                return service.requestData(mCache.getAsString("token"));
            }
        }.asLiveData();
    }

    /**
     * 获取闸机属性和名字
     */
    public LiveData<Resource<BaseResponse<DeviceName>>> getDevice() {
        return new NetworkResource<BaseResponse<DeviceName>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<DeviceName>>> createCall() {
                return service.getDevice(mCache.getAsString("token"));
            }
        }.asLiveData();
    }

    //-------------------数据库操作相关
    public void insert(List<DbOption> list) {
        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<DbOption>>() {
                    @Override
                    public void accept(List<DbOption> list) throws Exception {
                        if (list != null) {
                            userDao.insert(list);
                        }
                    }
                });

//        appExecutors.diskIO().execute(() -> {
//            if (list != null) {
//                userDao.insert(list);
//            }
//        });
    }

    public void update(List<DbOption> list) {
        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<DbOption>>() {
                    @Override
                    public void accept(List<DbOption> list) throws Exception {
                        if (list != null) {
                            userDao.update(list);
                        }
                    }
                });
//        appExecutors.diskIO().execute(() -> {
//            if (list != null) {
//                userDao.update(list);
//            }
//        });
    }

    public void delete(List<DbOption> list) {
        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<DbOption>>() {
                    @Override
                    public void accept(List<DbOption> list) throws Exception {
                        if (list != null) {
                            userDao.delete(list);
                        }
                    }
                });
//        appExecutors.diskIO().execute(() -> {
//            if (list != null) {
//                userDao.delete(list);
//            }
//        });
    }

    public void delete(DbOption data) {
        Disposable disposable = Flowable.just(data)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<DbOption>() {
                    @Override
                    public void accept(DbOption dbOption) throws Exception {
                        userDao.delete(data);
                    }
                });
//        appExecutors.diskIO().execute(() -> {
//            if (data != null) {
//                userDao.delete(data);
//            }
//        });
    }

    public void insert(MyRecord data) {
        Disposable disposable = Flowable.just(data)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<MyRecord>() {
                    @Override
                    public void accept(MyRecord myRecord) throws Exception {
                        userDao.insertRecord(data);
                    }
                });
//        appExecutors.diskIO().execute(() -> {
//            if (data != null) {
//                userDao.insertRecord(data);
//            }
//        });
    }

    public void delete(MyRecord data) {
        Disposable disposable = Flowable.just(data)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<MyRecord>() {
                    @Override
                    public void accept(MyRecord myRecord) throws Exception {
                        userDao.deleteRecord(data);
                    }
                });
//        appExecutors.diskIO().execute(() -> {
//            if (data != null) {
//                userDao.deleteRecord(data);
//            }
//        });
    }

    public LiveData<List<MyRecord>> observeRecordAll() {
        return userDao.observeRecordAll();
    }

    public LiveData<List<DbOption>> observeAll() {
        return userDao.observeAll();
    }


    public void userInsert(List<LocalUser> list) {
        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<LocalUser>>() {
                    @Override
                    public void accept(List<LocalUser> localUsers) throws Exception {
                        userDao.insertUsers(localUsers);
                    }
                });
    }

    public void userDel(List<LocalUser> list) {
        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<LocalUser>>() {
                    @Override
                    public void accept(List<LocalUser> localUsers) throws Exception {
                        userDao.deleteUsers(localUsers);
                    }
                });
    }

    public LocalUser getUserByKey(String key) {
        return userDao.getUser(key);
    }

    public UserDao getDao(){
        return userDao;
    }


    //------------摄像头
    public boolean isCameraEnable() {
        return mCache.getAsString(Key.KEY_CAMERA_ACCOUNT) != null
                && mCache.getAsString(Key.KEY_CAMERA_IP) != null
                && mCache.getAsString(Key.KEY_CAMERA_PORT) != null
                && mCache.getAsString(Key.KEY_CAMERA_PWD) != null;
    }

    public String getCameraIp() {
        return mCache.getAsString(Key.KEY_CAMERA_IP);
    }

    public int getCameraPort() {
        return Integer.parseInt(mCache.getAsString(Key.KEY_CAMERA_PORT));
    }

    public String getCameraAccount() {
        return mCache.getAsString(Key.KEY_CAMERA_ACCOUNT);
    }

    public String getCameraPwd() {
        return mCache.getAsString(Key.KEY_CAMERA_PWD);
    }

    //----------------OSS配置
    public LiveData<Resource<BaseResponse<OSSKey>>> getOSSConfig() {
        return new NetworkResource<BaseResponse<OSSKey>>(appExecutors) {
            @NonNull
            @Override
            protected LiveData<ApiResponse<BaseResponse<OSSKey>>> createCall() {
                return service.getOSSConfig(mCache.getAsString("token"));
            }
        }.asLiveData();
    }


    //------------------------2019/7/3/
    public void saveConfig(GlobalConfig config) {
        mCache.put("config_error_flag", String.valueOf(config.getError_flag()));
        mCache.put("config_setting_traffic_flag", String.valueOf(config.getSetting_traffic_flag()));
    }

    public void delClassCourse(List<DbClassOptionContent> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        Disposable disposable = Flowable.fromIterable(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<DbClassOptionContent>() {
                    @Override
                    public void accept(DbClassOptionContent data) throws Exception {
                        if (data.getType_flag() == 3) {
                            userDao.deleteClassCourse3(data.getClass_id(), data.getClass_course_id());
                        } else if (data.getType_flag() == 4) {
                            userDao.deleteClassCourse4(data.getClass_id(), data.getUser_key());
                        }
                    }
                });
    }

    public void insetClassCourse(List<DbClassOptionContent> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Disposable disposable = Flowable.fromIterable(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<DbClassOptionContent>() {
                    @Override
                    public void accept(DbClassOptionContent data) throws Exception {
                        if (data.getType_flag() == 1) {
                            userDao.insetClassCourse(data);
                        } else if (data.getType_flag() == 2) {
                            userDao.updateClass(data.getStart_time(), data.getEnd_time(), data.getClass_id(), data.getClass_course_id());
                        }
                    }
                });
    }

    public void delLeaves(List<DbLeaveOptionContent> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<DbLeaveOptionContent>>() {
                    @Override
                    public void accept(List<DbLeaveOptionContent> dbLeaveOptionContents) throws Exception {
                        userDao.deleteLeaves(dbLeaveOptionContents);
                    }
                });
    }

    public void insertLeaves(List<DbLeaveOptionContent> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<DbLeaveOptionContent>>() {
                    @Override
                    public void accept(List<DbLeaveOptionContent> dbLeaveOptionContents) throws Exception {
                        userDao.insertLeaves(dbLeaveOptionContents);
                    }
                });
    }

    public void delSettingsAndAdd(List<SettingContent> list){
        Disposable disposable = Flowable.just(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<SettingContent>>() {
                    @Override
                    public void accept(List<SettingContent> list) throws Exception {
                        userDao.delSettings();
                        userDao.insertSettings(list);
                    }
                });

    }

}
