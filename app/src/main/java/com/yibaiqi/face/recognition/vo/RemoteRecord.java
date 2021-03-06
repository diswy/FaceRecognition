package com.yibaiqi.face.recognition.vo;

/**
 * Created by @author xiaofu on 2019/5/11.
 */
public class RemoteRecord {
    private String user_key;
    private String face_photo;
    private String body_photo;
    private String create_time;
    private int error_type;
    private boolean is_open;

    public RemoteRecord(String user_key, String face_photo, String body_photo, String create_time,
                        int errorType, boolean isOpen) {
        this.user_key = user_key;
        this.face_photo = face_photo;
        this.body_photo = body_photo;
        this.create_time = create_time;
        this.error_type = errorType;
        this.is_open = isOpen;
    }

    public int getError_type() {
        return error_type;
    }

    public void setError_type(int error_type) {
        this.error_type = error_type;
    }

    public boolean isIs_open() {
        return is_open;
    }

    public void setIs_open(boolean is_open) {
        this.is_open = is_open;
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

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}
