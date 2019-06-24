package com.jack8.floatwindow.Window;

import android.content.Context;
import android.content.SharedPreferences;

import com.jack8.floatwindow.R;

/**
 * 視窗顏色
 */
public class WindowColor {
    private int windowBackground;//視窗內容區的顏色
    private int titleBar;//標題列的顏色
    private int sizeBar;//視窗大小調整條的顏色
    private int microMaxButtonBackground;//視窗隱藏、最小化、放大、縮小按鈕的顏色
    private int closeButtonBackground;//關閉視窗按鈕的顏色
    private int windowNotFoucs;//視窗失焦時的外框顏色
    private final SharedPreferences spf;
    public WindowColor(Context context){
        spf =  context.getSharedPreferences(WindowConfig.WINDOW_CONF,0);
        /*windowBackground=Color.parseColor(String.format("#%06X",
                (0xFFFFFF & spf.getInt(WindowConfig.WINDOW_BACKGROUND,context.getResources().getColor(R.color.windowBackground)))));*/
        windowBackground = spf.getInt(WindowConfig.WINDOW_BACKGROUND,context.getResources().getColor(R.color.windowBackground));
        titleBar = spf.getInt(WindowConfig.TITLE_BAR,context.getResources().getColor(R.color.windowFoucsColor));
        sizeBar = spf.getInt(WindowConfig.SIZE_BAR,context.getResources().getColor(R.color.windowFoucsColor));
        microMaxButtonBackground = spf.getInt(WindowConfig.MICRO_MAX_BUTTON_BACKGROUND,context.getResources().getColor(R.color.windowFoucsColor));
        closeButtonBackground = spf.getInt(WindowConfig.CLOSE_BUTTON_BACKGROUND,context.getResources().getColor(R.color.closeButton));
        windowNotFoucs = spf.getInt(WindowConfig.WINDOW_NOT_FOUSE,context.getResources().getColor(R.color.windowNotFoucsColor));
    }
    public int getWindowBackground(){
        return windowBackground;
    }
    public void setWindowBackground(int color){
        windowBackground=color;
    }
    public int getTitleBar(){
        return titleBar;
    }
    public void setTitleBar(int color){
        titleBar=color;
    }
    public int getSizeBar(){
        return sizeBar;
    }
    public void setSizeBar(int color){
        sizeBar=color;
    }
    public int getMicroMaxButtonBackground(){
        return microMaxButtonBackground;
    }
    public void setMicroMaxButtonBackground(int color){
        microMaxButtonBackground=color;
    }
    public int getCloseButtonBackground(){
        return closeButtonBackground;
    }
    public void setCloseButtonBackground(int color){
        closeButtonBackground=color;
    }
    public int getWindowNotFoucs(){
        return windowNotFoucs;
    }
    public void setWindowNotFoucs(int color){
        windowNotFoucs=color;
    }
    public void save(){
        SharedPreferences.Editor editor=spf.edit();
        editor.putInt(WindowConfig.WINDOW_BACKGROUND,windowBackground);
        editor.putInt(WindowConfig.TITLE_BAR,titleBar);
        editor.putInt(WindowConfig.SIZE_BAR,sizeBar);
        editor.putInt(WindowConfig.MICRO_MAX_BUTTON_BACKGROUND,microMaxButtonBackground);
        editor.putInt(WindowConfig.CLOSE_BUTTON_BACKGROUND,closeButtonBackground);
        editor.putInt(WindowConfig.WINDOW_NOT_FOUSE,windowNotFoucs);
        editor.commit();
    }
}
