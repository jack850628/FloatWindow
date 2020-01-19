package com.example.jack8.floatwindow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JackLog {
    public interface JackLogCallBack{
        void callBack(String log);
    }
    private static boolean canWriteLog;
    public static void setWriteLogDrive(Context context, String... ids){
        String driveId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
        Log.i("裝置id", driveId);
        Log.i("JackLog", context.getFilesDir().getPath());
        for(String id : ids){
            if(driveId.equals(id)) {
                Log.i("JackLog", "On");
                canWriteLog = true;
            }
        }
    }

    public static boolean isCanWriteLog(){
        return canWriteLog;
    }

    public static void writeLog(Context context, String logString){
        writeLog(context, logString, Context.MODE_APPEND);
    }
    public static void writeLog(Context context, String logString, int mode){
        Log.i("JackLog", logString);
        if(canWriteLog) {
            try {
                FileOutputStream fileOutputStream = context.openFileOutput("logFile.txt", mode);
                fileOutputStream.write(logString.getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void readLog(final Context context, final JackLogCallBack callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuffer log = new StringBuffer();
                try {
                    FileInputStream fileInputStream = context.openFileInput("logFile.txt");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                    String str;
                    while ((str = bufferedReader.readLine()) != null)
                        log.append(str).append("\n");
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.callBack(log.toString());
                    }
                });
            }
        }).start();
    }
}
