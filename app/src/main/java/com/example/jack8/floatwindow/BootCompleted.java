package com.example.jack8.floatwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootCompleted extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(WindowParameter.isAutoRun(context)){
            Intent i = new Intent(context, FloatServer.class);
            i.putExtra(FloatServer.INTENT,FloatServer.OPEN_NONE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                context.startService(i);
            else
                context.startForegroundService(i);
        }
    }
}
