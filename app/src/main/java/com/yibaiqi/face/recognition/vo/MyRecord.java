package com.yibaiqi.face.recognition.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * 刷脸记录，先保存在本地，有网络时进行上传
 * Created by @author xiaofu on 2019/5/3.
 */
@Entity
public class MyRecord {
    @PrimaryKey
    @NonNull
    private String create_time;
    @ColumnInfo(name = "user_key")
    private String user_key;
    @ColumnInfo(name = "file_name")
    private String fileName;
    @ColumnInfo(name = "hikStatus")
    private boolean hikStatus;
    @ColumnInfo(name = "faceStatus")
    private boolean faceStatus;

    public MyRecord(@NonNull String create_time, String user_key, String fileName,
                    boolean hikStatus, boolean faceStatus) {
        this.create_time = create_time;
        this.user_key = user_key;
        this.fileName = fileName;
        this.hikStatus = hikStatus;
        this.faceStatus = faceStatus;
    }

    @NonNull
    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(@NonNull String create_time) {
        this.create_time = create_time;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public boolean isHikStatus() {
        return hikStatus;
    }

    public void setHikStatus(boolean hikStatus) {
        this.hikStatus = hikStatus;
    }

    public boolean isFaceStatus() {
        return faceStatus;
    }

    public void setFaceStatus(boolean faceStatus) {
        this.faceStatus = faceStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


}
