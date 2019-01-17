/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.model;


/**
 *  该类封装了一帧图片。
 */
public class ImageFrame {
    /**
     * argb数据
     */
    private int[] argb;

    /**
     * nir 数据
     */
    private byte[] ir;

    /**
     * depth 数据
     */
    private byte[] depth;
    /**
     * 图片宽度
     */
    private int width;
    /**
     * 图片调试
     */
    private int height;
    private ArgbPool pool;
    private boolean retained = false;

    public ImageFrame() {

    }

    public void setPool(ArgbPool pool) {
        this.pool = pool;
    }

    public ArgbPool getPool() {
        return pool;
    }

    public ImageFrame(int[] argb, int width, int height) {
        this.argb = argb;
        this.width = width;
        this.height = height;
    }

    public int[] getArgb() {
        return argb;
    }

    public void setArgb(int[] argb) {
        this.argb = argb;
    }

    public byte[] getIr() {
        return ir;
    }

    public void setIr(byte[] ir) {
        this.ir = ir;
    }

    public byte[] getDepth() {
        return depth;
    }

    public void setDepth(byte[] depth) {
        this.depth = depth;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // TODO
    public void retain() {
        this.retained = true;
    }

    public void release() {
//        if (!retained) {
//            pool.release(argb);//TODO
//        }
        retained = false;
    }
}
