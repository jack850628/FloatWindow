package com.example.jack8.floatwindow;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.window);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&!Settings.canDrawOverlays(MainActivity.this))
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + MainActivity.this.getPackageName())), 1);
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
            intent.putExtra("intent",launcher);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                startService(intent);
            else
                startForegroundService(intent);
        }
        finish();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (Settings.canDrawOverlays(this))
                startFloatWindow();
            else
                finish();
        }
    }
}