package com.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.colorpicker.renderscript.ScriptC_colorPicker;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author lql
 * @version 0
 * 类说明
 */
public class ColorPicker extends View {
    private static final String TAG="ColorPikerView";
    private static final String COLOR_PICKER_PNG = "colorPikerImage%dx%d.png";
    private float circleRadius;
    private Rect contentRect;           
    private Bitmap rgbBitmap;
    private Point selectedPoint = new Point();
    private boolean mIsDragging = false;
    private int latest_x = 0;
    private int latest_y = 0;
    private OnColorSelectedListener listener;
    float[] colorHsv = { 0f, 0f, 1f };
    private Bitmap pickerBitmap;
    private Paint textPaint;
    private  Context context;

    public final static int JAVA_CODE = 0;
    public final static int NATIVE_CODE = 1;
    public final static int RS_CODE = 2;
    private int genMethod = JAVA_CODE;

    static {
        System.loadLibrary("colorpicker-lib");
    }

    private PaintFlagsDrawFilter filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);;

    public ColorPicker(Context context) {
        this(context,null);
    }

    public ColorPicker(Context context,  @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ColorPicker(Context context,  @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        float density = context.getResources().getDisplayMetrics().density;
        Log.i(TAG,"density:"+density);
        pickerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.color_picker1);
        textPaint=new Paint();
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,14,context.getResources().getDisplayMetrics()));
        textPaint.setColor(Color.RED);
        textPaint.setAntiAlias(true);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int left = getPaddingLeft() ;
        int top = getPaddingTop() ;
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int contentWidth = getWidth() - left - right;
        int contentHeight = getHeight() - top - bottom;
        int startX, startY, el;
        if (contentWidth < contentHeight) {
            el = contentWidth;
            startX = left;
            startY = top + (contentHeight - contentWidth) / 2;
        } else {
            el =  contentHeight;
            startX = left + (contentWidth - contentHeight) / 2;
            startY = top;
        }
        contentRect = new Rect(startX, startY, startX + el, startY + el);
        circleRadius = Math.min(contentRect.width(), contentRect.height()) / 2;
        if (contentRect.width() > 0 && contentRect.height() > 0) {
            createBitmap();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.setDrawFilter(filter);

        if(rgbBitmap!=null) {
            Log.d(TAG,"hsv:"+ Arrays.toString(this.colorHsv));
            canvas.drawBitmap(rgbBitmap, null, contentRect, textPaint);
            getPointForColor();
            canvas.drawBitmap(pickerBitmap, selectedPoint.x - pickerBitmap.getWidth() / 2, selectedPoint.y - pickerBitmap.getHeight() / 2, null);
        }else{
            String text = context.getString(R.string.colors_initialisation_load);
            float textWidth = textPaint.measureText(text, 0, text.length());
            canvas.drawText(text,(contentRect.left+contentRect.right-textWidth)/2,(contentRect.top+contentRect.bottom)/2,textPaint);
        }
    }

    private void getPointForColor() {
        float hue = colorHsv[0] / 180f * (float)Math.PI;
        selectedPoint.x = contentRect.left + (int) (Math.cos(hue) * colorHsv[1] * circleRadius + circleRadius);
        selectedPoint.y = contentRect.top + (int) (Math.sin(hue) * colorHsv[1] * circleRadius + circleRadius);
    }

    private Bitmap createBitmap() {
//        final String fileName = String.format(COLOR_PICKER_PNG, contentRect.width(), contentRect.height());
//        rgbBitmap = getBitmap(getContext(), fileName);
        if (rgbBitmap == null) {
            new Thread() {
                public void run() {
                    try {
                        rgbBitmap = createColorPNG(contentRect, context, genMethod);
//                        saveImagePNG(getContext(), fileName, rgbBitmap);
                        post(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        return rgbBitmap;
    }

    //native Color hsv2rgb 10次 1930.4ms
    //native hsv2rgb 10次 139.3ms
    //java code 10次 1061.4ms
    //renderscript 10 22.4ms
    private static Bitmap createColorPNG(Rect contentRect,Context context,int genMethod) {
        Bitmap rgbBitmap = Bitmap.createBitmap(contentRect.width(), contentRect.height(), Bitmap.Config.ARGB_8888);
        Log.d(TAG,String.format("%dx%d",rgbBitmap.getWidth(),rgbBitmap.getHeight()));
        long start=SystemClock.elapsedRealtime();
//        for(int i=0;i<10;i++) {
            if (genMethod == JAVA_CODE) {
                Log.d(TAG,"--fillBitmapPixel--");
                fillBitmapPixel(rgbBitmap);
            } else if (genMethod == NATIVE_CODE) {
                Log.d(TAG,"--nativeFillBitmapPixel--");
                nativeFillBitmapPixel(rgbBitmap);
            } else if (genMethod == RS_CODE) {
                Log.d(TAG,"--rsfillBitmapPixel--");
                rsfillBitmapPixel(rgbBitmap, context);
            }
//        }
//        rgbBitmap =Bitmap.createScaledBitmap(rgbBitmap,contentRect.width(),contentRect.height(),true);
        long end = SystemClock.elapsedRealtime();
        Log.d(TAG, "fill bitmap pixel cost:" + (end - start)/*/10.0*/ + "ms");
        return rgbBitmap;
    }

    public static Bitmap rsfillBitmapPixel(Bitmap image, Context context) {
        //Get image size
        int width = image.getWidth();
        int height = image.getHeight();
        int circleRadius = Math.min(image.getWidth(), image.getHeight()) / 2;

        //Create new bitmap
        Bitmap res = image/*.copy(image.getConfig(), true)*/;

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocationA = Allocation.createFromBitmap(rs, res);

        //Create allocation with same type
        Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

        //Create script from rs file.
        ScriptC_colorPicker colorPickerScript = new ScriptC_colorPicker(rs);

        colorPickerScript.set_width(width);
        colorPickerScript.set_height(height);
        colorPickerScript.set_circleRadius(circleRadius);

        //Call the first kernel.
        colorPickerScript.forEach_root(allocationA, allocationB);

        //Copy script result into bitmap
        allocationB.copyTo(res);

        //Destroy everything to free memory
        allocationA.destroy();
        allocationB.destroy();
        colorPickerScript.destroy();
        rs.destroy();

        return res;
    }


    private static void fillBitmapPixel(Bitmap rgbBitmap) {
        int width = rgbBitmap.getWidth();
        int height = rgbBitmap.getHeight();
        int[] pixels = new int[width * height];
        float[] hsv = new float[]{0f, 0f, 1f};
        int w = width;
        int h = height;
        int circleRadius = Math.min(width, height) / 2;
        int x = circleRadius, y = circleRadius;

        for (int i = pixels.length - 1; i >= 0; i--) {
            if ((i+1) % w == 0) {
                x = circleRadius;
                y--;
            }else{
                x--;
            }
            double centerDist = Math.sqrt(x * x + y * y);
            if (centerDist <= circleRadius) {
                float v = (float) (Math.atan2(y, x) / Math.PI * 180f);
                if (v < 0) {
                    v += 360;
                }
                hsv[0] = v;
                hsv[1] = (float) (centerDist / circleRadius);
                pixels[i] = Color.HSVToColor(hsv);
            }else{
                pixels[i] = 0x00000000;
            }
        }
        rgbBitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private static native  void nativeFillBitmapPixel(Bitmap bitmap);

    public void setColor( int color ) {
        Color.colorToHSV(color, colorHsv);
        invalidate();
    }

    public int getColor(){
        return Color.HSVToColor(colorHsv);
    }

    public int getColorForPoint(int x, int y, float[] hsv) {
        x -= ((contentRect.left + contentRect.right) / 2);
        y -= ((contentRect.top + contentRect.bottom) / 2);
        double centerDist = Math.sqrt(x * x + y * y);
        float v = (float) (Math.atan2(y, x) / Math.PI * 180f);
        Log.d(TAG, "v:" + v);
        if (v < 0) {
            v += 360;
        }
        Log.d(TAG, "v+360:" + v);
        hsv[0] = v;
        hsv[1] = Math.max(0f, Math.min(1f, (float) (centerDist / circleRadius)));
        hsv[2] = 1f;
        return Color.HSVToColor(hsv);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int action = event.getActionMasked();
        switch ( action ) {
            case MotionEvent.ACTION_DOWN:
                startDragging(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDragging()) {
                    updateDragging(event);
                } else {
                    startDragging(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopDragging();
                break;
        }
        invalidate();
        return true;
    }

    private void startDragging(MotionEvent ev) {
        mIsDragging = true;
    }

    private boolean isDragging() {
        return mIsDragging;
    }

    private void stopDragging() {
        mIsDragging = false;
        int colorForPoint = getColorForPoint(latest_x, latest_y, colorHsv);
        if ( listener != null ) {
            listener.onDragStop(colorForPoint);
        }
    }

    private void updateDragging(MotionEvent ev) {
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        Log.i(TAG,String.format("x=%d,y=%d",x,y));
        if (latest_x != x || latest_y != y) {
            latest_x = x;
            latest_y = y;
            int colorForPoint = getColorForPoint(latest_x, latest_y, colorHsv);
            if ( listener != null ) {
                listener.colorSelected(colorForPoint);
            }
        }
    }

    public OnColorSelectedListener getColorChangedListener() {
        return listener;
    }

    public void setColorChangedListener(OnColorSelectedListener colorChangedListener) {
        this.listener = colorChangedListener;
    }

    public interface OnColorSelectedListener {

        public void colorSelected(Integer color);

        /**
         * @param color Latest color that selected before stop drag.
         */
        public void onDragStop(Integer color);
    }

    private Bitmap getBitmap(Context context, String fileName) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = context.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    // 保存图片
    private void saveImagePNG(Context context, String fileName, Bitmap bitmap)
            throws IOException {
        saveImage(context, fileName, bitmap, 100, Bitmap.CompressFormat.PNG);
    }

    // 保存图片
    private void saveImage(Context context, String fileName,
                           Bitmap bitmap, int quality, Bitmap.CompressFormat format) throws IOException {
        if (bitmap == null || fileName == null || context == null)
            return;

        FileOutputStream fos = context.openFileOutput(fileName,
                Context.MODE_PRIVATE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, stream);
        byte[] bytes = stream.toByteArray();
        fos.write(bytes);
        fos.close();
    }


    public int getGenMethod() {
        return genMethod;
    }

    public void setGenMethod(int genMethod) {
        this.genMethod = genMethod;
    }
}
