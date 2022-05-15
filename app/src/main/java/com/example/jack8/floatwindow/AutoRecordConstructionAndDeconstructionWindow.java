package com.example.jack8.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.CallSuper;

import com.jack8.floatwindow.Window.WindowStruct;

import java.util.HashMap;
import java.util.Map;

public class AutoRecordConstructionAndDeconstructionWindow extends WindowStruct.constructionAndDeconstructionWindow {
    public static final String PAGE_POSITION = "pagePosition";

    protected String uriStr;
    protected Map<String, String> querys = new HashMap<>();
    protected String[] pathLayers = new String[]{};

    private Class appLauncherClass;
    private String[] finalPathLayers;
    private Map<String, Object> args;
    private Context context;
    public boolean doNotDeleteUri = false;

    public AutoRecordConstructionAndDeconstructionWindow(Class appLauncherClass){
        this.appLauncherClass = appLauncherClass;
    }

    public void updateUri(WindowStruct windowStruct, Context context){
        if(windowStruct.nowState == WindowStruct.State.CLOSE) return;
        this.finalPathLayers = new String[pathLayers.length + 1];
        this.finalPathLayers[0] = appLauncherClass.getName();
        for(int i = 0; i < pathLayers.length; i++){
            this.finalPathLayers[i + 1] = pathLayers[i];
        }
        uriStr = JTools.createAppUri(this.finalPathLayers, windowStruct, context, querys);
        Log.d("FloatWindow uri string", uriStr);
        JTools.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                DataBase.getInstance(context)
                        .workingWindowDao()
                        .addWorkingWindow(new DataBase.WorkingWindow(
                                windowStruct.getNumber(),
                                uriStr
                        ));
            }
        });
    }



    public static Map<String, Object> createArgs(Intent intent){
        Map<String, Object> args = new HashMap<>();
        if(intent.hasExtra(PAGE_POSITION)){
            args.put(PAGE_POSITION, Integer.valueOf(intent.getStringExtra(PAGE_POSITION)));
        }
        return args;
    }

    @Override
    @CallSuper
    public void onCreate(Context context, Map<String, Object> args, WindowStruct windowStruct){
        this.args = args;
        this.context = context;
        updateUri(windowStruct, context);
        windowStruct.addWindowSizeChangeListener(new WindowStruct.OnWindowSizeChangeListener() {
            @Override
            public void onSizeChanged(Context context, WindowStruct windowStruct) {
                updateUri(windowStruct, context);
            }
        });
        windowStruct.addWindowPositionChangeListener(new WindowStruct.OnWindowPositionChangeListener() {
            @Override
            public void onPositionChanged(Context context, WindowStruct windowStruct) {
                updateUri(windowStruct, context);
            }
        });
        windowStruct.addWindowStateChangeListener(new WindowStruct.OnWindowStateChangeListener() {
            @Override
            public void onStateChanged(Context context, WindowStruct windowStruct) {
                updateUri(windowStruct, context);
            }
        });
    }

    @Override
    @CallSuper
    public void onPageInitialized(Context context, WindowStruct windowStruct){
        if(args.containsKey(PAGE_POSITION)){
            windowStruct.showPage((int) args.get(PAGE_POSITION));
        }
    }

    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct){
        querys.put(PAGE_POSITION, String.valueOf(position));
        updateUri(windowStruct, context);
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct){

    }

    @Override
    @CallSuper
    public void onDestroy(Context context, WindowStruct windowStruct){
        if(!doNotDeleteUri) {
            JTools.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    DataBase.getInstance(context)
                            .workingWindowDao()
                            .deleteWorkingWindow(windowStruct.getNumber());
                }
            });
        }
    }
}
