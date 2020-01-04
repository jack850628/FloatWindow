package com.example.jack8.floatwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jack8.floatwindow.Window.WindowStruct;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收螢幕方向改變廣播
 */
public class ScreenChangeListener extends BroadcastReceiver {
    private static final String BCAST_CONFIGCHANGED ="android.intent.action.CONFIGURATION_CHANGED";
    private static ScreenChangeListener instance = null;

    private HashMap<Integer,WindowStruct> windowList;
    private int screenOrientation;

    private  ScreenChangeListener(Context context){
        screenOrientation = context.getResources().getConfiguration().orientation;
        try {//用反射取得所有視窗清單
            Field field = WindowStruct.class.getDeclaredField("windowList");
            field.setAccessible(true);
            windowList = (HashMap<Integer,WindowStruct>)field.get(WindowStruct.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ScreenChangeListener getInstance(Context context){
        if(instance == null)
            instance = new ScreenChangeListener(context);
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(BCAST_CONFIGCHANGED) && screenOrientation != context.getResources().getConfiguration().orientation) {
            screenOrientation = context.getResources().getConfiguration().orientation;
            if(windowList != null)
                for(Map.Entry<Integer,WindowStruct> entry : windowList.entrySet()){
                    WindowStruct windowStruct = entry.getValue();
                    windowStruct.setPosition(
                            windowStruct.getPositionY(),
                            windowStruct.getPositionX()
                    );
                    if(windowStruct.nowState == WindowStruct.State.MINI){
                        windowStruct.setGeneralPosition(
                                windowStruct.getGeneralPositionY(),
                                windowStruct.getGeneralPositionX()
                        );
                    }else if(windowStruct.nowState == WindowStruct.State.MAX){
                        int transitionsDuration = windowStruct.getTransitionsDuration();
                        windowStruct.setTransitionsDuration(0);
                        windowStruct.nowState = WindowStruct.State.GENERAL;
                        windowStruct.max();
                        windowStruct.setTransitionsDuration(transitionsDuration);
                    }
                }
            /*if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.i("螢幕方向", "橫向");
            }else{
                Log.i("螢幕方向", "直向");
            }*/
        }
    }
}
