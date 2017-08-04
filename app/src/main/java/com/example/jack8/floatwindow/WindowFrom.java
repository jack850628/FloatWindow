package com.example.jack8.floatwindow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class WindowFrom extends LinearLayout {
    final WindowManager wm;
    WindowManager.LayoutParams wmlp=null;
    public WindowFrom(Context context) {
        super(context);
        wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public WindowFrom(Context context, AttributeSet attrs) {
        super(context, attrs);
        wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public void setLayoutParams(WindowManager.LayoutParams wmlp){
        this.wmlp=wmlp;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(wmlp!=null) {
            wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;//讓視窗聚焦
            wm.updateViewLayout(this, wmlp);
        }
        return super.onInterceptTouchEvent(event);
    }
}
