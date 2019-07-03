package com.yibaiqi.face.recognition.vo;

/**
 * Created by @author xiaofu on 2019/7/3.
 */
public class GlobalConfig {
    private int error_flag;//用户异常时，是否开门 1_不直接开门.2_直接开门 (ps:不管开不开门，都需要 调用 上报异常记录接口)
    private int setting_traffic_flag;//固定配置的通行类型 1_允许通行,2_禁止通行

    public int getError_flag() {
        return error_flag;
    }

    public void setError_flag(int error_flag) {
        this.error_flag = error_flag;
    }

    public int getSetting_traffic_flag() {
        return setting_traffic_flag;
    }

    public void setSetting_traffic_flag(int setting_traffic_flag) {
        this.setting_traffic_flag = setting_traffic_flag;
    }
}
