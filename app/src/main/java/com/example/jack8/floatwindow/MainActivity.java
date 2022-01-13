package com.example.jack8.floatwindow;

import android.app.Activity;
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
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        startFloatWindow();
                    }else{
                        finish();
                    }
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.window);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this))
            activityResultLauncher.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + MainActivity.this.getPackageName())));
        else {
            startFloatWindow();
        }
    }
    private void startFloatWindow(){
        Intent intent = new Intent(this, FloatServer.class);
        int launcher = getIntent().getIntExtra(FloatServer.LAUNCHER, FloatServer.OPEN_NONE);
        if(launcher == FloatServer.OPEN_SETTING){
            intent.setClass(this, Setting.class);
            startActivity(intent);
        }else{
            if(launcher == FloatServer.OPEN_NONE)
                launcher = FloatServer.OPEN_MAIN_MENU;
            intent.putExtra(FloatServer.INTENT,launcher);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                startService(intent);
            else
                startForegroundService(intent);
        }
        finish();
    }
}