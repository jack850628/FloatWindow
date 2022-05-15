package com.example.jack8.floatwindow;

import android.content.Context;
import androidx.annotation.IntRange;

import com.jack8.floatwindow.Window.WindowConfig;

public class WindowParameter {
    private static final String SECOND = "Second";
    private static final String BUTTONS_HEIGHT = "ButtonsHeight";
    private static final String BUTTONS_WIDTH = "ButtonsWidth";
    private static final String SIZE_BAR_HEIGHT = "SizeBarHeight";
    private static final String BUTTON_WIDTH_FOR_MINI_STATE = "ButtonWidthForMiniState";
    private static final String BUTTON_HEIGHT_FOR_MINI_STATE = "ButtonHeightForMiniState";
    private static final String AUTO_RUN = "autoRun";
    private static final String PERMANENT = "permanent";
    private static final String WHAT_IS_NEW_VERSION = "whatIsNewVersion";

    private static int SECOND_TEMP = -1;
    private static int BUTTONS_HEIGHT_TEMP = -1;
    private static int BUTTONS_WIDTH_TEMP = -1;
    private static int SIZE_BAR_HEIGHT_TEMP = -1;
    private static int BUTTON_WIDTH_FOR_MINI_STATE_TEMP = -1;
    private static int BUTTON_HEIGHT_FOR_MINI_STATE_TEMP = -1;
    private static int AUTO_RUN_TEMP = -1;
    private static int PERMANENT_TEMP = -1;
    private static String WHAT_IS_NEW_VERSION_TEMP = null;

    public static int getWindowTransitionsDuration(Context context){
        if(SECOND_TEMP == -1)
            SECOND_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(SECOND,500);
        return SECOND_TEMP;
    }
    public static void setWindowTransitionsDuration(Context context,@IntRange(from = 0) int speed){
        SECOND_TEMP = speed;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(SECOND,speed).apply();
    }

    /**
     * 取得視窗按鈕高度
     * @param context app context
     * @return 視窗按鈕高度，單位dp
     */
    public static int getWindowButtonsHeight(Context context){
        if(BUTTONS_HEIGHT_TEMP == -1)
            BUTTONS_HEIGHT_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(BUTTONS_HEIGHT,30);
        return BUTTONS_HEIGHT_TEMP;
    }

    /**
     * 是否開機後自動執行
     * @param context app context
     * @return 是否開機後自動執行
     */
    public static boolean isAutoRun(Context context){
        if(AUTO_RUN_TEMP == -1)
            AUTO_RUN_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getBoolean(AUTO_RUN,false) ? 1 : 0;
        return AUTO_RUN_TEMP == 1;
    }

    /**
     * 關閉視窗後通知是否繼續常駐
     * @param context app context
     * @return 關閉視窗後通知是否繼續常駐
     */
    public static boolean isPermanent(Context context){
        if(PERMANENT_TEMP == -1)
            PERMANENT_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getBoolean(PERMANENT,false) ? 1 : 0;
        return PERMANENT_TEMP == 1;
    }

    /**
     * 設定視窗按鈕高度
     * @param context app context
     * @param buttonsHeight 視窗按鈕高度，單位dp
     */
    public static void setWindowButtonsHeight(Context context,@IntRange(from = 0) int buttonsHeight){
        BUTTONS_HEIGHT_TEMP = buttonsHeight;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(BUTTONS_HEIGHT,buttonsHeight).apply();
    }

    /**
     * 取得視窗按鈕寬度
     * @param context app context
     * @return 視窗按鈕寬度，單位dp
     */
    public static int getWindowButtonsWidth(Context context){
        if(BUTTONS_WIDTH_TEMP == -1)
            BUTTONS_WIDTH_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(BUTTONS_WIDTH,30);
        return BUTTONS_WIDTH_TEMP;
    }

    /**
     *
     * @param context app context
     * @param buttonsWidth 視窗按鈕寬度，單位dp
     */
    public static void setWindowButtonsWidth(Context context,@IntRange(from = 0) int buttonsWidth){
        BUTTONS_WIDTH_TEMP = buttonsWidth;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(BUTTONS_WIDTH,buttonsWidth).apply();
    }

