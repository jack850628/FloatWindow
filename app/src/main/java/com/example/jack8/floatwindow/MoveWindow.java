package com.example.jack8.floatwindow;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jack8.floatwindow.Window.WindowStruct;

/**
 * 無外框模式時的便條紙所用來移動視窗Touch Event
 */
public class MoveWindow implements View.OnTouchListener{
    private float H =- 1,W =- 1;

    Context context;
    WindowStruct windowStruct;

    public MoveWindow(Context context, WindowStruct windowStruct){
        this.context = context;
        this.windowStruct = windowStruct;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(windowStruct.nowState == WindowStruct.State.GENERAL) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (H == -1 || W == -1) {
                    H = event.getX();//取得點擊的X座標到視窗頂點的距離
                    W = event.getY();//取得點擊的Y座標到視窗頂點的距離
                    return false;
                }
                int x = (int) (event.getRawX() - H);
                int y = (int) (event.getRawY() - W - getStatusBarHeight());//60為狀態列高度
                if (v.getParent() != null)
                    y -= (v.getTop() - ((ViewGroup) v.getParent()).getTop());
                if (y < 0)
                    y = 0;
                windowStruct.setPosition(x, y);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                H = -1;
                W = -1;
            }
        }
        return false;
    }

    private int getStatusBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
}
