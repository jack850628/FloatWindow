package com.example.jack8.floatwindow;

import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class NotePageLauncher extends AppCompatActivity {

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
        if(!intent.hasExtra(Intent.EXTRA_TEXT) && !intent.hasExtra(JTools.IntentParameter.PATH)){
            intent.putExtra(JTools.IntentParameter.PATH, "/" + NotePage.NODE_LIST);
        }
//        String url = null;
//        if(extra_intent.getStringExtra(Intent.EXTRA_TEXT) != null)//帶有外部字串的啟動方法
//            url = extra_intent.getStringExtra(Intent.EXTRA_TEXT);
        intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_NOTE_PAGE);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            startService(intent);
        else
            startForegroundService(intent);
        finish();
    }
}
