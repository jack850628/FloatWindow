package com.example.jack8.floatwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jack8.floatwindow.Window.WindowManager;
import com.jack8.floatwindow.Window.WindowStruct;

import java.util.Map;

public class HomeKeyListener extends BroadcastReceiver {
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    private static HomeKeyListener instance = null;

//    private HashMap<Integer, WindowStruct> windowList;
//    private Field nowFocusNumber;

    private  HomeKeyListener(Context context){
//        try {//用反射取得所有視窗清單
//            Field field = WindowStruct.class.getDeclaredField("windowList");
//            field.setAccessible(true);
//            windowList = (HashMap<Integer,WindowStruct>)field.get(WindowStruct.class);
//            nowFocusNumber = WindowStruct.class.getDeclaredField("NOW_FOCUS_NUMBER");
//            nowFocusNumber.setAccessible(true);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }

    public static HomeKeyListener getInstance(Context context){
        if(instance == null)
            instance = new HomeKeyListener(context);
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(WindowManager.getFocusedWindowNumber() != -1 && intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if(reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)){
                for(Map.Entry<Integer, WindowStruct> entry : WindowManager.entrySet()){
                    if(entry.getValue().nowState != WindowStruct.State.HIDE && entry.getValue().nowState != WindowStruct.State.FULLSCREEN)
                        entry.getValue().mini();
                }
            }
        }
    }
}
