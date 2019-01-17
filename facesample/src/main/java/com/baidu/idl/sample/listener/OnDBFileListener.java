package com.baidu.idl.sample.listener;

/**
 * 数据库导入导出监听
 * Created by v_liujialu01 on 2018/12/18.
 */

public interface OnDBFileListener {
    void importDBSuccess();
    void outputDBSuccess();
    void showErrMsg();
}
