package com.example.jack8.floatwindow.Window;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.example.jack8.floatwindow.R;

/**
 * 視窗顏色
 */
public class WindowColor {
    private final static String WINDOW_COLOR="windowColor";//顏色設定檔的名稱
    private int windowBackground;//視窗內容區的顏色
    private int titleBar;//標題列的顏色
    private int sizeBar;//視窗大小調整條的顏色
    private int microMaxButtonBackground;//視窗隱藏、最小化、放大、縮小按鈕的顏色
    private int closeButtonBackground;//關閉視窗按鈕的顏色
    private int windowNotFoucs;//視窗失焦時的外框顏色
    private final SharedPreferences spf;
    public WindowColor(Context context){
        spf =  context.getSharedPreferences(WINDOW_COLOR,0);
        windowBackground=Color.parseColor(String.format("#%06X",
                (0xFFFFFF & spf.getInt("windowsBackground",context.getResources().getColor(R.color.windowsBackground)))));
        titleBar=Color.parseColor(String.format("#%06X",
                (0xFFFFFF & spf.getInt("titleBar",context.getResources().getColor(R.color.windowFoucsColor)))));
        sizeBar=Color.parseColor(String.format("#%06X",
                (0xFFFFFF & spf.getInt("sizeBar",context.getResources().getColor(R.color.windowFoucsColor)))));
        microMaxButtonBackground=Color.parseColor(String.format("#%06X",
                (0xFFFFFF & spf.getInt("microMaxButtonBackground",context.getResources().getColor(R.color.windowFoucsColor)))));
        closeButtonBackground=Color.parseColor(String.format("#%06X",
                (0xFFFFFF & spf.getInt("closeButtonBackground",context.getResources().getColor(R.color.closeButton)))));
        windowNotFoucs=Color.parseColor(String.format("#%06X",
                (0xFFFFFF & spf.getInt("windowNotFoucs",context.getResources().getColor(R.color.windowNotFoucsColor)))));
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
        editor.putInt("windowBackground",windowBackground);
        editor.putInt("titleBar",titleBar);
        editor.putInt("sizeBar",sizeBar);
        editor.putInt("microMaxButtonBackground",microMaxButtonBackground);
        editor.putInt("closeButtonBackground",closeButtonBackground);
        editor.putInt("windowNotFoucs",windowNotFoucs);
        editor.commit();
    }
}
