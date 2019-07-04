package com.yibaiqi.face.recognition.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by @author xiaofu on 2019/7/3.
 */
@Entity
public class DbClassOptionContent {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int type_flag;
    private String user_key;
    private int class_course_id;
    private int class_id;
    private String start_time;
    private String end_time;

    public int getType_flag() {
        return type_flag;
    }

    public void setType_flag(int type_flag) {
        this.type_flag = type_flag;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public int getClass_course_id() {
        return class_course_id;
    }

    public void setClass_course_id(int class_course_id) {
        this.class_course_id = class_course_id;
    }

    public int getClass_id() {
        return class_id;
    }

    public void setClass_id(int class_id) {
        this.class_id = class_id;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
