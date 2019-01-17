package com.baidu.idl.sample.common;

import com.baidu.idl.facesdk.utils.PreferencesUtil;

/**
 * Created by litonghui on 2018/12/5.
 */

public class GlobalSet {

    public static int FACE_AUTH_STATUS = -1;

    // 最小人脸设置类型
    public static final String TYPE_MIN_FACE_SET = "TYPE_MIN_FACE_SET";

    // 检测角度类型
    public static final int TYPE_TRACK_ZERO_ANGLE = 0;
    public static final int TYPE_TRACK_NINETY_ANGLE = 90;
    public static final int TYPE_TRACK_ONE_HUNDERED_EIGHTY_ANGLE = 180;
    public static final int TYPE_TRACK_TWO_HUNDERED_SEVENTY_ANGLE = 270;
    public static final String TYPE_TRACK_ANGLE = "TYPE_TRACK_ANGLE";

    // 预览角度
    public static final int TYPE_PREVIEW_ZERO_ANGLE = 0;
    public static final int TYPE_TPREVIEW_NINETY_ANGLE = 90;
    public static final int TYPE_PREVIEW_ONE_HUNDERED_EIGHTY_ANGLE = 180;
    public static final int TYPE_PREVIEW_TWO_HUNDERED_SEVENTY_ANGLE = 270;
    public static final String TYPE_PREVIEW_ANGLE = "TYPE_PREVIEW_ANGLE";

    // 质量检测开启关闭
    public static final String TYPE_QUALITY = "TYPE_QUALITY";
    public static final int TYPE_QUALITY_OPEN = 0;
    public static final int TYPE_QUALITY_CLOSE = 1;

    // 质量参数阈值类型
    public static final String TYPE_BLURR_THRESHOLD = "TYPE_BLURR_THRESHOLD";
    public static final String TYPE_OCCLUSION_THRESHOLD = "TYPE_OCCLUSION_THRESHOLD";
    public static final String TYPE_ILLUMINATION_THRESHOLD = "TYPE_ILLUMINATION_THRESHOLD";

    // 人脸识别相似度阈值类型
    public static final String TYPE_RGB_FEATURE_THRESHOLD = "TYPE_RGB_FEATURE_THRESHOLD";
    public static final String TYPE_ID_FEATURE_THRESHOLD = "TYPE_ID_FEATURE_THRESHOLD";
    public static final String TYPE_DETECT_CONF_THRESHOLD = "TYPE_DETECT_CONF_THRESHOLD";

    public static final String TYPE_NO_LIVENSS = "type_no_livenss";
    public static final String TYPE_RGB_LIVENSS = "type_rgb_livenss";
    public static final String TYPE_RGB_IR_LIVENSS = "type_rgb_ir_livenss";
    public static final String TYPE_RGB_DEPTH_LIVENSS = "type_rgb_depth_livenss";
    public static final String TYPE_RGB_IR_DEPTH_LIVENSS = "type_rgb_ir_depth_livenss";

    public static final String TYPE_LIVENSS_STATUS = "type_livenss_status";
    public static final String TYPE_STRUCTURED_LIGHT_STATUS = "type_structured_light_status";

    // 鉴权类型  默认为0
    public static final String LICENSE_STATUS = "license_status";
    public static final int LICENSE_APP = 1;
    public static final int LICENSE_ONLINE = 2;
    public static final int LICENSE_OFFLINE = 3;
    public static final String LICENSE_ONLINE_KEY = "license_online_key";

    // 设置图片样本导入状态
    private static final String IMPORT_FACES_SAMPLE = "IMPORT_FACES_SAMPLE";

    // 默认摄像头分辨率，实际采集的图片和该分辨率是相反的
    public static final int PREFER_WIDTH = 640;
    public static final int PERFER_HEIGH = 480;

    // 图片尺寸限制大小
    public static final int pictureSize = 1000000;

    private static float LIVE_RGB_VALUE = 0.80f;
    private static float LIVE_NIR_VALUE = 0.80f;
    private static float LIVE_DEPTH_VALUE = 0.80f;
    private static float QUALITY_OCCLUR_VALUE = 0.50f;
    private static float QUALITY_BLUR_VALUE = 0.70f;
    private static float QUALITY_ILLUM_VALUE = 40.00f;
    private static float FEATURE_RGB_VALUE = 90f;
    private static float FEATURE_PHONE_VALUE = 90f;
    private static float DETECT_CONF_VALUE = 2.0f;

    public static final int IMPORT_REQUEST_CODE = 1;
    public static final int IMPORT_RESULT_CODE = 2;

    public static final int DB_REQUEST_CODE = 3;
    public static final int DB_RESULT_CODE = 4;

    public enum LIVE_STATUS {
        NO, RGB, RGN_NIR, RGB_DEPTH
    }

    public enum STRUCTURED_LIGHT {
        OBI_ASTRA_MINI, OBI_ASTRA_PRO, HUAJIE_AMY_MINI
    }


    public static float getLiveRgbValue() {
        return PreferencesUtil.getFloat(TYPE_RGB_LIVENSS, LIVE_RGB_VALUE);
    }

