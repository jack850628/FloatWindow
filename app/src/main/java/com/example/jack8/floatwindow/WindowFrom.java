package com.example.jack8.floatwindow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class WindowFrom extends LinearLayout {
    final WindowManager wm;
    WindowManager.LayoutParams wmlp=null;
    public boolean isStart=true;
    public WindowFrom(Context context) {
        super(context);
        wColor=new WindowColor(context);
        wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public WindowFrom(Context context, AttributeSet attrs) {
        super(context, attrs);
        wColor=new WindowColor(context);
        wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public void setLayoutParams(WindowManager.LayoutParams wmlp){
        this.wmlp=wmlp;
    }
    View titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground;
    WindowColor wColor;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        titleBar=this.findViewById(R.id.title_bar);
        sizeBar=this.findViewById(R.id.size);
        microMaxButtonBackground=this.findViewById(R.id.micro_max_button_background);
        closeButtonBackground=this.findViewById(R.id.close_button_background);

        if(isStart){
            isStart=false;
            titleBar.setBackgroundColor(wColor.getTitleBar());
            sizeBar.setBackgroundColor(wColor.getSizeBar());
            microMaxButtonBackground.setBackgroundColor(wColor.getMicroMaxButtonBackground());
            closeButtonBackground.setBackgroundColor(wColor.getCloseButtonBackground());
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(wmlp!=null) {
            wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;//讓視窗聚焦
            wm.updateViewLayout(this, wmlp);
            titleBar.setBackgroundColor(wColor.getTitleBar());
            sizeBar.setBackgroundColor(wColor.getSizeBar());
            microMaxButtonBackground.setBackgroundColor(wColor.getMicroMaxButtonBackground());
            closeButtonBackground.setBackgroundColor(wColor.getCloseButtonBackground());
        }
        return super.onInterceptTouchEvent(event);
    }
}
