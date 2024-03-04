package com.example.myapplication;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeListContainer extends LinearLayout {
    private int max_row=2;
    private LinearLayout row_linear_layout;
    private LayoutParams layoutParams;
    public HomeListContainer(Context context) {
        super(context);
        row_linear_layout=new LinearLayout(context);
        setOrientation(LinearLayout.VERTICAL);
        layoutParams=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        row_linear_layout.setLayoutParams(layoutParams);
        row_linear_layout.setOrientation(HORIZONTAL);
        this.addView(row_linear_layout);
    }
    public void setMax_row(int max_row){
        this.max_row=max_row;
    }
    public HomeListContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        row_linear_layout=new LinearLayout(context);
        setOrientation(LinearLayout.VERTICAL);
        layoutParams=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        row_linear_layout.setLayoutParams(layoutParams);
        row_linear_layout.setOrientation(HORIZONTAL);
        this.addView(row_linear_layout);
    }

    public void addView(View view,boolean t) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

// 设置控件的布局参数
        view.setLayoutParams(layoutParams);
        if(row_linear_layout.getChildCount()>max_row){
            row_linear_layout=new LinearLayout(getContext());
            row_linear_layout.setOrientation(HORIZONTAL);
            row_linear_layout.setLayoutParams(layoutParams);
            this.addView(row_linear_layout);
            row_linear_layout.addView(view);
        }else{
            try{
                row_linear_layout.addView(view);
            }catch (Exception e){
                Log.e("e",e.getMessage().toString());
            }
        }
    }
}

