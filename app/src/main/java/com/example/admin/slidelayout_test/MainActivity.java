package com.example.admin.slidelayout_test;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends Activity implements View.OnTouchListener,FlipperLayout.TouchListener {
    public static int position = 0;
    FlipperLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rootLayout = (FlipperLayout) findViewById(R.id.container);
        View recoverView = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_new, null);

        TextView textView1 = (TextView) recoverView.findViewById(R.id.main_textview);
        textView1.setText("this is currenton page.");

        View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_new, null);


        View view2 = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_new, null);
        TextView textView2 = (TextView) view2.findViewById(R.id.main_textview);
        textView2.setText("this is currentdown page.");

        TextView textView = (TextView) view1.findViewById(R.id.main_textview);
        textView.setText("this is currentshow page.");

//        System.out.printf("textview width: %d, height: %d.", textView.getWidth(), textView.getHeight());

        rootLayout.initFlipperViews(MainActivity.this, view2, view1, recoverView);
    }




    @Override
    public View createView(final int direction) {
//        System.out.println("cteatview:"+position);
        if (direction == MOVE_TO_LEFT) {
            position --;
        } else {
            position++;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.view_new,null);
        TextView textView = (TextView) view.findViewById(R.id.main_textview);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.newview_linearlayout);
        Random random = new Random();
        int r = random.nextInt(256);
        int g= random.nextInt(256);
        int b = random.nextInt(256);
        int mColor = Color.rgb(r, g, b);
//        linearLayout.setBackgroundColor(mColor);

//        System.out.printf("createview-->textview width: %d, height: %d.",textView.getWidth(),textView.getHeight());
        textView.setText("this is "+position+" page.");
        r = random.nextInt(256);
        g= random.nextInt(256);
        b = random.nextInt(256);
        mColor = Color.rgb(r, g, b);

        textView.setBackgroundColor(mColor);

        return view;
    }

    @Override
    public boolean whetherHasPreviousPage() {
        return position != 0;
    }

    @Override
    public boolean whetherHasNextPage() {
        return true;
    }

    @Override
    public boolean currentIsFirstPage() {
        return position == 0;
    }

    @Override
    public boolean currentIsLastPage() {
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}