package com.yibaiqi.face.recognition.vo;

import java.io.Serializable;

/**
 * Created by @author xiaofu on 2019/5/10.
 */
public class OSSConfig implements Serializable {

    private String Endpoint;
    private String BucketName;
    private String ObjectPath;

    public String getEndpoint() {
        return Endpoint;
    }

    public void setEndpoint(String endpoint) {
        Endpoint = endpoint;
    }

    public String getBucketName() {
        return BucketName;
    }

    public void setBucketName(String bucketName) {
        BucketName = bucketName;
    }

    public String getObjectPath() {
        return ObjectPath;
    }

    public void setObjectPath(String objectPath) {
        ObjectPath = objectPath;
    }
}
