package com.example.jack8.floatwindow;

import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class CalculatorLauncher extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        JTools.intentExtraCopyToIntent(getIntent(), intent);

        intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_CALCULATOR);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            startService(intent);
        else
            startForegroundService(intent);
        finish();
    }
}
