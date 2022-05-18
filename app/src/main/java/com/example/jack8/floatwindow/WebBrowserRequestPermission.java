package com.example.jack8.floatwindow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jack8.floatwindow.Window.WindowManager;
import com.jack8.floatwindow.Window.WindowStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebBrowserRequestPermission extends AppCompatActivity {
    public static final Map<String, String> WEBKIT_PERMISSION_MAP = new HashMap<>();
    public static final Map<String, Integer> WEBKIT_PERMISSION_NAME_MAP = new HashMap<>();

    private WindowStruct windowStruct;
    private Map<Integer, WindowStruct.State> miniWindow = null;
    private String[] permissionNames;

    public enum WebkitPermissionID{
        UNKNOWN("UNKNOWN", -1),
        RESOURCE_VIDEO_CAPTURE(PermissionRequest.RESOURCE_VIDEO_CAPTURE, 0),
        RESOURCE_AUDIO_CAPTURE(PermissionRequest.RESOURCE_AUDIO_CAPTURE, 1),
        RESOURCE_MIDI_SYSEX(PermissionRequest.RESOURCE_MIDI_SYSEX, 2),
        RESOURCE_PROTECTED_MEDIA_ID(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID, 3),
        ACCESS_COARSE_LOCATION(Manifest.permission.ACCESS_COARSE_LOCATION, 4);


        private String name;
        private int id;

        WebkitPermissionID(String name, int id){
            this.name = name;
            this.id = id;
        }

        public String getName(){
            return this.name;
        }

        public int getId() {
            return id;
        }

        public static WebkitPermissionID getWebkitPermissionID(String name){
            switch (name){
                case PermissionRequest.RESOURCE_VIDEO_CAPTURE:
                    return RESOURCE_VIDEO_CAPTURE;
                case PermissionRequest.RESOURCE_AUDIO_CAPTURE:
                    return RESOURCE_AUDIO_CAPTURE;
                case PermissionRequest.RESOURCE_MIDI_SYSEX:
                    return RESOURCE_MIDI_SYSEX;
                case PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID:
                    return RESOURCE_PROTECTED_MEDIA_ID;
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    return ACCESS_COARSE_LOCATION;
                default:
                    return UNKNOWN;
            }
        }
    }

    static {
        WEBKIT_PERMISSION_MAP.put(PermissionRequest.RESOURCE_AUDIO_CAPTURE, Manifest.permission.RECORD_AUDIO);
        WEBKIT_PERMISSION_MAP.put(PermissionRequest.RESOURCE_VIDEO_CAPTURE, Manifest.permission.CAMERA);

        WEBKIT_PERMISSION_NAME_MAP.put(PermissionRequest.RESOURCE_VIDEO_CAPTURE, R.string.camera);
        WEBKIT_PERMISSION_NAME_MAP.put(PermissionRequest.RESOURCE_AUDIO_CAPTURE, R.string.microphone);
        WEBKIT_PERMISSION_NAME_MAP.put(PermissionRequest.RESOURCE_MIDI_SYSEX, R.string.MIDI_Sysex);
        WEBKIT_PERMISSION_NAME_MAP.put(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID, R.string.protected_media);
        WEBKIT_PERMISSION_NAME_MAP.put(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.location);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        int windowNumber = getIntent().getIntExtra(WebRequestPermission.WINDOW_NUMBER, -1);
        if(windowNumber == -1){
            JackLog.writeLog(this, "Web Browser權限請求時Window Number丟失");
            crashlytics.log("Web Browser權限請求時Window Number丟失");
            finish();
        }
        permissionNames = getIntent().getStringArrayExtra(WebRequestPermission.PERMISSION_NAME);
        if(permissionNames == null)
            permissionNames = new String[]{};
        windowStruct = WindowManager.getWindowStruct(windowNumber);
        if(windowStruct == null){
            JackLog.writeLog(this, String.format("WebBrowser權限請求時視窗找不到 WebBrowserID: %d\n", windowNumber));
            crashlytics.log(String.format("WebBrowser權限請求時視窗找不到 WebBrowserID: %d\n", windowNumber));
            finish();
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            miniWindow = JTools.toMiniStateForAllWindow();
        new RequestPermission(this, new RequestPermission.Callback() {
            @Override
            public void callback(String[] success, String[] refuse) {
                ((WebBrowser) windowStruct.getConstructionAndDeconstructionWindow())
                        .getWebRequestPermission()
                        .getRequestPermissionResult()
                        .callback(success, refuse);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
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
                    .getWebRequestPermission()
                    .getRequestPermissionResult()
                    .callback(new String[]{}, permissionNames);
        }
    }


    public static class WebRequestPermission{
        public static final String WINDOW_NUMBER = "windowNumber";
        public static final String PERMISSION_NAME = "permissionName";

        private RequestPermission.Callback requestPermissionResult;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void requestToUser(Context context, PermissionRequest request, String webTitle, WindowStruct webWindow){
            checkRequestFromDB(0, request, new ArrayList<>(), context, webTitle, webWindow);
        }

        public void checkRequestFromDB(int index, PermissionRequest request, List<String> successPermissions, Context context, String webTitle, WindowStruct webWindow){
            JTools.threadPool.execute(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    DataBaseForBrowser.WebsitePermission websitePermission;
                    WebkitPermissionID webkitPermissionID = WebkitPermissionID.getWebkitPermissionID(request.getResources()[index]);
                    if(webkitPermissionID != WebkitPermissionID.UNKNOWN)
                        websitePermission = DataBaseForBrowser.getInstance(context).websitePermissionDao().getWebsitePermission(request.getOrigin().getHost(), webkitPermissionID.getId());
                    else
                        websitePermission = null;
                    JTools.uiThread.post(new Runnable() {
                        @Override
                        public void run() {
                            if(websitePermission != null){
                                successPermissions.add(request.getResources()[index]);
                                if(index + 1 < request.getResources().length)
                                    checkRequestFromDB(index + 1, request, successPermissions, context, webTitle, webWindow);
                                else
                                    requestToSystem(request, successPermissions, context, webWindow);
                            }else
                                requestToUser(index, request, successPermissions, context, webTitle, webWindow);
                        }
                    });
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void requestToUser(int index, PermissionRequest request, List<String> successPermissions, Context context, String webTitle, WindowStruct webWindow){
            String permissionName;
            if(WEBKIT_PERMISSION_NAME_MAP.containsKey(request.getResources()[index]))
                permissionName = context.getString(WEBKIT_PERMISSION_NAME_MAP.get(request.getResources()[index]));
            else
                permissionName = request.getResources()[index];
            View messageView = LayoutInflater.from(context).inflate(R.layout.alert, null);
            ((TextView)messageView.findViewById(R.id.message)).setText(String.format("%s: %s\n%s\"%s\"%s", context.getString(R.string.website), request.getOrigin().getHost(), context.getString(R.string.request), permissionName, context.getString(R.string.permission)));
            messageView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
            messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            JTools.createAlertWindow(context, messageView, webWindow)
                    .windowPageTitles(new String[]{context.getString(R.string.permission_request)})
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        @Override
                        public void Construction(Context context, View pageView, int position, Map<String, Object> args, final WindowStruct ws) {
                            pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    WebkitPermissionID webkitPermissionID = WebkitPermissionID.getWebkitPermissionID(request.getResources()[index]);
                                    successPermissions.add(request.getResources()[index]);
                                    if(webkitPermissionID != WebkitPermissionID.UNKNOWN) {
                                        JTools.threadPool.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                DataBaseForBrowser.getInstance(context).websitePermissionDao().addWebsitePermission(new DataBaseForBrowser.WebsitePermission(request.getOrigin().getHost(), webkitPermissionID.getId()));
                                            }
                                        });
                                    }
                                    ws.close();
                                }
                            });
                            pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ws.close();
                                }
                            });
                        }

                        @Override
                        public void onDestroy(Context context, WindowStruct windowStruct){
                            if(index + 1 < request.getResources().length)
                                checkRequestFromDB(index + 1, request, successPermissions, context, webTitle, webWindow);
                            else
                                requestToSystem(request, successPermissions, context, webWindow);
                        }
                    })
                    .show();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void requestToSystem(PermissionRequest request, List<String> successPermissions, Context context, WindowStruct webWindow){
            if(successPermissions.size() == 0){
                request.deny();
                return;
            }
            int count = 0;
            Map<String, String> sysPermissions = new HashMap();
            for(String permission: successPermissions){
                if(WebBrowserRequestPermission.WEBKIT_PERMISSION_MAP.containsKey(permission)) {
                    String p = WebBrowserRequestPermission.WEBKIT_PERMISSION_MAP.get(permission);
                    sysPermissions.put(p, permission);
                    if (context.checkCallingOrSelfPermission(p) == PackageManager.PERMISSION_GRANTED) {
                        count++;
                    }
                }
            }
            if(count == sysPermissions.size()){
                String[] p = new String[successPermissions.size()];
                successPermissions.toArray(p);
                request.grant(p);
                return;
            }
            requestPermissionResult = new RequestPermission.Callback() {
                @Override
                public void callback(String[] success, String[] refuse) {
                    for(String r: refuse){
                        if(sysPermissions.containsKey(r)){
                            successPermissions.remove(sysPermissions.get(r));
                        }
                    }
                    if(successPermissions.size() > 0) {
                        String[] p = new String[successPermissions.size()];
                        successPermissions.toArray(p);
                        request.grant(p);
                    }else
                        request.deny();
                }
            };
            String[] requestPermissionList = new String[sysPermissions.size()];
            sysPermissions.keySet().toArray(requestPermissionList);
            Intent intent = new Intent(context, WebBrowserRequestPermission.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PERMISSION_NAME, requestPermissionList);
            intent.putExtra(WINDOW_NUMBER, webWindow.getNumber());
            context.startActivity(intent);
        }

        public RequestPermission.Callback getRequestPermissionResult(){
            return requestPermissionResult;
        }

        public void requestGeolocationPermissions(Context context, String origin, String host, WindowStruct webWindow, GeolocationPermissions.Callback callback) {
            JTools.threadPool.execute(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    DataBaseForBrowser.WebsitePermission websitePermission = DataBaseForBrowser.getInstance(context).websitePermissionDao().getWebsitePermission(host, WebkitPermissionID.ACCESS_COARSE_LOCATION.getId());
                    JTools.uiThread.post(new Runnable() {
                        @Override
                        public void run() {
                            if(websitePermission != null){
                                requestGeolocationPermissionsToSystem(context, origin, host, webWindow, callback);
                            }else
                                requestGeolocationPermissionsToUsr(context, origin, host, webWindow, callback);
                        }
                    });
                }
            });
        }

        private void requestGeolocationPermissionsToUsr(Context context, String origin, String host, WindowStruct webWindow, GeolocationPermissions.Callback callback) {
            View messageView = LayoutInflater.from(context).inflate(R.layout.alert, null);
            ((TextView)messageView.findViewById(R.id.message)).setText(String.format("%s: %s\n%s\"%s\"%s", context.getString(R.string.website), host, context.getString(R.string.request), context.getString(R.string.location), context.getString(R.string.permission)));
            messageView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
            messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            JTools.createAlertWindow(context, messageView, webWindow)
                    .windowPageTitles(new String[]{context.getString(R.string.permission_request)})
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        boolean isConfirm = false;
                        @Override
                        public void Construction(Context context, View pageView, int position, Map<String, Object> args, final WindowStruct ws) {
                            pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    isConfirm = true;
                                    ws.close();
                                    JTools.threadPool.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            DataBaseForBrowser.getInstance(context).websitePermissionDao().addWebsitePermission(new DataBaseForBrowser.WebsitePermission(host, WebkitPermissionID.ACCESS_COARSE_LOCATION.getId()));
                                        }
                                    });
                                    requestGeolocationPermissionsToSystem(context, origin, host, webWindow, callback);
                                }
                            });
                            pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ws.close();
                                }
                            });
                        }

                        @Override
                        public void onDestroy(Context context, WindowStruct windowStruct){
                            if(!isConfirm) callback.invoke(origin, false, false);
                        }
                    })
                    .show();
        }
        private void requestGeolocationPermissionsToSystem(Context context, String origin, String host, WindowStruct webWindow, GeolocationPermissions.Callback callback) {
            if(
                    context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    &&
                    context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ){
                callback.invoke(origin, true, false);
                return;
            }
            requestPermissionResult = new RequestPermission.Callback() {
                @Override
                public void callback(String[] success, String[] refuse) {
                    if(refuse.length == 0)
                        callback.invoke(origin, true, false);
                    else
                        callback.invoke(origin, false, false);
                }
            };
            Intent intent = new Intent(context, WebBrowserRequestPermission.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PERMISSION_NAME, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
            intent.putExtra(WINDOW_NUMBER, webWindow.getNumber());
            context.startActivity(intent);
        }
    }
}
