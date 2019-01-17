#include "digColor.h"

//#define  LOG_TAG    "Richard"
const char *g_log_tag = "Richard";
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, g_log_tag, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, g_log_tag, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, g_log_tag, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

const char *g_class_name = "com/baidu/aip/iminect/Jni";
jobject g_color_obj = NULL;
jmethodID g_callback_id = NULL;
bool g_is_obj_create = false;

/*
* JNI registration.
*/
static JNINativeMethod s_g_methods[] = {
        /* name, signature, funcPtr */
        {"digColorPerson", "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;II)V", (void *) dig_color}
};

int register_native_funtions(JNIEnv *env, const char *className) {
    jclass cls = env->FindClass(className);
    return env->RegisterNatives(cls, s_g_methods, NELEM(s_g_methods));
}

jint jni_onload(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGD("[Richard] GetEnv failed!");
        return -1;
    }

    register_native_funtions(env, g_class_name);
    g_is_obj_create = false;
    return JNI_VERSION_1_4;
}

void dig_color(JNIEnv *env, jobject obj, jobject color, jobject depth, jint width, jint height) {
    jbyte *color_buffer = (jbyte *) env->GetDirectBufferAddress(color);
    jbyte *depth_buffer = (jbyte *) env->GetDirectBufferAddress(depth);
    if (color_buffer == NULL || depth_buffer == NULL) {
        LOGD("[Richard]LINE_47: JNI buffer is NULL");
        return;
    }

    uint16_t *p_depth = (uint16_t *) depth_buffer;
    if (width == 640 && height == 480) {
        int num = 640 * 480;
        for (int i = 0; i < num; i++) {
            if (p_depth[i] == 0) {
                color_buffer[i * 3] = 0;
                color_buffer[i * 3 + 1] = 0;
                color_buffer[i * 3 + 2] = 0;
            }
        }

    }

    // 回调java方法
    if (!g_is_obj_create) {
        g_color_obj = (jobject) env->NewGlobalRef(obj);
        jclass cls = env->GetObjectClass(g_color_obj);
        g_callback_id = env->GetMethodID(cls, "updateVertices", "()V");
        if (g_callback_id == NULL) {
            LOGD("[Richard]LINE_70: JNI methodID is NULL");
            return;
        }

        g_is_obj_create = true;
    }

    env->CallVoidMethod(g_color_obj, g_callback_id);
    //LOGD("[Richard]LINE_76: ======jni exit digColor======");
}

#ifdef __cplusplus
}
#endif

