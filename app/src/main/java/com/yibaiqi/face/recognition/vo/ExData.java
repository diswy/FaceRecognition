package com.yibaiqi.face.recognition.vo;

/**
 * Created by @author xiaofu on 2019/5/2.
 */
public class ExData {
    private DbUserOption users;// 此类会操作人脸数据库这些

    private GlobalConfig config;

    private DbClassOption class_course;

    private Leaves leaves;

    private Settings setting;

    public GlobalConfig getConfig() {
        return config;
    }

    public void setConfig(GlobalConfig config) {
        this.config = config;
    }

    public DbClassOption getClass_course() {
        return class_course;
    }

    public void setClass_course(DbClassOption class_course) {
        this.class_course = class_course;
    }

    public Leaves getLeaves() {
        return leaves;
    }

    public void setLeaves(Leaves leaves) {
        this.leaves = leaves;
    }

    public Settings getSetting() {
        return setting;
    }

    public void setSetting(Settings setting) {
        this.setting = setting;
    }

    public DbUserOption getUsers() {
        return users;
    }

    public void setUsers(DbUserOption users) {
        this.users = users;
    }
}
