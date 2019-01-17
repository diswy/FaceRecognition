/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.baidu.idl.sample.model.ARGBImg;

import java.io.InputStream;

public class FeatureUtils {

    public static byte[] bytes = new byte[2048];


    public static ARGBImg getFeatureByImagePath(String imageName, Context context, byte[] feature) {
        Bitmap bitmap = getBitmap(imageName, context);
        ARGBImg argbImg = getImageInfo(bitmap);
        return argbImg;
    }

    public static Bitmap getBitmap(String imageName, Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ARGBImg getARGBImgFromPath(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        ARGBImg argbImg = getImageInfo(bitmap);

        return argbImg;
    }


    public static ARGBImg getImageInfo(Bitmap bmp) {
        ARGBImg argbImg = new ARGBImg();
        if (bmp != null) {
            int[] argbData = new int[bmp.getWidth() * bmp.getHeight()];
            bmp.getPixels(argbData, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
            argbImg = new ARGBImg(argbData, bmp.getWidth(), bmp.getHeight(), 0, 0);
        }
        return argbImg;
    }


    /**
     * 打印flot数组
     *
     * @param floats
     */
    public static void logByteArray(byte[] floats) {
        StringBuilder featureInfo = new StringBuilder();
        featureInfo.setLength(0);
        for (int i = 0; i < floats.length; i++) {
            featureInfo.append(byteToBit(floats[i]) + ",");
        }
        Log.w("facefeature", featureInfo.toString());

        featureInfo.delete(0, featureInfo.length());
    }

    private static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }
}
