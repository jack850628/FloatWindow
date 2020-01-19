package com.example.jack8.floatwindow;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NotePageLauncher extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.window);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&!Settings.canDrawOverlays(NotePageLauncher.this))
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + NotePageLauncher.this.getPackageName())), 1);
        else {
            startFloatWindow();
        }
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
            intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_NOTE_PAGE);
        }else{
            intent.putExtra(FloatServer.INTENT,FloatServer.OPEN_NOTE_PAGE | FloatServer.OPEN_EXTRA_URL);
            intent.putExtra(FloatServer.EXYRA_URL,url);
        }

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
