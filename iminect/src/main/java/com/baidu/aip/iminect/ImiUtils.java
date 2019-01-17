/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.iminect;

import android.content.Context;
import android.widget.Toast;

public class ImiUtils {
    public static final boolean DEBUG = true;

    private static Toast sToast = null;

    public static void showToast(Context context, String content) {
        if (sToast != null) {
            sToast.cancel();
        }

        sToast = Toast.makeText(context, content, Toast.LENGTH_LONG);
        sToast.show();
    }

    public static String byteToHexString(byte src) {
        StringBuilder builder = new StringBuilder();
        String hex = Integer.toHexString(src & 0xFF);
        if (hex.length() < 2) {
            builder.append(0);
        }
        builder.append(hex);
        return builder.toString();
    }
}
