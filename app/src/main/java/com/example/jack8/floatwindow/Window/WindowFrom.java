package com.example.jack8.floatwindow.Window;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.jack8.floatwindow.R;

public class WindowFrom extends LinearLayout {
    final WindowManager wm;
    WindowStruct WS;
    public boolean inited = false;//判斷視窗畫面是否初始化完成
    public WindowFrom(Context context) {
        super(context);
        wColor = new WindowColor(context);
        wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public WindowFrom(Context context, AttributeSet attrs) {
        super(context, attrs);
        wColor = new WindowColor(context);
        wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public void seWindowStruct(WindowStruct WS){
        this.WS = WS;
    }
    View titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground;
    WindowColor wColor;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        titleBar = this.findViewById(R.id.title_bar);
        sizeBar = this.findViewById(R.id.size);
        microMaxButtonBackground = this.findViewById(R.id.micro_max_button_background);
        closeButtonBackground = this.findViewById(R.id.close_button_background);
        findViewById(R.id.menu_list_and_context).setBackgroundColor(wColor.getWindowBackground());

        if(!inited){
            inited = true;
            if(WS != null) {
                if(WindowStruct.NOW_FOCUS_NUMBER == WS.Number)//如果被觸碰的視窗編號是現在焦點視窗編號
                    setWindowStyleOfFocus();
                else
                    setWindowStyleOfUnFocus();
            }
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(WS != null) {
            if(WindowStruct.NOW_FOCUS_NUMBER != WS.Number){//如果被觸碰的視窗編號不是現在焦點視窗編號
                WS.focusWindow();
                return true;//防止點擊事件因為至視窗至頂切換變成長按事件
            }
        }
        return super.onInterceptTouchEvent(event);
    }
    public void setWindowStyleOfFocus(){
        if(!inited)
            return;
        titleBar.setBackgroundColor(wColor.getTitleBar());
        sizeBar.setBackgroundColor(wColor.getSizeBar());
        microMaxButtonBackground.setBackgroundColor(wColor.getMicroMaxButtonBackground());
        closeButtonBackground.setBackgroundColor(wColor.getCloseButtonBackground());
    }
    public void setWindowStyleOfUnFocus(){
        if(!inited)
            return;
        titleBar.setBackgroundColor(wColor.getWindowNotFoucs());
        sizeBar.setBackgroundColor(wColor.getWindowNotFoucs());
        microMaxButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
        closeButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
    }
}
