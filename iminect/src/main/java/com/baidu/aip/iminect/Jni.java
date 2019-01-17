/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.iminect;

import java.nio.ByteBuffer;

public class Jni {

    static {
        System.loadLibrary("digColor");
    }


    public native void digColorPerson(ByteBuffer colorBuffer, ByteBuffer depthBuffer, int width, int height);
}
