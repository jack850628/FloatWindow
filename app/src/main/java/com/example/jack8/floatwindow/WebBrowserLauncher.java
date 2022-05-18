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
            public void callback(String[] success, String[] refuse) {
                if(refuse.length == 0)
                    startFloatWindow();
                else
                    finish();
            }
        }).resultPermission();
    }
    private void startFloatWindow(){
        Intent intent = new Intent(this, FloatServer.class);
        Intent extra_intent = getIntent();
        JTools.intentExtraCopyToIntent(extra_intent, intent);
        if(extra_intent.getDataString() != null)
            intent.putExtra(Intent.EXTRA_TEXT, extra_intent.getDataString());
        if(intent.getStringExtra(Intent.EXTRA_TEXT) == null && intent.getStringExtra(JTools.IntentParameter.PATH) == null){
            intent.putExtra(JTools.IntentParameter.PATH, "/" + NotePage.NODE_LIST);
        }
        intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_WEB_BROWSER);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            startService(intent);
        else
            startForegroundService(intent);
        finish();
    }
}
