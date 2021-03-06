package com.yibaiqi.face.recognition.tools;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fujiayi on 2017/5/19.
 */

public class FileUtil {

    public static boolean delFile(File file) {
        boolean b = false;
        try {
            if (file.exists()) {
                b = file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    // 创建一个临时目录，用于复制临时文件，如assets目录下的离线资源文件
    public static String createTmpDir(Context context) {
        String sampleDir = "baiduTTS";
        String tmpDir = Environment.getExternalStorageDirectory().toString() + "/" + sampleDir;
        if (!FileUtil.makeDir(tmpDir)) {
            tmpDir = context.getExternalFilesDir(sampleDir).getAbsolutePath();
            if (!FileUtil.makeDir(sampleDir)) {
                throw new RuntimeException("create model resources dir failed :" + tmpDir);
            }
        }
        return tmpDir;
    }

    public static boolean fileCanRead(String filename) {
        File f = new File(filename);
        return f.canRead();
    }

    public static boolean makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }

    public static void copyFromAssets(AssetManager assets, String source, String dest, boolean isCover)
            throws IOException {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = assets.open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
        }
    }


    public static File saveBitmap(Bitmap bitmap, String fileName) {
        String path = EBQValue.CAPTURE_PATH;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        File mFile = new File(path, fileName + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();

            return mFile;
        } catch (IOException e) {
            Log.w("ebq", "记录：图片保存失败：理由：" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
