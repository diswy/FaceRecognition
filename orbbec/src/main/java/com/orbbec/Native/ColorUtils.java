/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.orbbec.Native;

import java.nio.ByteBuffer;


public class ColorUtils {

    static {
        System.loadLibrary("DepthUtils");
    }

    public  native static int RGB888TORGBA(ByteBuffer src, ByteBuffer dst, int w, int h, int strideInBytes);
}
