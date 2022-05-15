package com.example.jack8.floatwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jack8.floatwindow.Window.WindowManager;
import com.jack8.floatwindow.Window.WindowStruct;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收螢幕方向改變廣播
 */
public class ScreenChangeListener extends BroadcastReceiver {
    private static ScreenChangeListener instance = null;
    private int screenOrientation;

    private ScreenChangeListener(Context context){
        updateOrientation(context);
    }

    public static ScreenChangeListener getInstance(Context context){
        if(instance == null)
            instance = new ScreenChangeListener(context);
        else//因為在用戶按下通知中心的關閉FloatWindow按鈕後，instance中的實例不一定會馬上被系統回收，如果用戶在實例還沒被回收時把螢幕轉個方向然後在次開啟FloatWindow，那麼instance的實例肯定與上一次用的是同一個，但是在方向廣播監聽解除後，螢幕已經轉了一個方向，因此screenOrientation中的方向一定與現在螢幕的方向不一樣，所以當用戶開著視窗然後再次將螢幕轉向，那麼視窗的座標將不會被改變。
            instance.updateOrientation(context);//所以當再次使用同一個實例時，必須先更新screenOrientation。
        return instance;
    }

    private void updateOrientation(Context context){
        screenOrientation = context.getResources().getConfiguration().orientation;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(screenOrientation != context.getResources().getConfiguration().orientation) {
            updateOrientation(context);
            for (Map.Entry<Integer, WindowStruct> entry : WindowManager.entrySet()) {
                WindowStruct windowStruct = entry.getValue();
                windowStruct.setPosition(
                        windowStruct.getPositionY(),
                        windowStruct.getPositionX()
                );
                if (windowStruct.nowState == WindowStruct.State.MINI) {
                    windowStruct.setGeneralPosition(
                            windowStruct.getGeneralPositionY(),
                            windowStruct.getGeneralPositionX()
                    );
                } else if (windowStruct.nowState == WindowStruct.State.MAX) {
                    int transitionsDuration = windowStruct.getTransitionsDuration();
                    windowStruct.setTransitionsDuration(0);
                    windowStruct.nowState = WindowStruct.State.GENERAL;
                    windowStruct.max();
                    windowStruct.setTransitionsDuration(transitionsDuration);
                }
            }
        }
    }
}
