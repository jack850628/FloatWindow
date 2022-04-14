package com.jack8.floatwindow.Window;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class FullscreenWindowActivity extends AppCompatActivity {

    public static final String WINDOW_NUMBER_EXTRA_NAME = "WINDOW_NUMBER";
    private WindowStruct.OnWindowTitleChangeListener onWindowTitleChangeListener;
    private int windowNumber;
    private View winform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fullscreen_window);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        windowNumber = getIntent().getIntExtra(WINDOW_NUMBER_EXTRA_NAME, -1);
        WindowStruct ws = WindowManager.getWindowStruct(windowNumber);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            if(ws == null){
                finish();
                return;
            }else if(ws.nowState != WindowStruct.State.FULLSCREEN){
                ws.fullscreen();
                return;
            }
        }else{
            onWindowTitleChangeListener = new WindowStruct.OnWindowTitleChangeListener() {
                @Override
                public void onTitleChanged(Context context, WindowStruct windowStruct, String title) {
                    setTaskDescription(new ActivityManager.TaskDescription(title));
                }
            };
            ws.addWindowTitleChangeListener(onWindowTitleChangeListener);
            setTaskDescription(new ActivityManager.TaskDescription(ws.getWindowTitle()));
        }
        ws.setFullscreenActivity(this);
        winform = ws.getWinformForFullScreenActivity();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        if(winform.getParent() != null){
//            if(!(winform.getParent() instanceof ViewGroup)){
//                WindowManager.getWindowStruct(windowNumber).fullscreen();
//                return;
//            }
            ((ViewGroup)winform.getParent()).removeView(winform);
        }
        ((ViewGroup)getWindow().getDecorView().findViewById(android.R.id.content)).addView(winform, layoutParams);
    }

    void exitFullscreen(){
        ((ViewGroup)winform.getParent()).removeView(winform);
        WindowManager.getWindowStruct(windowNumber).setFullscreenActivity(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WindowStruct ws = WindowManager.getWindowStruct(windowNumber);//當使用者是按X按鈕關視窗時，ws會為null
            if(ws != null) {
                ws.removeWindowTitleChangeListener(onWindowTitleChangeListener);
                if (ws.nowState == WindowStruct.State.FULLSCREEN) {//當使用者是是從多工畫面關掉視窗時，ws.nowState會等於WindowStruct.State.FULLSCREEN
                    ws.close();
                }
            }
        }
    }
}