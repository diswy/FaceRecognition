package com.baidu.idl.sample.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.common.FaceEnvironment;
import com.baidu.idl.sample.db.DBManager;
import com.baidu.idl.sample.listener.OnImportListener;
import com.baidu.idl.sample.model.ARGBImg;
import com.baidu.idl.sample.utils.FeatureUtils;
import com.baidu.idl.sample.utils.FileUtils;
import com.baidu.idl.sample.utils.ImageUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 导入相关管理类
 * Created by v_liujialu01 on 2018/12/5.
 */

public class ImportFileManager {
    private static final String TAG = "ImportFileManager";

    private static ImportFileManager single = null;

    // 是否关闭导入
    private volatile boolean mImporting;
    private int mTotalCount;
    private int mFinishCount;
    private int mSuccessCount;
    private int mFailCount;

    private OnImportListener mImportListener;
    private Future mFuture;
    private ExecutorService mExecutorService;

    // 私有构造
    private ImportFileManager() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public static ImportFileManager getInstance() {
        if (single == null) {
            synchronized (ImportFileManager.class) {
                if (single == null) {
                    single = new ImportFileManager();
                }
            }
        }
        return single;
    }

    public void setOnImportListener(OnImportListener importListener) {
        mImportListener = importListener;
    }

    public void setImport(boolean importing) {
        mImporting = importing;
    }

    /**
     * 批量导入
     */
    public void batchImport() {
        // 获取导入目录 /sdcard/Import Faces
        File batchFaceDir = FileUtils.getBatchFaceDirectory();
        String[] files = batchFaceDir.list();
        if (files == null || files.length == 0) {
            if (mImportListener != null) {
                mImportListener.showToastMessage("导入数据的文件夹没有数据");
            }
            return;
        }

        // 获取导入图片目录 /sdcard/Import Faces/FacePictures
        File batchPicDir = FileUtils.getBatchFacePicDirectory();
        String[] picFiles = batchPicDir.list();
        if (picFiles == null || picFiles.length == 0) {
            if (mImportListener != null) {
                mImportListener.showToastMessage("导入图片的文件夹没有图片");
            }
            return;
        }

        // 获取txt文件目录 /sdcard/Import Faces/Faces List.txt
        File txtDir = FileUtils.getFaceTxtDirectory(batchFaceDir.getPath(), "Faces List.txt");
        if (txtDir == null) {
            if (mImportListener != null) {
                mImportListener.showToastMessage("导入数据的文件夹没有txt文件");
            }
            return;
        }

        // 开启线程导入
        asyncImport(picFiles, batchPicDir, txtDir);
    }

    private void asyncImport(final String[] picFiles, final File batchPicDir,
                             final File txtDir) {
        mImporting = true;
        mTotalCount = picFiles.length;
        mFinishCount = 0;
        mSuccessCount = 0;
        mFailCount = 0;
        // 开始导入
        if (mImportListener != null) {
            mImportListener.startImport();
        }

        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }

        mFuture = mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                // 读取导入txt文件的内容
                String contents = FileUtils.readTxtFile(txtDir);
                if (contents == null) {
                    return;
                }

                String[] contentLines = contents.split("\\n");
                int lineNums = contentLines.length;  // 需要减去标题
//                int picNums = picFiles.length;
//                if (lineNums != picNums) {
//                    if (mImportListener != null) {
//                        mImportListener.showToastMessage("人脸信息数与图片数不一致");
//                    }
//                    return;
//                }

