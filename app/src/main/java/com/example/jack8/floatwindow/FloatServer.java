package com.example.jack8.floatwindow;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jack8.floatwindow.Window.WindowStruct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 浮動視窗服務
 */
public class FloatServer extends Service {
    WindowManager wm;
    Notification NF;
    final int NOTIFY_ID=851262;
    final String NOTIFY_CHANNEL_ID = "浮動視窗";
    int wm_count=0;//計算FloatServer總共開了多少次
    AlertDialog menu;
    HashMap<Integer,WindowStruct> windowList;
    @Override
    public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Intent toSetup=new Intent(this,Setup.class);
        Intent unHide=new Intent(this,FloatServer.class);
        unHide.putExtra("Layouts",new int[0]);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder NFB = new NotificationCompat.Builder(this);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle("浮動視窗").
                    addAction(new NotificationCompat.Action.Builder(R.drawable.settings, "設定", PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    addAction(new NotificationCompat.Action.Builder(R.drawable.menu, "所有視窗清單", PendingIntent.getService(this, 1, unHide, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    setContentText("浮動視窗已啟用");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                NFB.setContentIntent(PendingIntent.getService(this, 0, unHide, PendingIntent.FLAG_UPDATE_CURRENT));
            NF = NFB.build();
            startForeground(NOTIFY_ID, NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        }else{
            NotificationChannel NC = new NotificationChannel(NOTIFY_CHANNEL_ID,"浮動視窗",NotificationManager.IMPORTANCE_HIGH);
            NC.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(NC);

            Notification.Builder NFB = new Notification.Builder(this,NOTIFY_CHANNEL_ID);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle("浮動視窗").
                    addAction(new Notification.Action.Builder(R.drawable.settings, "設定", PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    addAction(new Notification.Action.Builder(R.drawable.menu, "所有視窗清單", PendingIntent.getService(this, 1, unHide, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    setContentText("浮動視窗已啟用");
            NF = NFB.build();
            startForeground(NOTIFY_ID, NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        }
        Log.i("WMStrver","Create");

        try {//用反射取得所有視窗清單
            Field field = WindowStruct.class.getDeclaredField("windowList");
            field.setAccessible(true);
            windowList = (HashMap<Integer,WindowStruct>)field.get(WindowStruct.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    /*
    關於onStartCommand的說明
    http://www.cnblogs.com/not-code/archive/2011/05/21/2052713.html
     */
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        int[] layouts = intent.getExtras().getIntArray("Layouts");
        WindowStruct.WindowAction windowAction = new WindowStruct.WindowAction() {
            @Override
            public void goHide(WindowStruct windowStruct) {

            }

            @Override
            public void goClose() {
                if (--wm_count == 0) {
                    FloatServer.this.stopForeground(true);
                    stopSelf();
                }
            }
        };
        if(layouts.length!=0) {
            wm_count++;
            String[] titles = intent.getExtras().getStringArray("Titles");
            String extra_url = intent.getStringExtra("extra_url");
            if(extra_url == null)
                new WindowStruct(this, wm, layouts, titles, new Object[layouts.length][0], windowAction,new initWindow());
            else{
                ListView menu_list = new ListView(this);
                menu_list.setId(0);
                menu_list.setAdapter(new ArrayAdapter<String>(this,R.layout.simple_selectable_list_item,titles));
                new WindowStruct(this, wm, new View[]{menu_list}, new String[]{"要使用哪個頁面開啟?"}, new Object[][]{new Object[]{layouts,titles,extra_url}},60,60,(int)(getResources().getDisplayMetrics().density*80*layouts.length),(int)(getResources().getDisplayMetrics().density*200),WindowStruct.ALL_NOT_DISPLAY, windowAction, new ProcessShare(wm,windowAction));
            }
        }else{
            //---------------------收起下拉選單-----------------------------
            try {
                Object statusBarManager = getSystemService("statusbar");
                Method collapse;

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    collapse = statusBarManager.getClass().getMethod("collapse");
                } else {
                    collapse = statusBarManager.getClass().getMethod("collapsePanels");
                }
                collapse.invoke(statusBarManager);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
            //-----------------------------------------------------------------------
            if(menu!=null)
                menu.dismiss();
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN){
                ListView Menu=new ListView(this);
                Menu.setAdapter(new ArrayAdapter<String>(FloatServer.this,android.R.layout.simple_selectable_list_item,new String[]{"設定","所有視窗清單"}));
                menu=new AlertDialog.Builder(this).setView(Menu).create();
                menu.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                menu.show();
                Menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        menu.dismiss();
                        switch (position){
                            case 0:
                                Intent intent=new Intent(FloatServer.this,Setup.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                            case 1:
                                showUnWindowMenu();
                        }
                    }
                });
            }else
                showUnWindowMenu();
        }

        return START_REDELIVER_INTENT;
    }
    void showUnWindowMenu(){
        final ListView hideMenu=new ListView(this);
        final hideMenuAdapter hma = new hideMenuAdapter();
        hideMenu.setAdapter(hma);
        menu=new AlertDialog.Builder(this).setTitle("所有視窗清單").setView(hideMenu).create();
        menu.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        menu.show();
        hideMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                menu.dismiss();
                windowList.get(hma.key[position]).focusAndShowWindow();
            }
        });
    }

    class hideMenuAdapter extends BaseAdapter{
        Integer[] key;

        public hideMenuAdapter(){
            key = windowList.keySet().toArray(new Integer[windowList.size()]);
        }

        @Override
        public int getCount() {
            return windowList.size();
        }

        @Override
        public Object getItem(int position) {
            return windowList.get(key[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=LayoutInflater.from(FloatServer.this).inflate(R.layout.hide_menu_item,parent,false);

                ((TextView)convertView.findViewById(R.id.item_text)).setText(windowList.get(key[position]).getWindowTitle());
            }
            return convertView;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

