# 颜色选择控件(HSV颜色模型顶部横截面)

支持颜色的设置，当前选择颜色的监听,效果如图

![截屏](images/colorpicker.gif)


## 原理
我们知道颜色的表示有RGB,YUV,HSV,HSL等方式。

- RGB颜色空间，可以表示为一个立方体如图

![RGB色彩空间](images/rgb_colorspace.jpg)

- YUV 色彩空间，电视系统这种表示方法用的较多，兼容黑白电视，与RGB互转公式为（RGB取值范围均为0-255）：

```

　Y = 0.299R + 0.587G + 0.114B
　U = -0.147R - 0.289G + 0.436B
　V = 0.615R - 0.515G - 0.100B

　R = Y + 1.14V
　G = Y - 0.39U - 0.58V
　B = Y + 2.03U
```

- HSV 可以表示为园柱体

![HSV色彩空间](images/hsv_colorspace.jpg)

    其中HSV 分别表示色调（hue），饱和度（saturation），值（value）。其颜色模型是一倒置的圆锥体，value等于0的地方为黑色，值为1的横截面中心为白色 
    
Hue： 对应角度

Staturation: 对应到圆心的距离于圆半径的比值

Value ：对应到圆锥顶部的距离


HSV模型在Value值为1的时候，根据颜色能够方便的计算出对应的几何坐标值，根据坐标轴值也直接计算出对应的颜色值，到达颜色值与位置对应起来。

## 圆盘的绘制
















## 性能对比

 > 环境: Pixel 2  Android 8.1.0

|  方法   |  10次平均耗时  |
|   ---  |      ---     |
| java code | 1061.4ms |
| native Color hsv2rgb | 1930.4ms |
| native hsv2rgb | 139.3ms |
| renderscript | 22.4ms |