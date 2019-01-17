/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.orbbec.Native;

import java.nio.ByteBuffer;

/**
 * Created by xlj on 17-3-2.
 */

public class DepthUtils {

    static {
        System.loadLibrary("DepthUtils");
    }

    public  native static int ConvertTORGBA(ByteBuffer src, ByteBuffer dst, int w, int h, int strideInBytes);
    public  native static int RGB888TORGBA(ByteBuffer src, ByteBuffer dst, int w, int h, int strideInBytes);
    public  native static void cameraByte2Bitmap(byte[] data, int[] rgba, int w, int h);
}
