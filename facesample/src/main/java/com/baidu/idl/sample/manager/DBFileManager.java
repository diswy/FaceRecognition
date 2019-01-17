package com.baidu.idl.sample.manager;

import com.baidu.idl.sample.listener.OnDBFileListener;
import com.baidu.idl.sample.utils.FileUtils;

/**
 * Created by v_liujialu01 on 2018/12/18.
 */

public class DBFileManager {
    private static DBFileManager single = null;
    private OnDBFileListener mDBFileListener;

    // 私有构造
    private DBFileManager() {

    }

    public static DBFileManager getInstance() {
        if (single == null) {
            synchronized (DBFileManager.class) {
                if (single == null) {
                    single = new DBFileManager();
                }
            }
        }
        return single;
    }

    public void setOnDBFileListener(OnDBFileListener dbFileListener) {
        mDBFileListener = dbFileListener;
    }

    // 复制数据库到SD卡
    public void copyDBFileToSDCard() {
        String sqlPath = "/data/data/com.baidu.idl.face.demo/databases/bdface.db";
        String newPath = "/sdcard/bdface/output/bdface.db";
        if (FileUtils.copyFile(sqlPath, newPath)){
            if (mDBFileListener != null) {
                mDBFileListener.outputDBSuccess();
            }
        } else {
            if (mDBFileListener != null) {
                mDBFileListener.showErrMsg();
            }
        }
    }

    // 复制数据库到内存
    public void copyDBFileToData() {
        String sqlPath = "/sdcard/bdface/import/bdface.db";
        String newPath = "/data/data/com.baidu.idl.face.demo/databases/bdface.db";
        if (FileUtils.copyFile(sqlPath, newPath)){
            if (mDBFileListener != null) {
                mDBFileListener.importDBSuccess();
            }
        } else {
            if (mDBFileListener != null) {
                mDBFileListener.showErrMsg();
            }
        }
    }
}
