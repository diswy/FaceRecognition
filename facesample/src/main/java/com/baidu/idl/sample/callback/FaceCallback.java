package com.baidu.idl.sample.callback;

/**
 * Created by chaixiaogang on 2018/11/8.
 */

public interface FaceCallback {
    /**
     *  回调函数 code 0 : 成功；code 1 加载失败
     * @param code
     * @param response
     */
    void onResponse(int code, String response);
}
