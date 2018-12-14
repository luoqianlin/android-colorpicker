#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.colorpicker.renderscript)

#include "rs_debug.rsh"
#include "rs_math.rsh"
int32_t width;
int32_t height;
int32_t circleRadius;

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
    i = (int)(floor(h));
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

uchar4 __attribute__((kernel)) root(uchar4 in, uint32_t x, uint32_t y) {
	float _x=x-width/2.0f;
	float _y=y-height/2.0f;
	float3 hsv;
	hsv.z=1.0f;
	uchar4 rgba={0,0,0,0};
	float centerDist = sqrt(_x * _x + _y * _y);
	if (centerDist <= circleRadius) {
			float v = atan2(_y, _x) / M_PI * 180;
			if (v < 0) {
				v += 360;
			}
			hsv.x = v;
			hsv.y = (float) (centerDist / circleRadius);
			float r, g, b;
			HSVtoRGB(&r,&g,&b, hsv.x, hsv.y, hsv.z);
			rgba.r= (uchar)(((uchar)(r * 0xFF)) & 0xFF);
			rgba.g= (uchar)(((uchar)(g * 0xFF)) & 0xFF);
			rgba.b= (uchar)(((uchar)(b * 0xFF)) & 0xFF);
			rgba.a=255;
	}
	return rgba;
}