package com.yibaiqi.face.recognition.vo;

/**
 * Created by @author xiaofu on 2019/5/23.
 */
public class ImportUser {
    private String userKey;
    private String userName;
    private String picUrl;

    public ImportUser(String userKey, String userName, String picUrl) {
        this.userKey = userKey;
        this.userName = userName;
        this.picUrl = picUrl;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
