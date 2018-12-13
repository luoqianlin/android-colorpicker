//
// Created by luoqianlin on 2018/4/17.
//

#ifndef FFMPEGDEMO_LOG_H
#define FFMPEGDEMO_LOG_H

#include <android/log.h>

#define  LOG_TAG   __func__


#define  LOGD(...)  (__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define  LOGI(...)  (__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
#define  LOGW(...)  (__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))
#define  LOGE(...)  (__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))






#endif //FFMPEGDEMO_LOG_H
