package com.example.jack8.floatwindow;

import android.content.Context;

import com.example.jack8.floatwindow.Window.WindowConfig;

public class WindowTransitionsDuration {
    private final static String SECOND = "Second";

    public static int getWindowTransitionsDuration(Context context){
        return context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(SECOND,500);
    }
    public static void setWindowTransitionsDuration(Context context,int speed){
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(SECOND,speed).commit();
    }
}
