package com.yibaiqi.face.recognition.vo;

/**
 * Created by @author xiaofu on 2019/7/3.
 */
public class Settings {
    private boolean delete;
    private Data change;

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public Data getChange() {
        return change;
    }

    public void setChange(Data change) {
        this.change = change;
    }

    public static class Data {
        private int type_flag;
        private String start_time;
        private String end_time;
        private String time_detail;
        private String app_types;

        public int getType_flag() {
            return type_flag;
        }

        public void setType_flag(int type_flag) {
            this.type_flag = type_flag;
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

        public String getTime_detail() {
            return time_detail;
        }

        public void setTime_detail(String time_detail) {
            this.time_detail = time_detail;
        }

        public String getApp_types() {
            return app_types;
        }

        public void setApp_types(String app_types) {
            this.app_types = app_types;
        }
    }
}
