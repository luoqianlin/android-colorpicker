//
// Created by luoqianlin on 2018/12/13.
//

#include "JNIExceptionHelper.h"
#include "log.h"

void JNIExceptionHelper::throwExcepton(JNIEnv *env, const char *name,const char* msg) {
    jclass cls = env->FindClass(name);
    if (cls != NULL) {
        env->ThrowNew(cls,msg);
        env->DeleteLocalRef(cls);
    }else{
        LOGE("class %s not found",name);
    }
}
