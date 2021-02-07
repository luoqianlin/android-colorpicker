package com.colorpicker;

import android.graphics.Color;

/**
 * @author lql E-mail: qianlinluo@foxmail.com
 * @version 0  create date:2021/2/7 10:39
 */
public class ColorUtils {

    static float NearlyZero = (1.0f / (1 << 12));

    static float clip(float x, float min, float max) {
        return Math.min(Math.max(x, min), max);
    }

    public static int hsvToRgb(int a, float hsv[]) {
        if (hsv.length != 3) throw new IllegalArgumentException();
        float s = clip(hsv[1], 0, 1);
        float v = clip(hsv[2], 0, 1);
        int v_byte = Math.round(v * 255);
        if (isNearlyZero(s)) {
            return Color.argb(a, v_byte, v_byte, v_byte);
        }
        float hx = (hsv[0] < 0 || hsv[0] >= 360) ? 0 : hsv[0] / 60;
        float w = (float) Math.floor(hx);
        float f = hx - w;
        int p = Math.round((1 - s) * v * 255);
        int q = Math.round((1 - (s * f)) * v * 255);
        int t = Math.round((1 - (s * (1 - f))) * v * 255);

        int r, g, b;

        if (((int) w) >= 6) {
            throw new IllegalStateException("w is " + (int) w);
        }
        switch (((int) w)) {
            case 0:
                r = v_byte;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v_byte;
                b = p;
                break;
            case 2:
                r = p;
                g = v_byte;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v_byte;
                break;
            case 4:
                r = t;
                g = p;
                b = v_byte;
                break;
            default:
                r = v_byte;
                g = p;
                b = q;
                break;

        }
        return Color.argb(a, r, g, b);
    }

    static boolean isNearlyZero(float x) {
        return Math.abs(x) <= NearlyZero;
    }
}
