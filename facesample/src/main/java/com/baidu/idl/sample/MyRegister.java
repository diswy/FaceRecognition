package com.baidu.idl.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.baidu.idl.sample.api.FaceApi;
import com.baidu.idl.sample.common.FaceEnvironment;
import com.baidu.idl.sample.manager.FaceSDKManager;
import com.baidu.idl.sample.model.ARGBImg;
import com.baidu.idl.sample.utils.FeatureUtils;

import java.io.File;

/**
 * Created by @author xiaofu on 2019/5/10.
 */
public class MyRegister {
    public static void registerFace(String userKey, String userName, String filePath, String fileName) {
        System.out.println("----------!@#");
        File picPath = new File(filePath, fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(picPath.getAbsolutePath());
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        FaceInfo[] faceInfos = new FaceInfo[1];

        System.out.println("----------!@#="+picPath.getAbsolutePath());


        FaceEnvironment environment = new FaceEnvironment();
        environment.detectInterval = environment.trackInterval = 0;
        FaceSDKManager.getInstance().getFaceDetector().loadConfig(environment);

        // 检测人脸，提取人脸特征值
        byte[] bytes2 = new byte[512];
        System.out.println("-----执行到这");
        try {
            float ret2 = FaceApi.getInstance().getFeature(argbImg, bytes2,
                    FaceFeature.FeatureType.FEATURE_VIS, environment, faceInfos);
            System.out.println("---------------------live_photo=" + ret2);
        }catch (Exception e){
            System.out.println("-------->>>"+e.getMessage());
        }

        System.out.println("-----执行到这2");
//        FaceInfo faceInfo = faceInfos[0];
//        System.out.println("---------------------live_photo=" + ret2);
        return;
//
//        if (argbImg.width * argbImg.height <= 1000 * 1000) {
//            byte[] bytes = new byte[512];
//            float ret = -1;
//            ret = FaceApi.getInstance().getFeature(argbImg, bytes,
//                    FaceFeature.FeatureType.FEATURE_VIS, environment, faceInfos);
//            FaceInfo faceInfo = faceInfos[0];
//            if (ret == -1) {
//                //失败
//            } else if (ret == 128) {
//                Bitmap cropBitmap = null;
//                String cropImgName = null;
//                // 人脸抠图
//                if (faceInfo != null) {
//                    cropBitmap = ImageUtils.noBlackBoundImgCrop(faceInfo.landmarks,
//                            argbImg.height, argbImg.width, argbImg.data);
//
//                    if (cropBitmap == null) {
//                        cropBitmap = bitmap;
//                    }
//                    cropImgName = "crop_" + fileName;
//                }
//                Feature feature = new Feature();
//                feature.setGroupId("0");
//                feature.setUserId(userKey);
//                feature.setFeature(bytes);
//                feature.setImageName(fileName);
//                feature.setUserName(userName);
//                feature.setCropImageName(cropImgName);
//
//                // 保存数据库
//                if (FaceApi.getInstance().featureAdd(feature)) {
//
//                    System.out.println("--->>>人脸注册成功");
//
////                    success = true;
//                    // 保存图片到新目录中
//                    File facePicDir = FileUtils.getFacePicDirectory();
//                    // 保存抠图图片到新目录中
//                    File faceCropDir = FileUtils.getFaceCropPicDirectory();
//
//                    if (facePicDir != null) {
//                        File savePicPath = new File(facePicDir, fileName);
//                        if (FileUtils.saveFile(savePicPath, bitmap)) {
//                            System.out.println("--->>>人脸注册成功->保存成功");
//
//                        }
//                    }
//
//                    if (faceCropDir != null && cropBitmap != null) {
//                        File saveCropPath = new File(faceCropDir, cropImgName);
//                        if (FileUtils.saveFile(saveCropPath, cropBitmap)) {
//                            if (cropBitmap != null && !cropBitmap.isRecycled()) {
//                                cropBitmap.recycle();
//                            }
//                        }
//                    }
//                }
//
//            }
//        } else {
//            // 失败，图片太大 超过了1000*1000
//        }
//        if (!bitmap.isRecycled()) {
//            bitmap.recycle();
//        }
//
    }
}
