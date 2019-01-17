package com.baidu.idl.sample.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by litonghui on 2018/11/26.
 */

public class ToastUtils {

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void toast(final Context context, final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void toast(final Context context, final int resId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
            }
        });
    }
}
