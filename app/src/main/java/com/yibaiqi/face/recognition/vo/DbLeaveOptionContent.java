package com.yibaiqi.face.recognition.vo;

/**
 * Created by @author xiaofu on 2019/7/3.
 */
public class DbLeaveOptionContent {
    private int type_flag;
    private String user_key;
    private int leave_id;
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

    public int getLeave_id() {
        return leave_id;
    }

    public void setLeave_id(int leave_id) {
        this.leave_id = leave_id;
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
}
