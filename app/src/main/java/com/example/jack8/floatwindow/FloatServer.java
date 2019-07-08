package com.example.jack8.floatwindow;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
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


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jack8.floatwindow.Window.WindowStruct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 浮動視窗服務
 */
public class FloatServer extends Service {
    public static final int OPEN_FLOAT_WINDOW = 0x01;
    public static final int OPEN_EXTRA_URL = 0x02;
    public static final int SHOW_WINDOW_MANAGER = 0x04;
    public static final int SHOW_FLOAT_WINDOW_MENU = 0x08;
    public static final int OPEN_WEB_BROWSER = 0x10;
    public static final int OPEN_NOTE_PAGE = 0x20;
    public static final int OPEN_CALCULATO = 0x40;
    public static final int OPEN_MAIN_MENU = 0x80;

    private static final String BCAST_CONFIGCHANGED ="android.intent.action.CONFIGURATION_CHANGED";

    static int wm_count=0;//計算FloatServer總共開了多少次

    WindowManager wm;
    Notification NF;
    final int NOTIFY_ID=851262;
    final String NOTIFY_CHANNEL_ID = "FloatWindow";
    HashMap<Integer, WindowStruct> windowList;
    WindowStruct windowManager = null;//視窗管理員
    WindowStruct menu;
    Handler handler = new Handler();
    WindowStruct.WindowAction windowAction = new WindowStruct.WindowAction() {
        @Override
        public void goHide(WindowStruct windowStruct) {

        }

        @Override
        public void goClose(WindowStruct windowStruct) {
            if (--wm_count == 0) {
                FloatServer.this.stopForeground(true);
                FloatServer.this.unregisterReceiver(ScreenChangeListener.getInstance());
                stopSelf();
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        Intent toSetup = new Intent(this, Setting.class);

        Intent showWindowManager = new Intent(this,FloatServer.class);
        showWindowManager.putExtra("intent",SHOW_WINDOW_MANAGER);

        Intent showFloatWindowMenu = new Intent(this,FloatServer.class);
        showFloatWindowMenu.putExtra("intent",SHOW_FLOAT_WINDOW_MENU);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder NFB = new NotificationCompat.Builder(this);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle(getString(R.string.app_name)).
                    addAction(new NotificationCompat.Action.Builder(R.drawable.settings, getString(R.string.setting), PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    addAction(new NotificationCompat.Action.Builder(R.drawable.menu, getString(R.string.windows_list), PendingIntent.getService(this, 1, showWindowManager, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    setContentText(getString(R.string.runing));
            NFB.setContentIntent(PendingIntent.getService(this, 0, showFloatWindowMenu, PendingIntent.FLAG_UPDATE_CURRENT));
            NF = NFB.build();
            startForeground(NOTIFY_ID, NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        }else{
            NotificationChannel NC = new NotificationChannel(NOTIFY_CHANNEL_ID,getString(R.string.app_name),NotificationManager.IMPORTANCE_LOW);
            NC.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(NC);

            Notification.Builder NFB = new Notification.Builder(this,NOTIFY_CHANNEL_ID);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle(getString(R.string.app_name)).
                    addAction(new Notification.Action.Builder(R.drawable.settings, getString(R.string.setting), PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    addAction(new Notification.Action.Builder(R.drawable.menu, getString(R.string.windows_list), PendingIntent.getService(this, 1, showWindowManager, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    setContentText(getString(R.string.runing));
            NFB.setContentIntent(PendingIntent.getService(this, 0, showFloatWindowMenu, PendingIntent.FLAG_UPDATE_CURRENT));
            NF = NFB.build();
            startForeground(NOTIFY_ID, NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        }
        Log.i("WMStrver","Create");

        //---------------註冊翻轉事件廣播接收---------------
        IntentFilter filter = new IntentFilter();
        filter.addAction(BCAST_CONFIGCHANGED);
        this.registerReceiver(ScreenChangeListener.getInstance(), filter);
        //-------------------------------------------------

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
        int initCode = intent.getIntExtra("intent",-1);
        if((initCode & OPEN_MAIN_MENU) == OPEN_MAIN_MENU) {
            wm_count++;
            new WindowStruct.Builder(this,wm)
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                    .windowPages(new int[]{R.layout.main_menu})
                    .windowPageTitles(new String[]{getResources().getString(R.string.app_name)})
                    .windowInitArgs(new Object[1][0])
                    .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                    .windowAction(windowAction)
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        AdView adView;
                        @Override
                        public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
                            View.OnClickListener onClickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Class clazz = null;
                                    switch (v.getId()){
                                        case R.id.web_browser:{
                                            clazz = WebBrowserLauncher.class;
                                            break;
                                        }
                                        case R.id.note:{
                                            clazz = NotePageLauncher.class;
                                            break;
                                        }
                                        case R.id.calculato:{
                                            clazz = CalculatoLauncher.class;
                                            break;
                                        }
                                    }
                                    Intent intent = new Intent(FloatServer.this, clazz);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    FloatServer.this.startActivity(intent);
                                    windowStruct.close();
                                }
                            };
                            pageView.findViewById(R.id.web_browser).setOnClickListener(onClickListener);
                            pageView.findViewById(R.id.note).setOnClickListener(onClickListener);
                            pageView.findViewById(R.id.calculato).setOnClickListener(onClickListener);
                            adView = pageView.findViewById(R.id.adView);
                            AdRequest adRequest = new AdRequest.Builder()
                                    .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                                    .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
                                    .addTestDevice("F4734F4691C588DB93799277888EA573")
                                    .build();
                            adView.loadAd(adRequest);
                        }

                        @Override
                        public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {
                            adView.destroy();
                        }

                        @Override
                        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }
                    })
                    .show();
        }else if((initCode & OPEN_WEB_BROWSER) == OPEN_WEB_BROWSER) {
            wm_count++;
            if((initCode & OPEN_EXTRA_URL) != OPEN_EXTRA_URL)
                new WindowStruct.Builder(this,wm)
                        .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.web_browser), getResources().getString(R.string.bookmarks), getResources().getString(R.string.history)})
                        .windowInitArgs(new Object[3][0])
                        .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new WebBrowser())
                        .show();
            else{
                String extra_url = intent.getStringExtra("extra_url");
                new WindowStruct.Builder(this,wm)
                        .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.web_browser), getResources().getString(R.string.bookmarks), getResources().getString(R.string.history)})
                        .windowInitArgs(new Object[][]{new Object[]{extra_url}})
                        .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new WebBrowser())
                        .show();
            }
        }else if((initCode & OPEN_NOTE_PAGE) == OPEN_NOTE_PAGE) {
            wm_count++;
            if((initCode & OPEN_EXTRA_URL) != OPEN_EXTRA_URL)
                new WindowStruct.Builder(this,wm)
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.HIDE_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                        .windowPages(new int[]{R.layout.note_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.note)})
                        .windowInitArgs(new Object[1][0])
                        .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new NotePage())
                        .show();
            else{
                String extra_url = intent.getStringExtra("extra_url");
                new WindowStruct.Builder(this,wm)
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.HIDE_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                        .windowPages(new int[]{R.layout.note_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.note)})
                        .windowInitArgs(new Object[][]{new Object[]{NotePage.ADD_NOTE,extra_url}})
                        .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new NotePage())
                        .show();
            }
        }else if((initCode & OPEN_CALCULATO) == OPEN_CALCULATO) {
            wm_count++;
            new WindowStruct.Builder(this,wm)
                    .windowPages(new int[]{R.layout.window_context, R.layout.window_conetxt2})
                    .windowPageTitles(new String[]{getResources().getString(R.string.temperature_conversion), getResources().getString(R.string.BMI_conversion)})
                    .windowInitArgs(new Object[2][0])
                    .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                    .windowAction(windowAction)
                    .constructionAndDeconstructionWindow(new Calculato())
                    .show();
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
            if((initCode & SHOW_FLOAT_WINDOW_MENU) == SHOW_FLOAT_WINDOW_MENU){
                ListView menuView = new ListView(this);
                menuView.setAdapter(new ArrayAdapter<String>(FloatServer.this,R.layout.hide_menu_item,R.id.item_text,new String[]{getString(R.string.setting),getString(R.string.windows_list)}));
                if(menu!=null)
                    menu.focusAndShowWindow();
                else {
                    wm_count++;
                    menu = new WindowStruct.Builder(this, wm)
                            .windowPages(new View[]{menuView})
                            .windowPageTitles(new String[]{getString(R.string.app_name)})
                            .top(60)
                            .left(60)
                            .height((int) (140 * this.getResources().getDisplayMetrics().density))
                            .width((int) (200 * this.getResources().getDisplayMetrics().density))
                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                            .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                            .windowAction(new WindowStruct.WindowAction() {
                                @Override
                                public void goHide(WindowStruct windowStruct) {

                                }

                                @Override
                                public void goClose(WindowStruct windowStruct) {
                                    menu = null;
                                    windowAction.goClose(windowStruct);
                                }
                            })
                            .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                @Override
                                public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {

                                }

                                @Override
                                public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }

                                @Override
                                public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }

                                @Override
                                public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }
                            }).show();
                    menuView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            menu.close();
                            switch (position) {
                                case 0:
                                    Intent intent = new Intent(FloatServer.this, Setting.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    showUnWindowMenu();
                            }
                        }
                    });
                }
            }else if((initCode & SHOW_WINDOW_MANAGER) == SHOW_WINDOW_MANAGER)
                showUnWindowMenu();
        }

        return START_REDELIVER_INTENT;
    }
    void showUnWindowMenu(){
        final ListView hideMenu=new ListView(this);
        final hideMenuAdapter hma = new hideMenuAdapter();
        hideMenu.setAdapter(hma);
        /*menu=new AlertDialog.Builder(this).setTitle("所有視窗清單").setView(hideMenu).create();
        menu.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        menu.show();
        hideMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //menu.dismiss();
                windowList.get(hma.key[position]).focusAndShowWindow();
            }
        });*/
        if(windowManager == null) {
            wm_count++;
            windowManager = new WindowStruct.Builder(this, wm)
                    .windowPages(new View[]{hideMenu})
                    .windowPageTitles(new String[]{getString(R.string.windows_list)})
                    .top(60)
                    .left(60)
                    .height((int) (200 * this.getResources().getDisplayMetrics().density))
                    .width((int) (195 * this.getResources().getDisplayMetrics().density))
                    .displayObject(WindowStruct.SIZE_BAR | WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                    .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                    .windowAction(new WindowStruct.WindowAction() {
                        @Override
                        public void goHide(WindowStruct windowStruct) {

                        }

                        @Override
                        public void goClose(WindowStruct windowStruct) {
                            windowManager = null;
                            windowAction.goClose(windowStruct);
                        }
                    })
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        @Override
                        public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {

                        }

                        @Override
                        public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }
                    }).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int windowListLength = 0;
                    while (windowManager != null) {
                        if (windowListLength != windowList.size())
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    hma.updateWindowList();
                                    hma.notifyDataSetChanged();
                                }
                            });
                        windowListLength = windowList.size();
                        try {
                            Thread.sleep(1l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }else
            windowManager.focusAndShowWindow();
    }

    class hideMenuAdapter extends BaseAdapter{
        private Integer[] key;

        public hideMenuAdapter(){
            updateWindowList();
        }

        public void updateWindowList(){
            key = windowList.keySet().toArray(new Integer[windowList.size()]);
        }

        @Override
        public int getCount() {
            return key.length;
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
            if (convertView == null)
                convertView = LayoutInflater.from(FloatServer.this).inflate(R.layout.window_manager, null);
            if(windowList.containsKey(key[position])) {
                final WindowStruct windowStruct = windowList.get(key[position]);
                ((TextView) convertView.findViewById(R.id.title)).setText(windowStruct.getWindowTitle());
                convertView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        windowStruct.close();
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        windowStruct.focusAndShowWindow();
                    }
                });
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