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
    @ColumnInfo(name = "face_photo")
    private String face_photo;
    @ColumnInfo(name = "body_photo")
    private String body_photo;
    @ColumnInfo(name = "isSync")
    private boolean isSync;// 是否网络同步过


    public MyRecord(@NonNull String create_time, String user_key, String face_photo, String body_photo, boolean isSync) {
        this.create_time = create_time;
        this.user_key = user_key;
        this.face_photo = face_photo;
        this.body_photo = body_photo;
        this.isSync = isSync;
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

    public String getFace_photo() {
        return face_photo;
    }

    public void setFace_photo(String face_photo) {
        this.face_photo = face_photo;
    }

    public String getBody_photo() {
        return body_photo;
    }

    public void setBody_photo(String body_photo) {
        this.body_photo = body_photo;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }
}
