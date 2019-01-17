#ifndef BAIDU_AIP_ANDROID_FACERECOGNIZEOFFLINE_IMINECT_SRC_MAIN_CPP_DIGCOLOR_H
#define BAIDU_AIP_ANDROID_FACERECOGNIZEOFFLINE_IMINECT_SRC_MAIN_CPP_DIGCOLOR_H

#include <jni.h>
#include <stdio.h>
#include <android/log.h>

#ifndef NELEM
#define NELEM(x) ((int)(sizeof(x) / sizeof((x)[0])))
#endif

#ifdef __cplusplus
extern "C" {
#endif

void dig_color(JNIEnv *env, jobject obj, jobject color, jobject depth, jint width, jint height);

jint jni_onload(JavaVM *vm, void *reserved);

int register_native_funtions(JNIEnv *env, const char *className);

#ifdef __cplusplus
}
#endif

#endif
