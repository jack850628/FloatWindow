package com.example.jack8.floatwindow;

import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class WebBrowserLauncher extends AppCompatActivity {

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
        Intent intent=new Intent(this, FloatServer.class);
        Intent extra_intent = getIntent();
        String url = null;
        if(extra_intent.getStringExtra(Intent.EXTRA_TEXT) != null)//帶有外部字串的啟動方法
            url = extra_intent.getStringExtra(Intent.EXTRA_TEXT);
        else if(extra_intent.getDataString() != null)//呼叫瀏覽器的的啟動方式
            url = extra_intent.getDataString();
        if(url == null) {
            intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_WEB_BROWSER);
        }else{
            intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_WEB_BROWSER | FloatServer.OPEN_EXTRA_URL);
            intent.putExtra(FloatServer.EXTRA_URL, url);
            intent.putExtra(FloatServer.BROWSER_MODE, extra_intent.getIntExtra(FloatServer.BROWSER_MODE, -1));
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            startService(intent);
        else
            startForegroundService(intent);
        finish();
    }
}
