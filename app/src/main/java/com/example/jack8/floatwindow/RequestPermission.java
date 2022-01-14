package com.example.jack8.floatwindow;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;


public class RequestPermission {
    public interface Callback{
        void callback();
    }

    private final ActivityResultLauncher<String[]> activityResultLauncher;

    public RequestPermission(final AppCompatActivity activity, final Callback success, final Callback refuse){

        ActivityResultLauncher<Intent> overlaysPermissionResultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onActivityResult(ActivityResult result) {
                        if(Settings.canDrawOverlays(activity)){//ACTION_MANAGE_OVERLAY_PERMISSION請求後不會有回傳結果，因此用result.getResultCode() == Activity.RESULT_OK是沒意義的，參考：https://stackoverflow.com/questions/41603332/onrequestpermissionsresult-not-being-triggered-for-overlay-permission
                            success.callback();
                        }else{
                            refuse.callback();
                        }
                    }
                }
        );

        activityResultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        for(Map.Entry<String, Boolean> entry : result.entrySet()){
                            if(!entry.getValue()){
                                refuse.callback();
                                return;
                            }
                        }
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity))
                            overlaysPermissionResultLauncher.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName())));
                        else {
                            success.callback();
                        }
                    }
                }
        );
    }

    public void resultPermission(){
        activityResultLauncher.launch(new String[]{
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        });
    }
}
