package com.yibaiqi.face.recognition.tools;

import android.os.Environment;

/**
 * Created by @author xiaofu on 2019/5/3.
 */
public class EBQValue {
    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String HIK_PATH = PATH + "/ebq/hik/pic/";
    public static final String CAPTURE_PATH = PATH + "/ebq/capture/pic/";
    public static final String REGISTER_PATH = PATH + "/ebq/register/pic/";
}
