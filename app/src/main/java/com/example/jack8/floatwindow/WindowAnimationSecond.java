package com.example.jack8.floatwindow;

import android.content.Context;

import com.example.jack8.floatwindow.Window.WindowConfig;

public class WindowAnimationSecond {
    private final static String SECOND = "Second";
    private final static String ANIMATION = "Animation";

    public static int getWindowSpeed(Context context){
        return context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(SECOND,500);
    }
    public static void setWindowSpeed(Context context,int speed){
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(SECOND,speed).commit();
    }

    public static boolean getWindowAnimation(Context context){

        return context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getBoolean(ANIMATION,false);
    }
    public static void setWindowAnimation(Context context,boolean animation){
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putBoolean(ANIMATION,animation).commit();
    }
}
