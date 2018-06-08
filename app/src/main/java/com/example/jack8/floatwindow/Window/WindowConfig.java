package com.example.jack8.floatwindow.Window;

import android.content.Context;

public class WindowConfig {
    public final static String WINDOW_CONF = "windowConf";//設定檔的名稱
    public final static String SECOND = "Second";
    public final static String WINDOW_BACKGROUND = "windowBackground";
    public final static String TITLE_BAR = "titleBar";
    public final static String SIZE_BAR = "sizeBar";
    public final static String MICRO_MAX_BUTTON_BACKGROUND = "microMaxButtonBackground";
    public final static String CLOSE_BUTTON_BACKGROUND = "closeButtonBackground";
    public final static String WINDOW_NOT_FOUSE = "windowNotFoucs";
    public static int getWindowSpeed(Context context){
        return context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(WindowConfig.SECOND,500);
    }
    public static void setWindowSpeed(Context context,int speed){
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(WindowConfig.SECOND,speed).commit();
    }
}