                // 日志，导入结束之后，会输出log.txt文件
                StringBuilder logBuilder = new StringBuilder();
                logBuilder.append("姓名\t图片名\t成功/失败\t失败原因\n");
                // Log.i(TAG, "picNums = " + picNums);
                FaceEnvironment environment = new FaceEnvironment();
                environment.detectInterval = environment.trackInterval = 0;
                FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);

                for (int i = 0; i < lineNums; i++) {
                    if (!mImporting) {
                        break;
                    }

                    String[] lineTxt = contentLines[i].split(" ");
                    String picFile = lineTxt[1];  // 图片名
                    String userName = lineTxt[0]; // 姓名
                    boolean success = false;

                    // 判断姓名是否有效
                    String nameResult = FaceApi.getInstance().isValidName(userName);
                    if (!"0".equals(nameResult)) {
                        // 保存失败信息
                        logBuilder.append(userName + "\t" + picFile + "\t"
                                + "失败" + "\t" + nameResult + "\n");
                        mFinishCount++;
                        mFailCount++;
                        // 更新进度
                        if (mFinishCount % 100 == 0) {
                            if (mImportListener != null) {
                                mImportListener.onImporting((float) mFinishCount / (float) mTotalCount);
                            }
                        }
                        continue;
                    }

                    // 根据姓名查询数据库与文件中对应的姓名是否相等，如果相等，则直接过滤
                    List<Feature> listFeatures = DBManager.getInstance().queryFeatureByName(userName);
                    if (listFeatures != null && listFeatures.size() > 0) {
                        String msg = "与之前图片对应的姓名相同";
                        // 保存失败信息
                        logBuilder.append(userName + "\t" + picFile + "\t"
                                + "失败" + "\t" + msg + "\n");
                        mFinishCount++;
                        mFailCount++;
                        // 更新进度
                        if (mFinishCount % 100 == 0) {
                            if (mImportListener != null) {
                                mImportListener.onImporting((float) mFinishCount / (float) mTotalCount);
                            }
                        }
                        continue;
                    }

                    // 获取图片路径
                    File picPath = new File(batchPicDir, picFile);

                    // 判断txt文档中的图片是否在路径下存在
                    if (picPath.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(picPath.getAbsolutePath());
                        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                        FaceInfo[] faceInfos = new FaceInfo[1];

                        if (argbImg.width * argbImg.height <= 1000 * 1000) {
                            byte[] bytes = new byte[512];
                            float ret = -1;
                            // 检测人脸，提取人脸特征值
                            ret = FaceApi.getInstance().getFeature(argbImg, bytes,
                                    FaceFeature.FeatureType.FEATURE_VIS, environment, faceInfos);
                            FaceInfo faceInfo = faceInfos[0];
                            Log.e(TAG, "live_photo = " + ret);

                            if (ret == -1) {
                                Log.e(TAG, picFile + "未检测到人脸，可能原因：人脸太小");
                                String msg = "未检测到人脸，可能原因：人脸太小"
                                        + "（必须大于最小检测人脸minFaceSize），"
                                        + "或者人脸角度太大，人脸不是朝上";
                                logBuilder.append(userName + "\t" + picFile + "\t"
                                        + "失败" + "\t" + msg + "\n");
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
                                    cropImgName = "crop_" + picFile;
                                }
                                Feature feature = new Feature();
                                feature.setGroupId("0");
                                final String uid = UUID.randomUUID().toString();
                                feature.setUserId(uid);
                                feature.setFeature(bytes);
                                feature.setImageName(picFile);
                                feature.setUserName(userName);
                                feature.setCropImageName(cropImgName);

                                // 保存数据库
                                if (FaceApi.getInstance().featureAdd(feature)) {
                                    success = true;
                                    // 保存图片到新目录中
                                    File facePicDir = FileUtils.getFacePicDirectory();
                                    // 保存抠图图片到新目录中
                                    File faceCropDir = FileUtils.getFaceCropPicDirectory();

                                    if (facePicDir != null) {
                                        File savePicPath = new File(facePicDir, picFile);
                                        if (FileUtils.saveFile(savePicPath, bitmap)) {
                                            Log.i(TAG, "图片保存成功");
                                        }
                                    }

                                    if (faceCropDir != null && cropBitmap != null) {
                                        File saveCropPath = new File(faceCropDir, cropImgName);
                                        if (FileUtils.saveFile(saveCropPath, cropBitmap)) {
                                            Log.i(TAG, "抠图图片保存成功");
                                            if (cropBitmap != null && !cropBitmap.isRecycled()) {
                                                cropBitmap.recycle();
                                            }
                                        }
                                    }
                                    logBuilder.append(userName + "\t" + picFile + "\t" + "成功\n");
                                }
                            } else {
                                Log.e(TAG, picFile + "未检测到人脸");
                                logBuilder.append(userName + "\t" + picFile + "\t"
                                        + "失败" + "\t" + "未检测到人脸\n");
                            }
                        } else {
                            Log.e(TAG, picFile + "该图片尺寸超过了最大尺寸(1000 * 1000)");
                            String msg = "该图片尺寸超过了最大尺寸(1000 * 1000)";
                            logBuilder.append(userName + "\t" + picFile + "\t"
                                    + "失败" + "\t" + msg + "\n");
                        }
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    } else {
                        Log.e(TAG, picFile + "未查到“Faces List”文件中对应的图片");
                        String msg = "未查到“Faces List”文件中对应的图片";
                        logBuilder.append(userName + "\t" + picFile + "\t"
                                + "失败" + "\t" + msg + "\n");
                    }
                    if (success) {
                        mSuccessCount++;
                    } else {
                        mFailCount++;
                        Log.e(TAG, "失败图片:" + picFile);
                    }
                    mFinishCount++;
                    // 导入中（用来显示进度）
                    Log.i(TAG, "mFinishCount = " + mFinishCount + " progress = " + ((float) mFinishCount / (float) mTotalCount));

                    if (mFinishCount % 20 == 0) {
                        if (mImportListener != null) {
                            mImportListener.onImporting((float) mFinishCount / (float) mTotalCount);
                        }
                    }
                }

                Log.i(TAG, "总人脸数:" + mTotalCount + ", 完成：" + mFinishCount
                        + " 成功:" + mSuccessCount + " 失败:" + mFailCount);

                // 保存日志文件
                File newFile = FileUtils.getFaceDirectory();
                FileUtils.writeTxtToFile(newFile.getPath(), logBuilder.toString(), "log.txt");

                // 还原检测参数配置
                environment.detectInterval = 200;
                environment.trackInterval = 500;
                FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);

                // 导入完成（弹出导入完成信息框）
                if (mImportListener != null) {
                    mImportListener.endImport(mFinishCount, mSuccessCount, mFailCount);
                }
            }
        });
    }

    // 释放，关闭线程
    public void release() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }

        if (mExecutorService != null) {
            mExecutorService = null;
        }
    }
}
