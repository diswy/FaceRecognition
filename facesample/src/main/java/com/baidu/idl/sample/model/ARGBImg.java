/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.model;

/**
 * Created by litonghui on 2018/1/22.
 */
public class ARGBImg {
    public int[] data;
    public int width;
    public int height;
    public int angle = 0;
    public int flip = 0;

    public ARGBImg() {
    }

    public ARGBImg(int[] data, int width, int height, int angle, int flip) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.angle = angle;
        this.flip = flip;
    }

    public String toString() {
        return "width:" + this.width + ", "
                + "height:" + this.height + ", " + "angle:"
                + this.angle + ", " + "flip:" + this.flip;
    }
}
