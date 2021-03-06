package com.example.jack8.floatwindow;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class CalculatorLauncher extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.window);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&!Settings.canDrawOverlays(CalculatorLauncher.this))
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + CalculatorLauncher.this.getPackageName())), 1);
        else {
            startFloatWindow();
        }
    }
    private void startFloatWindow(){
        Intent intent=new Intent(this, FloatServer.class);

        intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_CALCULATOR);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            startService(intent);
        else
            startForegroundService(intent);
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
