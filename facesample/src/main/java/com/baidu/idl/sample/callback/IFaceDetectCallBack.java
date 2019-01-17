package com.baidu.idl.sample.callback;

public interface IFaceDetectCallBack {
    /**
     * 是否检测到人脸
     * @param isDetect  true: 检测到人脸 false:没有检测到
     * @param faceWidth  人脸框宽度
     * @param faceCenterX 人脸中心点x
     * @param faceCenterY 人脸中心点y
     */
    public void onFaceDetectCallback(boolean isDetect, int faceWidth, int faceHeight,
                                     int faceCenterX, int faceCenterY, int imgWidth,
                                     int imgHeight);
}
