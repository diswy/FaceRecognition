/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.sample.callback;


import com.baidu.idl.sample.model.LivenessModel;

public interface ILivenessCallBack {
    public void onTip(int code, String msg);

    public void onCanvasRectCallback(LivenessModel livenessModel);

    public void onCallback(int code,LivenessModel livenessModel);
}