    /**
     * 取得視窗大小調整列高度
     * @param context app context
     * @return 視窗大小調整高度，單位dp
     */
    public static int getWindowSizeBarHeight(Context context){
        if(SIZE_BAR_HEIGHT_TEMP == -1)
            SIZE_BAR_HEIGHT_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(SIZE_BAR_HEIGHT,10);
        return SIZE_BAR_HEIGHT_TEMP;
    }

    /**
     *
     * @param context app context
     * @param sizeBarHeight 視窗大小調整高度，單位dp
     */
    public static void setWindowSizeBarHeight(Context context,@IntRange(from = 0) int sizeBarHeight){
        SIZE_BAR_HEIGHT_TEMP = sizeBarHeight;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(SIZE_BAR_HEIGHT,sizeBarHeight).apply();
    }

    /**
     * 取得最小化狀態視窗按鈕寬度
     * @param context app context
     * @return 最小化狀態視窗按鈕寬度，單位dp
     */
    public static int getButtonWidthForMiniState(Context context){
        if(BUTTON_WIDTH_FOR_MINI_STATE_TEMP == -1)
            BUTTON_WIDTH_FOR_MINI_STATE_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(BUTTON_WIDTH_FOR_MINI_STATE,30);
        return BUTTON_WIDTH_FOR_MINI_STATE_TEMP;
    }

    /**
     *
     * @param context app context
     * @param buttonWidthForMiniState 最小化狀態視窗按鈕寬度，單位dp
     */
    public static void setButtonWidthForMiniState(Context context,@IntRange(from = 0) int buttonWidthForMiniState){
        BUTTON_WIDTH_FOR_MINI_STATE_TEMP = buttonWidthForMiniState;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(BUTTON_WIDTH_FOR_MINI_STATE,buttonWidthForMiniState).apply();
    }

    /**
     * 取得最小化狀態視窗按鈕高度
     * @param context app context
     * @return 最小化狀態視窗按鈕高度，單位dp
     */
    public static int getButtonHeightForMiniState(Context context){
        if(BUTTON_HEIGHT_FOR_MINI_STATE_TEMP == -1)
            BUTTON_HEIGHT_FOR_MINI_STATE_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(BUTTON_HEIGHT_FOR_MINI_STATE,30);
        return BUTTON_HEIGHT_FOR_MINI_STATE_TEMP;
    }

    /**
     *
     * @param context app context
     * @param buttonHeightForMiniState 最小化狀態視窗按鈕高度，單位dp
     */
    public static void setButtonHeightForMiniState(Context context,@IntRange(from = 0) int buttonHeightForMiniState){
        BUTTON_HEIGHT_FOR_MINI_STATE_TEMP = buttonHeightForMiniState;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putInt(BUTTON_HEIGHT_FOR_MINI_STATE,buttonHeightForMiniState).apply();
    }

    /**
     * 設定是否開機後自動執行
     * @param context app context
     * @param value 是否開機後自動執行
     */
    public static void setAutoRun(Context context, boolean value){
        AUTO_RUN_TEMP = value ? 1 : 0;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putBoolean(AUTO_RUN,value).apply();
    }

    /**
     * 設定關閉視窗後通知是否繼續常駐
     * @param context app context
     * @param value 關閉視窗後通知是否繼續常駐
     */
    public static void setPermanent(Context context, boolean value){
        PERMANENT_TEMP = value ? 1 : 0;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putBoolean(PERMANENT,value).apply();;
    }

    /**
     * 取得新功能版本號
     * @param context app context
     * @return 新功能版本號
     */
    public static String getWhatIsNewVersion(Context context){
        if(WHAT_IS_NEW_VERSION_TEMP == null)
            WHAT_IS_NEW_VERSION_TEMP = context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getString(WHAT_IS_NEW_VERSION,"");
        return WHAT_IS_NEW_VERSION_TEMP;
    }

    /**
     * 設定新功能版本號
     * @param context app context
     */
    public static void setWhatIsNewVersion(Context context, String whatIsNewVersion){
        WHAT_IS_NEW_VERSION_TEMP = whatIsNewVersion;
        context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).edit().putString(WHAT_IS_NEW_VERSION, whatIsNewVersion).apply();
    }
}
