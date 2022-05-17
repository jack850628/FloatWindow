package com.example.jack8.floatwindow;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jack8.floatwindow.Window.WindowManager;
import com.jack8.floatwindow.Window.WindowStruct;

import java.util.HashMap;
import java.util.Map;

public class WebBrowserRequestPermission extends AppCompatActivity {
    public static final Map<String, String> WEBKIT_PERMISSION_MAP = new HashMap<>();

    WindowStruct windowStruct;
    Map<Integer, WindowStruct.State> miniWindow = null;

    static {
        WEBKIT_PERMISSION_MAP.put(PermissionRequest.RESOURCE_AUDIO_CAPTURE, Manifest.permission.RECORD_AUDIO);
        WEBKIT_PERMISSION_MAP.put(PermissionRequest.RESOURCE_VIDEO_CAPTURE, Manifest.permission.CAMERA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        int windowNumber = getIntent().getIntExtra(WebBrowser.WINDOW_NUMBER, -1);
        if(windowNumber == -1){
            JackLog.writeLog(this, "Web Browser權限請求時Window Number丟失");
            crashlytics.log("Web Browser權限請求時Window Number丟失");
            finish();
        }
        String[] permissionNames = getIntent().getStringArrayExtra(WebBrowser.PERMISSION_NAME);
        if(permissionNames == null)
            permissionNames = new String[]{};
        windowStruct = WindowManager.getWindowStruct(windowNumber);
        if(windowStruct == null){
            JackLog.writeLog(this, String.format("WebBrowser權限請求時視窗找不到 WebBrowserID: %d\n", windowNumber));
            crashlytics.log(String.format("WebBrowser權限請求時視窗找不到 WebBrowserID: %d\n", windowNumber));
            finish();
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            miniWindow = JTools.toMiniStateForAllWindow();
        new RequestPermission(this, new RequestPermission.Callback() {
            @Override
            public void callback() {
                ((WebBrowser)windowStruct.getConstructionAndDeconstructionWindow())
                        .getRequestPermissionSuccessCallback()
                        .callback();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                    JTools.setWindowsState(miniWindow);
                windowStruct = null;
                finish();
            }
        }, new RequestPermission.Callback() {
            @Override
            public void callback() {
               ((WebBrowser)windowStruct.getConstructionAndDeconstructionWindow())
                       .getRequestPermissionRefuseCallback()
                       .callback();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                    JTools.setWindowsState(miniWindow);
                windowStruct = null;
                finish();
            }
        }).resultPermission(permissionNames);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(windowStruct != null){
            ((WebBrowser)windowStruct.getConstructionAndDeconstructionWindow())
                    .getRequestPermissionRefuseCallback()
                    .callback();
        }
    }

}
