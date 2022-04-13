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
        screenOrientation = context.getResources().getConfiguration().orientation;
    }

    public static ScreenChangeListener getInstance(Context context){
        if(instance == null)
            instance = new ScreenChangeListener(context);
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(screenOrientation != context.getResources().getConfiguration().orientation) {
            screenOrientation = context.getResources().getConfiguration().orientation;
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
