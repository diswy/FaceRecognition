package com.yibaiqi.face.recognition.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by @author xiaofu on 2019/6/11.
 */
@Entity
public class LocalUser {
    @PrimaryKey
    @NonNull
    private String user_key;
    @ColumnInfo(name = "real_name")
    private String real_name;
    @ColumnInfo(name = "app_type")
    private int app_type;//用户类型 11_走读学生,12_走读学生,3_老师 等 不需要做判断
    @ColumnInfo(name = "is_traffic_error")
    private boolean is_traffic_error;// 当前用户刷脸成功后，是否为异常，如为异常 则需要请求 上报异常情况接口，是否开闸 已 config 中的 error_flag 为准
    @ColumnInfo(name = "is_intrude")
    private boolean is_intrude;//是否为入侵者(直接报 非法闯入，其他条件不判断)
    @ColumnInfo(name = "is_class_course")
    private boolean is_class_course;//是否验证课表时间


    public LocalUser(@NonNull String user_key, String real_name, int app_type, boolean is_traffic_error, boolean is_intrude, boolean is_class_course) {
        this.user_key = user_key;
        this.real_name = real_name;
        this.app_type = app_type;
        this.is_traffic_error = is_traffic_error;
        this.is_intrude = is_intrude;
        this.is_class_course = is_class_course;
    }

    @NonNull
    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(@NonNull String user_key) {
        this.user_key = user_key;
    }

    public String getReal_name() {
        return real_name;
    }

    public void setReal_name(String real_name) {
        this.real_name = real_name;
    }

    public int getApp_type() {
        return app_type;
    }

    public void setApp_type(int app_type) {
        this.app_type = app_type;
    }

    public boolean isIs_traffic_error() {
        return is_traffic_error;
    }

    public void setIs_traffic_error(boolean is_traffic_error) {
        this.is_traffic_error = is_traffic_error;
    }

    public boolean isIs_intrude() {
        return is_intrude;
    }

    public void setIs_intrude(boolean is_intrude) {
        this.is_intrude = is_intrude;
    }

    public boolean isIs_class_course() {
        return is_class_course;
    }

    public void setIs_class_course(boolean is_class_course) {
        this.is_class_course = is_class_course;
    }
}
