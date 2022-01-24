package com.example.jack8.floatwindow;

import android.content.Intent;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new RequestPermission(this, new RequestPermission.Callback() {
            @Override
            public void callback() {
                startFloatWindow();
            }
        }, new RequestPermission.Callback() {
            @Override
            public void callback() {
                finish();
            }
        }).resultPermission();
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