    public static void setLiveRgbValue(float liveRgbValue) {
        PreferencesUtil.putFloat(TYPE_RGB_LIVENSS, liveRgbValue);
    }

    public static float getLiveNirValue() {
        return PreferencesUtil.getFloat(TYPE_RGB_IR_LIVENSS, LIVE_NIR_VALUE);
    }

    public static void setLiveNirValue(float liveNirValue) {
        PreferencesUtil.putFloat(TYPE_RGB_IR_LIVENSS, liveNirValue);
    }

    public static float getLiveDepthValue() {
        return PreferencesUtil.getFloat(TYPE_RGB_DEPTH_LIVENSS, LIVE_DEPTH_VALUE);
    }

    public static void setLiveDepthValue(float liveDepthValue) {
        PreferencesUtil.putFloat(TYPE_RGB_DEPTH_LIVENSS, liveDepthValue);
    }

    public static float getQualityOcclurValue() {
        return PreferencesUtil.getFloat(TYPE_OCCLUSION_THRESHOLD, QUALITY_OCCLUR_VALUE);
    }

    public static void setQualityOcclurValue(float qualityOcclurValue) {
        PreferencesUtil.putFloat(TYPE_OCCLUSION_THRESHOLD, qualityOcclurValue);
    }

    public static float getQualityBlurValue() {
        return PreferencesUtil.getFloat(TYPE_BLURR_THRESHOLD, QUALITY_BLUR_VALUE);
    }

    public static void setQualityBlurValue(float qualityBlurValue) {
        PreferencesUtil.putFloat(TYPE_BLURR_THRESHOLD, qualityBlurValue);
    }

    public static float getQualityIllumValue() {
        return PreferencesUtil.getFloat(TYPE_ILLUMINATION_THRESHOLD, QUALITY_ILLUM_VALUE);
    }

    public static void setQualityIllumValue(float qualityIllumValue) {
        PreferencesUtil.putFloat(TYPE_ILLUMINATION_THRESHOLD, qualityIllumValue);
    }

    public static float getFeatureRgbValue() {
        return PreferencesUtil.getFloat(TYPE_RGB_FEATURE_THRESHOLD, FEATURE_RGB_VALUE);
    }

    public static void setFeatureRgbValue(float featureRgbValue) {
        PreferencesUtil.putFloat(TYPE_RGB_FEATURE_THRESHOLD, featureRgbValue);
    }

    public static float getFeaturePhoneValue() {
        return PreferencesUtil.getFloat(TYPE_ID_FEATURE_THRESHOLD, FEATURE_PHONE_VALUE);
    }

    public static void setFeaturePhoneValue(float featurePhoneValue) {
        PreferencesUtil.putFloat(TYPE_ID_FEATURE_THRESHOLD, featurePhoneValue);
    }

    public static float getDetectConfValue() {
        return PreferencesUtil.getFloat(TYPE_DETECT_CONF_THRESHOLD, DETECT_CONF_VALUE);
    }

    public static void setDetectConfValue(float detectConfValue) {
        PreferencesUtil.putFloat(TYPE_DETECT_CONF_THRESHOLD, detectConfValue);
    }

    public static LIVE_STATUS getLiveStatusValue() {
        return LIVE_STATUS.values()[PreferencesUtil.getInt(TYPE_LIVENSS_STATUS, LIVE_STATUS.RGB.ordinal())];
    }

    public static void setLiveStatusValue(LIVE_STATUS liveStatusValue) {
        PreferencesUtil.putInt(TYPE_LIVENSS_STATUS, liveStatusValue.ordinal());
    }

    public static STRUCTURED_LIGHT getStructuredLightValue() {
        return STRUCTURED_LIGHT.values()[PreferencesUtil.getInt(TYPE_STRUCTURED_LIGHT_STATUS,
                STRUCTURED_LIGHT.OBI_ASTRA_PRO.ordinal())];
    }

    public static void setStructuredLightValue(STRUCTURED_LIGHT structuredLightValue) {
        PreferencesUtil.putInt(TYPE_STRUCTURED_LIGHT_STATUS, structuredLightValue.ordinal());
    }

    //  鉴权的方式
    public static void setLicenseStatus(int status) {
        PreferencesUtil.putInt(LICENSE_STATUS, status);
    }

    public static int getLicenseStatus() {
        return PreferencesUtil.getInt(LICENSE_STATUS, 0);
    }

    //  在线鉴权，key储存
    public static void setLicenseOnLineKey(String key) {
        PreferencesUtil.putString(LICENSE_ONLINE_KEY, key);
    }

    public static String getLicenseOnLineKey() {
        return PreferencesUtil.getString(LICENSE_ONLINE_KEY, "");
    }

    public static void setIsImportSample(boolean isImportSample) {
        PreferencesUtil.putBoolean(IMPORT_FACES_SAMPLE, isImportSample);
    }

    public static boolean getIsImportSample() {
        return PreferencesUtil.getBoolean(IMPORT_FACES_SAMPLE, false);
    }
}
