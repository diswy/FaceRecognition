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
    private String data_key;
    @ColumnInfo(name = "user_key")
    private String user_key;
    @ColumnInfo(name = "real_name")
    private String real_name;
    @ColumnInfo(name = "face_image")
    private String face_image;
    @ColumnInfo(name = "status")
    private int status;// 0:add  1：delete

    //2019.6.11新增字段
    @ColumnInfo(name = "full_name")
    private String full_name;
    @ColumnInfo(name = "type_flag")
    private int type_flag;
    @ColumnInfo(name = "create_time")
    private String create_time;

    public DbOption(@NonNull String data_key, String user_key, String real_name, String face_image, int status) {
        this.user_key = user_key;
        this.data_key = data_key;
        this.real_name = real_name;
        this.face_image = face_image;
        this.status = status;
    }


    public DbOption(@NonNull String data_key, String user_key, String real_name, String face_image, int status
            , String full_name, int type_flag) {
        this.user_key = user_key;
        this.data_key = data_key;
        this.real_name = real_name;
        this.face_image = face_image;
        this.status = status;
        this.full_name = full_name;
        this.type_flag = type_flag;
    }

    @NonNull
    public String getData_key() {
        return data_key;
    }

    public void setData_key(@NonNull String data_key) {
        this.data_key = data_key;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public int getType_flag() {
        return type_flag;
    }

    public void setType_flag(int type_flag) {
        this.type_flag = type_flag;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}
