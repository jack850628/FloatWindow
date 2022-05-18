package com.example.jack8.floatwindow;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.webkit.PermissionRequest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RequestPermission {
    public interface Callback{
        void callback(String[] success, String[] refuse);
    }

    private final ActivityResultLauncher<String[]> activityResultLauncher;

    private final List<String> success = new ArrayList<>(), refuse = new ArrayList<>();

    public RequestPermission(final AppCompatActivity activity, final Callback result){

        ActivityResultLauncher<Intent> overlaysPermissionResultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onActivityResult(ActivityResult r) {
                        if(Settings.canDrawOverlays(activity)){//ACTION_MANAGE_OVERLAY_PERMISSION請求後不會有回傳結果，因此用result.getResultCode() == Activity.RESULT_OK是沒意義的，參考：https://stackoverflow.com/questions/41603332/onrequestpermissionsresult-not-being-triggered-for-overlay-permission
                            success.add(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        }else{
                            refuse.add(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        }
                        String[] _success = new String[success.size()], _refuse = new String[refuse.size()];
                        success.toArray(_success);
                        refuse.toArray(_refuse);
                        result.callback(_success, _refuse);
                        success.clear();
                        refuse.clear();
                    }
                }
        );

        activityResultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> r) {
                        for(Map.Entry<String, Boolean> entry : r.entrySet()){
                            if(entry.getValue()){
                                success.add(entry.getKey());
                            }else{
                                refuse.add(entry.getKey());
                            }
                        }
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity))
                            overlaysPermissionResultLauncher.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName())));
                        else {
                            String[] _success = new String[success.size()], _refuse = new String[refuse.size()];
                            success.toArray(_success);
                            refuse.toArray(_refuse);
                            result.callback(_success, _refuse);
                            success.clear();
                            refuse.clear();
                        }
                    }
                }
        );
    }

    public void resultPermission(){
        activityResultLauncher.launch(new String[]{
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
        });
    }
    public void resultPermission(String[] permissions){
        activityResultLauncher.launch(permissions);
    }
}
