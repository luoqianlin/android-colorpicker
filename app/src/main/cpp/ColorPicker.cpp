#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include "log.h"
#include "JNIExceptionHelper.h"
#include <cmath>
#include <iostream>

static void HSVtoRGB(float *r, float *g, float *b, float h, float s, float v)
{
    int i;
    float f, p, q, t;
    if (s == 0) {
        // achromatic (grey)
        *r = *g = *b = v;
        return;
    }
    h /= 60;            // sector 0 to 5
    i = static_cast<int>(floor(h));
    f = h - i;          // factorial part of h
    p = v * (1 - s);
    q = v * (1 - s * f);
    t = v * (1 - s * (1 - f));
    switch (i) {
        case 0:
            *r = v;
            *g = t;
            *b = p;
            break;
        case 1:
            *r = q;
            *g = v;
            *b = p;
            break;
        case 2:
            *r = p;
            *g = v;
            *b = t;
            break;
        case 3:
            *r = p;
            *g = q;
            *b = v;
            break;
        case 4:
            *r = t;
            *g = p;
            *b = v;
            break;
        default:        // case 5:
            *r = v;
            *g = p;
            *b = q;
            break;
    }
}
#define  USE_JAVA_COLORHSV_TO_RGB 1

extern "C" JNIEXPORT void JNICALL
Java_com_colorpicker_ColorPicker_nativeFillBitmapPixel(JNIEnv *env, jclass type, jobject bitmap) {

    AndroidBitmapInfo bitmapInfo;
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        char msg[80]={0};
        snprintf(msg,80,"AndroidBitmap_getInfo() failed ! error=%d",ret);
        JNIExceptionHelper::throwExcepton(env,"java/lang/IllegalArgumentException",msg);
        return ;
    }
    if(bitmapInfo.format!=ANDROID_BITMAP_FORMAT_RGBA_8888){
        JNIExceptionHelper::throwExcepton(env,"java/lang/IllegalArgumentException",
                                          "bitmap format must RGBA_8888");
        return ;
    }

    uint32_t height = bitmapInfo.height;
    uint32_t width = bitmapInfo.width;
    uint32_t *pixels;
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixels))) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return ;
    }
    float hsv[3] = {0, 0, 1};
    int w = width;
    int h = height;
    int circleRadius = std::min(width, height) / 2;
    int x = circleRadius, y = circleRadius;
#if USE_JAVA_COLORHSV_TO_RGB
    jclass ColorCls = env->FindClass("android/graphics/Color");
    jmethodID HSVToColor_mid=NULL;
    if(ColorCls!=NULL){
        HSVToColor_mid = env->GetStaticMethodID(ColorCls, "HSVToColor", "([F)I");

    }
    jfloatArray hsv_floatargs = env->NewFloatArray(3);
#endif
    for (int i = width*height- 1; i >= 0; i--) {
        if ((i+1) % w == 0) {
            x = circleRadius;
            y--;
        }else{
            x--;
        }
        double centerDist = sqrt(x * x + y * y);
        if (centerDist <= circleRadius) {
            float v = (float) (atan2(y, x) / M_PI * 180);
            if (v < 0) {
                v += 360;
            }
            hsv[0] = v;
            hsv[1] = (float) (centerDist / circleRadius);
#if !USE_JAVA_COLORHSV_TO_RGB
            float r, g, b;
            HSVtoRGB(&r,&g,&b, hsv[0], hsv[1], hsv[2]);
            uint8_t _r,_g,_b;
            _r= static_cast<uint8_t>(((uint8_t)(r * 0xFF)) & 0xFF);
            _g= static_cast<uint8_t>(((uint8_t)(g * 0xFF)) & 0xFF);
            _b= static_cast<uint8_t>(((uint8_t)(b * 0xFF)) & 0xFF);

            pixels[i] = static_cast<uint32_t>((0xFF << 24) | (_b << 16) | (_g << 8) | _r);
#else
            if(HSVToColor_mid!=NULL){
                env->SetFloatArrayRegion(hsv_floatargs,0,3,hsv);
                pixels[i]= static_cast<uint32_t>(env->CallStaticIntMethod(ColorCls, HSVToColor_mid, hsv_floatargs));
            }
#endif
        }else{
            pixels[i] = 0x00000000;
        }
    }

#if USE_JAVA_COLORHSV_TO_RGB
    env->DeleteLocalRef(ColorCls);
    env->DeleteLocalRef(hsv_floatargs);
#endif
    AndroidBitmap_unlockPixels(env, bitmap);

}