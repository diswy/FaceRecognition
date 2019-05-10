package com.yibaiqi.face.recognition.vo;

/**
 * Created by @author xiaofu on 2019/5/9.
 */
public class OSSKey {

    /**
     * AccessKeyId : STS.NHezVcQJwzos1pwTWz8DoFFqg
     * AccessKeySecret : CgLEyhBvu8gZpQ2wtWviadNaZDxcFKtwyvGwYs2mMVUL
     * Expiration : 2019-05-09T08:49:04Z
     * SecurityToken : CAISogN1q6Ft5B2yfSjIr4vQMezXvJVWza2YM1bGsFcvNMtDqYPahTz2IHlIeHhgBuoYsvkynGhW7/wTlqJ4T55IQ1Dza8J148yzJMsXos+T1fau5Jko1bcrcAr6Umxzta2/SuH9S8ynkJ7PD3nPii50x5bjaDymRCbLGJaViJlhHNZ1Ow6jdmhpCctxLAlvo9N4UHzKLqSVLwLNiGjdB1YKwg1nkjFT5KCy3sC74BjTh0GYr+gOvNbVI4O4V8B2IIwdI9Cux75ffK3bzAtN7wRL7K5skJFc/TDOsrP6BEJKsTGHKPbz+N9iJxNiHJJYfZRJt//hj/Z1l/XOnoDssXZ3MPpSTj7USfL+ornNE/j7Mc0iJ/SpeSaB+O2kFbTJhCUNRF9cdmE6ctE6eHhrEk5uGHOIZoWa03PnXjCFYo2o8tlvj8QvkAmyoovTegHeHOnF60tCZM9gNXFPHgUNwGnsfpUBdwFxaF59D96XN94qMEwP+fyy41GPC3U4kCgN7ueNbvfXq70Zbp7kQpVF3IwSaZJLqWI2SE7tTLajmq+A38+aoSQ8GoABlU4jv8ZEZtoHGGoTpQZ7O2PML6ziPNnzbBEyIKzw+qRGb1kPeMoW6pboCtN6LH/93y9KHO2BlEWTjxUiCbreBXjwqwC+VBgH9wasXSuBrUZh02p2XkYtxeJnZxZasLw1vxN4NuZrTs1sbrATfx31+nXy3aM6NGFTrj+qCmHIH3g=
     * Endpoint : oss-cn-hangzhou.aliyuncs.com
     * BucketName : yizhixiao
     * ObjectPath : FaceDevice/FacePicture/
     */

    private String AccessKeyId;
    private String AccessKeySecret;
    private String Expiration;
    private String SecurityToken;
    private String Endpoint;
    private String BucketName;
    private String ObjectPath;

    public String getAccessKeyId() {
        return AccessKeyId;
    }

    public void setAccessKeyId(String AccessKeyId) {
        this.AccessKeyId = AccessKeyId;
    }

    public String getAccessKeySecret() {
        return AccessKeySecret;
    }

    public void setAccessKeySecret(String AccessKeySecret) {
        this.AccessKeySecret = AccessKeySecret;
    }

    public String getExpiration() {
        return Expiration;
    }

    public void setExpiration(String Expiration) {
        this.Expiration = Expiration;
    }

    public String getSecurityToken() {
        return SecurityToken;
    }

    public void setSecurityToken(String SecurityToken) {
        this.SecurityToken = SecurityToken;
    }

    public String getEndpoint() {
        return Endpoint;
    }

    public void setEndpoint(String Endpoint) {
        this.Endpoint = Endpoint;
    }

    public String getBucketName() {
        return BucketName;
    }

    public void setBucketName(String BucketName) {
        this.BucketName = BucketName;
    }

    public String getObjectPath() {
        return ObjectPath;
    }

    public void setObjectPath(String ObjectPath) {
        this.ObjectPath = ObjectPath;
    }
}
