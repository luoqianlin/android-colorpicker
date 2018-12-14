package com.colorpicker;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
Button btn_red;
Button btn_green;
Button btn_blue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final View view=findViewById(R.id.view);
        btn_red=findViewById(R.id.btn_red);
        btn_green=findViewById(R.id.btn_green);
        btn_blue=findViewById(R.id.btn_blue);
        final ColorPicker colorpicker=findViewById(R.id.colorpicker);
        colorpicker.setGenMethod(ColorPicker.NATIVE_CODE);
        colorpicker.setColorChangedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void colorSelected(Integer color) {
                view.setBackgroundColor(color);
            }

            @Override
            public void onDragStop(Integer color) {

            }
        });
        btn_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorpicker.setColor(Color.RED);
                view.setBackgroundColor(colorpicker.getColor());
            }
        });
        btn_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorpicker.setColor(Color.GREEN);
                view.setBackgroundColor(colorpicker.getColor());
            }
        });

        btn_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorpicker.setColor(Color.BLUE);
                view.setBackgroundColor(colorpicker.getColor());
            }
        });
    }
}
