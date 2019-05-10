package com.yibaiqi.face.recognition.vo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by @author xiaofu on 2019/4/23.
 */
public class RegisterDevice {

    /**
     * im_token : eYpIT0vkxpxvOFw9VpzQFvPSFA5EIckBl0ShOfk9fUj7JNPqVJtvYugLRwnDtQTyhuIetx/SbomfBrlAYu5AjoockTR/UNwTUi+4BKhuX4AclNXliAtXcxqlYjFhDmyJ8RXKuaOlMTh71UOPtJCu+z7ekPGA/Qcp
     * serial_number : 3
     * token : 37ed178c52696ac0610a1aaa6edfdf89
     */

    @SerializedName("im_token")
    private String imToken;
    @SerializedName("serial_number")
    private String serialNumber;
    private String token;
    private ExData data;
    @SerializedName("oss_config")
    private OSSConfig ossConfig;

    public OSSConfig getOssConfig() {
        return ossConfig;
    }

    public void setOssConfig(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    public ExData getData() {
        return data;
    }

    public void setData(ExData data) {
        this.data = data;
    }

    public String getImToken() {
        return imToken;
    }

    public void setImToken(String imToken) {
        this.imToken = imToken;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
