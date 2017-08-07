package com.example.jack8.floatwindow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class WindowColor {
    private final static String WINDOW_COLOR="windowColor";
    private int titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground,windowNotFoucs;
    private final SharedPreferences spf;
    public WindowColor(Context context){
        spf =  context.getSharedPreferences(WINDOW_COLOR,0);
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
    public int getTitleBar(){
        return titleBar;
    }
    public void setTitleBar(int color){
        SharedPreferences.Editor editor=spf.edit();
        editor.putInt("titleBar",color);
        editor.commit();
        titleBar=color;
    }
    public int getSizeBar(){
        return sizeBar;
    }
    public void setSizeBar(int color){
        SharedPreferences.Editor editor=spf.edit();
        editor.putInt("sizeBar",color);
        editor.commit();
        sizeBar=color;
    }
    public int getMicroMaxButtonBackground(){
        return microMaxButtonBackground;
    }
    public void setMicroMaxButtonBackground(int color){
        SharedPreferences.Editor editor=spf.edit();
        editor.putInt("microMaxButtonBackground",color);
        editor.commit();
        microMaxButtonBackground=color;
    }
    public int getCloseButtonBackground(){
        return closeButtonBackground;
    }
    public void setCloseButtonBackground(int color){
        SharedPreferences.Editor editor=spf.edit();
        editor.putInt("closeButtonBackground",color);
        editor.commit();
        closeButtonBackground=color;
    }
    public int getWindowNotFoucs(){
        return windowNotFoucs;
    }
    public void setWindowNotFoucs(int color){
        SharedPreferences.Editor editor=spf.edit();
        editor.putInt("windowNotFoucs",color);
        editor.commit();
        windowNotFoucs=color;
    }
}
