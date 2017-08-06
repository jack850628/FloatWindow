package com.example.jack8.floatwindow;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
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
    View titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        titleBar=this.findViewById(R.id.title_bar);
        sizeBar=this.findViewById(R.id.size);
        microMaxButtonBackground=this.findViewById(R.id.micro_max_button_background);
        closeButtonBackground=this.findViewById(R.id.close_button_background);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(wmlp!=null) {
            wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;//讓視窗聚焦
            wm.updateViewLayout(this, wmlp);
            titleBar.setBackgroundColor(getResources().getColor(R.color.windowFoucsColor));
            sizeBar.setBackgroundColor(getResources().getColor(R.color.windowFoucsColor));
            microMaxButtonBackground.setBackgroundColor(getResources().getColor(R.color.windowFoucsColor));
            closeButtonBackground.setBackgroundColor(getResources().getColor(R.color.closeButton));
        }
        return super.onInterceptTouchEvent(event);
    }
}
