//
// Created by luoqianlin on 2018/12/13.
//

#ifndef COLORPICKER2_JNIEXCEPTIONHELPER_H
#define COLORPICKER2_JNIEXCEPTIONHELPER_H

#include <jni.h>


class JNIExceptionHelper {

public:
    static void throwExcepton(JNIEnv *env,const char* name,const char* msg);

};


#endif //COLORPICKER2_JNIEXCEPTIONHELPER_H
