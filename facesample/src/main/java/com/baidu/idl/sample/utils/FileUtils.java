/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件工具类
 * Created by v_liujialu01 on 2018/11/28.
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Checks if is sd card available.
     *
     * @return true, if is sd card available
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Gets the SD root file.
     *
     * @return the SD root file
     */
    public static File getSDRootFile() {
        if (isSdCardAvailable()) {
            return Environment.getExternalStorageDirectory();
        } else {
            return null;
        }
    }

    /**
     * 获取新目录信息
     *
     * @return
     */
    public static File getFaceDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "new_faces");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取导入目录信息
     *
     * @return
     */
    public static File getBatchFaceDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "Import Faces");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取导入图片目录
     *
     * @return
     */
    public static File getBatchFacePicDirectory() {
        File batchFaceFile = getBatchFaceDirectory();
        File file = null;
        if (batchFaceFile != null && batchFaceFile.exists()) {
            file = new File(batchFaceFile, "FacePictures");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取新图片目录
     *
     * @return
     */
    public static File getFacePicDirectory() {
        File picFile = getFaceDirectory();
        File file = null;
        if (picFile != null && picFile.exists()) {
            file = new File(picFile, "FacePictures");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取抠图目录
     *
     * @return
     */
    public static File getFaceCropPicDirectory() {
        File picFile = getFaceDirectory();
        File file = null;
        if (picFile != null && picFile.exists()) {
            file = new File(picFile, "FaceCrop");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取txt文件目录
     */
    public static File getFaceTxtDirectory(String fileDirectory, String fileName) {
        File file = new File(fileDirectory + "/" + fileName);
        try {
            if (file.exists()) {
                return file;
            } else {
                file.createNewFile();
                return file;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 读取txt文件的内容
     */
    public static String readTxtFile(File file) {
        StringBuilder outText = null;
        try {
            outText = new StringBuilder();
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            // FileReader read = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                outText.append(lineTxt);
                outText.append("\n");
            }
            fis.close();
            isr.close();
            bufferedReader.close();
            return outText + "";
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeTxtToFile(String fileDirectory, String content, String fileName) {
        FileOutputStream fos = null;
        try {
            File file = getFaceTxtDirectory(fileDirectory, fileName);
            if (file != null) {
                fos = new FileOutputStream(file.getAbsolutePath());
                fos.write(content.getBytes());
                fos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean saveFile(File file, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 复制文件
     *
     * @param oldPath
     * @param newPath
     * @autor yangrui09
     */
    public static boolean copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        boolean result = false;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            // 判断目录是否存在
            File newfile = new File(newPath);
            File newFileDir = new File(newfile.getPath().replace(newfile.getName(), ""));
            if (!newFileDir.exists()) {
                newFileDir.mkdirs();
            }

            if (oldfile.exists()) { // 文件存在时
                inStream = new FileInputStream(oldPath); // 读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 从assets目录下拷贝整个文件夹，不管是文件夹还是文件都能拷贝
     * String inPath = "ImportSrc";
     * String outPath = Environment.getExternalStorageDirectory().getPath();
     * @param context 上下文
     */
    public static boolean copyAssetsFiles2SDCard(Context context, String inPath, String outPath) {
        if (context == null) {
            return false;
        }

        String[] fileNames = null;
        // 获得Assets一共有多少文件
        try {
            fileNames = context.getAssets().list(inPath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // 如果是目录
        if (fileNames.length > 0) {
            File fileOutDir = new File(outPath);
            if (fileOutDir.isFile()) {
                boolean ret = fileOutDir.delete();
                if (!ret) {
                    Log.e(TAG, "delete() FAIL:" + fileOutDir.getAbsolutePath());
                }
            }
            if (!fileOutDir.exists()) {      // 如果文件路径不存在
                if (!fileOutDir.mkdirs()) {  // 创建文件夹
                    Log.e(TAG, "mkdirs() FAIL:" + fileOutDir.getAbsolutePath());
                    return false;
                }
            }

            for (String fileName : fileNames) {   // 递归调用复制文件夹
                String inDir = inPath;
                String outDir = outPath + File.separator;
                if (!inPath.equals("")) {         // 空目录特殊处理下
                    inDir = inDir + File.separator;
                }
                copyAssetsFiles2SDCard(context, inDir + fileName, outDir + fileName);
            }
            return true;
        } else { // 如果是文件
            try {
                File fileOut = new File(outPath);
                if (fileOut.exists()) {
                    boolean ret = fileOut.delete();
                    if (!ret) {
                        Log.e(TAG, "delete() FAIL:" + fileOut.getAbsolutePath());
                    }
                }
                boolean ret = fileOut.createNewFile();
                if (!ret) {
                    Log.e(TAG, "createNewFile() FAIL:" + fileOut.getAbsolutePath());
                }
                FileOutputStream fos = new FileOutputStream(fileOut);
                InputStream is = context.getAssets().open(inPath);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush(); // 刷新缓冲区
                is.close();
                fos.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
