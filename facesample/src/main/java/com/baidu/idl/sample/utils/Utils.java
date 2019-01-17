package com.baidu.idl.sample.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by litonghui on 2018/5/11.
 */

public class Utils {
    public static final String TAG = "file-face";

    public static void hideKeyboard(Activity context) {
        if (context != null) {
            View viewFocus = context.getCurrentFocus();
            if (viewFocus != null) {
                InputMethodManager imManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imManager.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            }
        }
    }

    public static boolean saveBitmapToFile(String savePath, Bitmap bitmap) {
        boolean result = false;
        FileOutputStream out = null;

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "BDFace");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File f = new File(dir, savePath);
        if (f.exists()) {
            f.delete();
        }
        try {
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static boolean saveStringToFile(String savePath, String landmark) {
        boolean result = false;
        FileOutputStream out = null;

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "BDFace");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File f = new File(dir, savePath);
        try {
            out = new FileOutputStream(f, true);
            out.write(landmark.getBytes());
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String saveToFile(File dir, String fileName, byte[] content) {
        try {
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir, fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getFromFile(String fileName) {
        byte[] content = new byte[2048];
        File file = new File(fileName);
        try {
            FileInputStream stream = new FileInputStream(file);
            stream.read(content);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static Bitmap getBitmapFromFile(String fileName) {
        File file = new File(fileName);
        try {
            FileInputStream stream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            return bitmap;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, String imageName) {
        Bitmap bmp = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(imageName);
            bmp = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    public static Bitmap getBitmap(String imageName) {
        Bitmap bmp = null;
        File file = new File(imageName);
        try {
            FileInputStream stream = new FileInputStream(file);
            bmp = BitmapFactory.decodeStream(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    public static Bitmap getBitmap(int[] pixels, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static byte[] getByte(Context context, String imageName) {
        try {
            InputStream is = context.getAssets().open(imageName);
            byte[] fileBytes = new byte[is.available()];
            is.read(fileBytes);
            is.close();
            return fileBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void printfByte(byte[] content) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte c : content) {
            stringBuilder.append(c).append(" ");
        }
        Log.e(TAG, stringBuilder.toString());
    }

    public static ArrayList<File> refreshFileList(String strPath) {
        ArrayList<File> fileList = new ArrayList<>();
        File dir = new File(strPath);
        File[] files = dir.listFiles();
        if (null == files) {
            return null;
        }

        for (int i = 0, size = files.length; i < size; i++) {
            // 如果是文件夹
            if (files[i].isDirectory()) {
                // 遍历此路径，执行此方法
                ArrayList<File> refreshFileList = refreshFileList(files[i].getAbsolutePath());
                if (null != refreshFileList) {
                    fileList.addAll(refreshFileList);
                }
            } else {
                // 添加到文件列表
                fileList.add(files[i]);
            }
        }
        return fileList;
    }

    public static ArrayList<File> listFiles(String strPath) {
        return refreshFileList(strPath);
    }

    /**
     * 8位灰度转Bitmap
     * <p>
     * 图像宽度必须能被4整除
     *
     * @param data   裸数据
     * @param width  图像宽度
     * @param height 图像高度
     * @return
     */
    public static Bitmap convert8bit(byte[] data, int width, int height) {
        byte[] Bits = new byte[data.length * 4]; //RGBA 数组

        int i;
        for (i = 0; i < data.length; i++) {
            // 原理：4个字节表示一个灰度，则RGB  = 灰度值，最后一个Alpha = 0xff;
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = data[i];
            Bits[i * 4 + 3] = -1; //0xff
        }

        // Bitmap.Config.ARGB_8888 表示：图像模式为8位
        Bitmap bmp = Bitmap
                .createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

        return bmp;
    }

    public static void drawShape(int[] shape, Bitmap img) {
        // draw lines
        int Pointcolor = Color.GREEN;
        int LineColor = Color.BLUE;

        Canvas canvas = new Canvas(img);
        Paint paint = new Paint();
        paint.setStrokeWidth(4);
        paint.setColor(LineColor);

        if (shape.length == 144) {
            int nComponents = 9;

            int[] comp1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            int[] comp2 = {13, 14, 15, 16, 17, 18, 19, 20, 13, 21};
            int[] comp3 = {22, 23, 24, 25, 26, 27, 28, 29, 22};
            int[] comp4 = {30, 31, 32, 33, 34, 35, 36, 37, 30, 38};
            int[] comp5 = {39, 40, 41, 42, 43, 44, 45, 46, 39};
            int[] comp6 = {47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 47};
            int[] comp7 = {51, 57, 52};
            int[] comp8 = {58, 59, 60, 61, 62, 63, 64, 65, 58};
            int[] comp9 = {58, 66, 67, 68, 62, 69, 70, 71, 58};
            int[] nPoints = {13, 10, 9, 10, 9, 11, 3, 9, 9};
            int[][] idx = {comp1, comp2, comp3, comp4, comp5, comp6, comp7, comp8, comp9};

            for (int i = 0; i < nComponents; ++i) {
                for (int j = 0; j < nPoints[i] - 1; ++j) {
                    canvas.drawLine(shape[idx[i][j] << 1], shape[1 + (idx[i][j] << 1)],
                            shape[idx[i][j + 1] << 1], shape[1 + (idx[i][j + 1] << 1)], paint);
                }
            }
        }

        paint.setStrokeWidth(6);
        paint.setColor(Pointcolor);
        // draw landmark points
        for (int i = 0; i < shape.length / 2; ++i) {
            canvas.drawCircle(shape[i << 1], shape[1 + (i << 1)], 2, paint);
        }
    }
}
