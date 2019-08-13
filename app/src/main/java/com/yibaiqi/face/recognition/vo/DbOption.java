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
    private int type_flag;//type_flag 1_代表新增(需操作人脸库，和本地数据库),2_代表更新(只操作本地数据库)
    @ColumnInfo(name = "create_time")
    private String create_time;

    //2019.7.3新增
    @ColumnInfo(name = "app_type")
    private int app_type;//用户类型 11_走读学生,12_走读学生,3_老师 等 不需要做判断
    @ColumnInfo(name = "is_traffic_error")
    private boolean is_traffic_error;// 当前用户刷脸成功后，是否为异常，如为异常 则需要请求 上报异常情况接口，是否开闸 已 config 中的 error_flag 为准
    @ColumnInfo(name = "is_intrude")
    private boolean is_intrude;//是否为入侵者(直接报 非法闯入，其他条件不判断)
    @ColumnInfo(name = "is_class_course")
    private boolean is_class_course;//是否验证课表时间

//    public DbOption(@NonNull String data_key, String user_key, String real_name, String face_image, int status) {
//        this.user_key = user_key;
//        this.data_key = data_key;
//        this.real_name = real_name;
//        this.face_image = face_image;
//        this.status = status;
//    }


    public DbOption(@NonNull String data_key, String user_key, String real_name, String face_image, int status
            , String full_name, int type_flag, int app_type, boolean is_traffic_error, boolean is_intrude, boolean is_class_course) {
        this.user_key = user_key;
        this.data_key = data_key;
        this.real_name = real_name;
        this.face_image = face_image;
        this.status = status;
        this.full_name = full_name;
        this.type_flag = type_flag;
        this.app_type = app_type;
        this.is_traffic_error = is_traffic_error;
        this.is_intrude = is_intrude;
        this.is_class_course = is_class_course;
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
