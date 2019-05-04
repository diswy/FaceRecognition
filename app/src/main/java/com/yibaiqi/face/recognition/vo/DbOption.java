package com.yibaiqi.face.recognition.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by @author xiaofu on 2019/5/2.
 */
@Entity
public class DbOption {
    @PrimaryKey
    @NonNull
    private String user_key;
    @ColumnInfo(name = "real_name")
    private String real_name;
    @ColumnInfo(name = "face_image")
    private String face_image;

    public DbOption(@NonNull String user_key, String real_name, String face_image) {
        this.user_key = user_key;
        this.real_name = real_name;
        this.face_image = face_image;
    }

    @NonNull
    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getReal_name() {
        return real_name;
    }

    public void setReal_name(String real_name) {
        this.real_name = real_name;
    }

    public String getFace_image() {
        return face_image;
    }

    public void setFace_image(String face_image) {
        this.face_image = face_image;
    }
}
