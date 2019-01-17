//
// Created by xlj on 17-3-2.
//

#include <jni.h>
#include <android/log.h>

#define REGISTER_CLASS "com/orbbec/nativeni/ColorUtils"

#define LOG_TAG "ColorUtils-Jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)

typedef unsigned char uint8_t;
typedef struct {
    uint8_t r;
    uint8_t g;
    uint8_t b;
}RGB888Pixel;


int ConventToRGBA(uint8_t* src, uint8_t* dst,  int w, int h, int strideInBytes){
    for (int y = 0; y < h; ++y)
    {
        uint8_t* pTexture = dst +  (y*w*4);
        const RGB888Pixel* pData = (const RGB888Pixel*)(src + y * strideInBytes);
        for (int x = 0; x < w; ++x, ++pData, pTexture += 4)
        {
            pTexture[0] = pData->r;
            pTexture[1] = pData->g;
            pTexture[2] = pData->b;
            pTexture[3] = 255;
        }

    }

    return 0;
}


jint RGB888TORGBA(JNIEnv* env, jobject obj, jobject src, jobject dst, jint w, jint h, jint strideInBytes){

    if(src == nullptr || dst == nullptr){
        return -1;
    }
    uint8_t* srcBuf = (uint8_t*)env->GetDirectBufferAddress(src);

    uint8_t* dstBuf = (uint8_t*)env->GetDirectBufferAddress(dst);

    ConventToRGBA(srcBuf, dstBuf, w, h, strideInBytes);

    return 0;
}



JNINativeMethod jniMethods[] = {
        { "RGB888TORGBA",                      "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;III)I",                      (void*)&RGB888TORGBA},
};

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env;
    jclass gCallbackClass = nullptr;

    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jclass clz     = env ->FindClass(REGISTER_CLASS);
    gCallbackClass = (jclass)env ->NewGlobalRef(clz);
    env ->RegisterNatives(clz, jniMethods, sizeof(jniMethods) / sizeof(JNINativeMethod));
    env ->DeleteLocalRef(clz);
    LOGD("ColorUtils JNI_OnLoad");
    return JNI_VERSION_1_6;
}
