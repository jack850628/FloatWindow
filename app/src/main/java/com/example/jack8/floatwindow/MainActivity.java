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
        Intent intent=new Intent(this, FloatServer.class);
        Intent extra_intent = getIntent();
        if(extra_intent.getStringExtra(Intent.EXTRA_TEXT) == null) {
            intent.putExtra("Layouts", new int[]{R.layout.webpage, R.layout.note_page, R.layout.window_context, R.layout.window_conetxt2});
            intent.putExtra("Titles", new String[]{"網頁瀏覽器", "便條紙", "溫度轉換", "BMI轉換"});
        }else{//帶有外部字串的啟動方法
            intent.putExtra("Layouts", new int[]{R.layout.webpage, R.layout.note_page});
            intent.putExtra("Titles", new String[]{"網頁瀏覽器", "便條紙"});
            intent.putExtra("extra_url",extra_intent.getStringExtra(Intent.EXTRA_TEXT));
        }

        startService(intent);
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