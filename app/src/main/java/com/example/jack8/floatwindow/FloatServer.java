package com.example.jack8.floatwindow;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jack8.floatwindow.Window.WindowStruct;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 浮動視窗服務
 */
public class FloatServer extends Service {
    public static final int OPEN_NONE = 0x0000;
    //public static final int OPEN_FLOAT_WINDOW = 0x0001;
    public static final int OPEN_EXTRA_URL = 0x0002;
    public static final int OPEN_WINDOW_MANAGER = 0x0004;
    //public static final int SHOW_FLOAT_WINDOW_MENU = 0x0008;
    public static final int OPEN_WEB_BROWSER = 0x0010;
    public static final int OPEN_NOTE_PAGE = 0x0020;
    public static final int OPEN_CALCULATOR = 0x0040;
    public static final int OPEN_MAIN_MENU = 0x0080;
    public static final int OPEN_SETTING = 0x0100;
    public static final int CLOSE_FLOAT_WINDOW = 0x0200;
    public static final int OPEN_WATCHED_AD = 0x0400;
    public static final String LAUNCHER = "launcher";
    public static final String INTENT = "intent";

    private static final String BCAST_CONFIGCHANGED ="android.intent.action.CONFIGURATION_CHANGED";
    private static final String WHAT_IS_NEW_VERSION_REGEX = "\\.\\d+$";

    private boolean workPhaseRestored = false;

    static int wm_count=0;//計算FloatServer總共開了多少次

    WindowManager wm;
    Notification NF;
    final int NOTIFY_ID=851262;
    final String NOTIFY_CHANNEL_ID = "FloatWindow";
    WindowStruct windowManager = null;//視窗管理員
    WindowStruct help = null;
    Handler handler = new Handler();
    boolean closeFloatWindow = false;
    WindowStruct.WindowAction windowAction = new WindowStruct.WindowAction() {
        @Override
        public void goHide(WindowStruct windowStruct) {

        }

        @Override
        public void goClose(WindowStruct windowStruct) {
            if (--wm_count == 0) {
                if(closeFloatWindow || !WindowParameter.isPermanent(FloatServer.this))
                    closeFloatWindow();
            }
        }
    };

    private FirebaseCrashlytics crashlytics;

    private void closeFloatWindow(){
        try {
            unregisterReceiver(ScreenChangeListener.getInstance(this));
        }catch (IllegalArgumentException e){
            crashlytics.recordException(e);
        }
        try {
            unregisterReceiver(HomeKeyListener.getInstance(this));
        }catch (IllegalArgumentException e){
            crashlytics.recordException(e);
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = PendingIntent.FLAG_IMMUTABLE;
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notify_view);
        remoteViews.setOnClickPendingIntent(R.id.web_browser,
                PendingIntent.getActivity(this,
                        0,
                        new Intent(this, WebBrowserLauncher.class),
                        flags
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.note,
                PendingIntent.getActivity(this,
                        1,
                        new Intent(this, NotePageLauncher.class),
                        flags
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.calculato,
                PendingIntent.getActivity(this,
                        2,
                        new Intent(this, CalculatorLauncher.class),
                        flags
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.setting,
                PendingIntent.getActivity(this,
                        3,
                        new Intent(this, Setting.class),
                        flags
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.window_list,
                PendingIntent.getService(this,
                        4,
                        new Intent(this,FloatServer.class).putExtra(INTENT,OPEN_WINDOW_MANAGER),
                        flags
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.close,
                PendingIntent.getService(this,
                        5,
                        new Intent(this,FloatServer.class).putExtra(INTENT,CLOSE_FLOAT_WINDOW),
                        flags
                )
        );
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder NFB = new NotificationCompat.Builder(this);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle(getString(R.string.app_name)).
                    setContent(remoteViews);
//                    addAction(new NotificationCompat.Action.Builder(R.drawable.settings, getString(R.string.setting), PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
//                    addAction(new NotificationCompat.Action.Builder(R.drawable.menu, getString(R.string.windows_list), PendingIntent.getService(this, 1, showWindowManager, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
//                    setContentText(getString(R.string.runing)).
//                    setContentIntent(PendingIntent.getService(this, 0, showFloatWindowMenu, PendingIntent.FLAG_UPDATE_CURRENT));
            NF = NFB.build();
        }else{
            NotificationChannel NC = new NotificationChannel(NOTIFY_CHANNEL_ID,getString(R.string.app_name),NotificationManager.IMPORTANCE_LOW);
            NC.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(NC);

            Notification.Builder NFB = new Notification.Builder(this,NOTIFY_CHANNEL_ID);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle(getString(R.string.app_name)).
                    setCustomContentView(remoteViews);
            //addAction(new Notification.Action.Builder(R.drawable.settings, getString(R.string.setting), PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
            //addAction(new Notification.Action.Builder(R.drawable.menu, getString(R.string.windows_list), PendingIntent.getService(this, 1, showWindowManager, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
            //setContentText(getString(R.string.runing))
            //setContentIntent(PendingIntent.getService(this, 0, showFloatWindowMenu, PendingIntent.FLAG_UPDATE_CURRENT));
            NF = NFB.build();
        }
        startForeground(NOTIFY_ID, NF);//將服務升級至前台等級，這樣就不會突然被系統回收

        crashlytics = FirebaseCrashlytics.getInstance();

        JackLog.setWriteLogDrive(this,
                "e77e084bb5155afb"
        );
        wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(
                Arrays.asList(
                        "6B58CCD0570D93BA1317A64BEB8BA677",
                        "1E461A352AC1E22612B2470A43ADADBA",
                        "F4734F4691C588DB93799277888EA573"
                )
        ).build());
        MobileAds.initialize(this);

        //---------------註冊翻轉事件廣播接收---------------
        this.registerReceiver(ScreenChangeListener.getInstance(this), new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
        //-------------------------------------------------
        //---------------註冊Home鍵廣播接收---------------
        this.registerReceiver(HomeKeyListener.getInstance(this), new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        //-------------------------------------------------

//        try {//用反射取得所有視窗清單
//            Field field = WindowStruct.class.getDeclaredField("windowList");
//            field.setAccessible(true);
//            windowList = (HashMap<Integer,WindowStruct>)field.get(WindowStruct.class);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }

    /*
    關於onStartCommand的說明
    http://www.cnblogs.com/not-code/archive/2011/05/21/2052713.html
     */
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        int initCode = (intent != null)
                ? intent.getIntExtra(INTENT,OPEN_NONE)
                : OPEN_NONE;
        if(!workPhaseRestored){
            workPhaseRestored = true;
            JTools.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    List<DataBase.WorkingWindow> workingWindows = DataBase.getInstance(FloatServer.this).workingWindowDao().getAllWorkingWindow();
                    DataBase.getInstance(FloatServer.this).workingWindowDao().deleteAllWorkingWindow();
                    final String[] uris = new String[workingWindows.size()];
                    for(int i = 0; i < uris.length; i++) {
                        uris[i] = workingWindows.get(i).uri;
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            JTools.workPhaseRecover(FloatServer.this, uris);
                        }
                    });
                }
            });
        }
        if((initCode & OPEN_MAIN_MENU) == OPEN_MAIN_MENU) {
            wm_count++;
            new WindowStruct.Builder(this,wm)
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                    .windowPages(new int[]{R.layout.main_menu})
                    .windowPageTitles(new String[]{getResources().getString(R.string.app_name)})
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .heightAndTopAutoCenter((int)(getResources().getDisplayMetrics().density * 320))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    .windowAction(windowAction)
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        AdView adView;
                        @Override
                        public void Construction(Context context, View pageView, int position, Map<String, Object> args, final WindowStruct windowStruct) {
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
                                            clazz = CalculatorLauncher.class;
                                            break;
                                        }
                                        case R.id.setting:{
                                            clazz = Setting.class;
                                            break;
                                        }
                                        case R.id.watch_ad:{
                                            clazz = HelpMeAd.class;
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
                            pageView.findViewById(R.id.setting).setOnClickListener(onClickListener);
                            pageView.findViewById(R.id.watch_ad).setOnClickListener(onClickListener);
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                                pageView.findViewById(R.id.tip).setVisibility(View.VISIBLE);
                                View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(final View v) {
                                        ListView menu_list = new ListView(FloatServer.this);
                                        menu_list.setAdapter(new ArrayAdapter<String>(FloatServer.this, R.layout.list_item, R.id.item_text, new String[]{getResources().getString(R.string.add_to_home_screen)}));
                                        menu_list.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                                        final PopupWindow popupWindow =new PopupWindow(FloatServer.this);
                                        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                                        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                                        popupWindow.setContentView(menu_list);
                                        popupWindow.setFocusable(true);
                                        int anchorLoc[] = new int[2];
                                        v.getLocationInWindow(anchorLoc);
                                        popupWindow.showAtLocation(v, Gravity.LEFT | Gravity.TOP,0, anchorLoc[1]);
                                        menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                int name = 0, R_icon = 0;
                                                Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT"),
                                                        launcher = new Intent(getApplicationContext() , MainActivity.class);
                                                switch (v.getId()){
                                                    case R.id.web_browser:{
                                                        name = R.string.web_browser;
                                                        R_icon = R.drawable.browser;
                                                        launcher.putExtra(LAUNCHER, OPEN_WEB_BROWSER);
                                                        break;
                                                    }
                                                    case R.id.note:{
                                                        name = R.string.note;
                                                        R_icon = R.drawable.note;
                                                        launcher.putExtra(LAUNCHER, OPEN_NOTE_PAGE);
                                                        break;
                                                    }
                                                    case R.id.calculato:{
                                                        name = R.string.calculator;
                                                        R_icon = R.drawable.calculator;
                                                        launcher.putExtra(LAUNCHER, OPEN_CALCULATOR);
                                                        break;
                                                    }
                                                    case R.id.setting:{
                                                        name = R.string.setting;
                                                        R_icon = R.drawable.setting_icon;
                                                        launcher.putExtra(LAUNCHER, OPEN_SETTING);
                                                        break;
                                                    }
                                                }
                                                //shortcutIntent.putExtra("duplicate", false);//是否可以重複建立
                                                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(name));
                                                Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R_icon);
                                                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                                                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcher);
                                                sendBroadcast(shortcutIntent);
                                                popupWindow.dismiss();
                                                Toast.makeText(FloatServer.this,getString(R.string.added_to_the_home_screen),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return true;
                                    }
                                };
                                pageView.findViewById(R.id.web_browser).setOnLongClickListener(onLongClickListener);
                                pageView.findViewById(R.id.note).setOnLongClickListener(onLongClickListener);
                                pageView.findViewById(R.id.calculato).setOnLongClickListener(onLongClickListener);
                                pageView.findViewById(R.id.setting).setOnLongClickListener(onLongClickListener);
                            }
                            adView = pageView.findViewById(R.id.adView);
                            adView.loadAd(new AdRequest.Builder().build());

                            Button helpButton = new Button(context);
                            helpButton.setLayoutParams(new ViewGroup.LayoutParams(windowStruct.getWindowButtonsWidth(), windowStruct.getWindowButtonsHeight()));
                            helpButton.setPadding(0,0,0,0);
                            helpButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.help));
                            helpButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openHelpWindow();
                                }
                            });
                            ViewGroup micro_max_button = pageView.getRootView().findViewById(R.id.micro_max_button_background);
                            micro_max_button.addView(helpButton,0);
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
            Map<String, Object> args = AutoRecordConstructionAndDeconstructionWindow.createArgs(intent);
            if(intent.hasExtra(Intent.EXTRA_TEXT)){
                args.put(WebBrowser.WEB_LINK, intent.getStringExtra(Intent.EXTRA_TEXT));
            }else if(intent.hasExtra(WebBrowser.WEB_LINK)){
                args.put(WebBrowser.WEB_LINK, intent.getStringExtra(WebBrowser.WEB_LINK));
            }
            if(intent.hasExtra(WebBrowser.BROWSER_MODE)){
                int browserMode = intent.getStringExtra(WebBrowser.BROWSER_MODE) == null
                        ?intent.getIntExtra(WebBrowser.BROWSER_MODE, WebBrowserSetting.BrowserMode.DEFAULT.getId())
                        :Integer.valueOf(intent.getStringExtra(WebBrowser.BROWSER_MODE));
                args.put(WebBrowser.BROWSER_MODE, browserMode);
            }
            if(intent.hasExtra(WebBrowser.HIDDEN_CONTROLS_BAR)){
                args.put(WebBrowser.HIDDEN_CONTROLS_BAR, Boolean.valueOf(intent.getStringExtra(WebBrowser.HIDDEN_CONTROLS_BAR)));
            }
            new JTools.WindowBuilderByIntent(intent).create(this, wm)
                    .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                    .windowPageTitles(new String[]{getResources().getString(R.string.web_browser), getResources().getString(R.string.bookmarks), getResources().getString(R.string.history)})
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    .windowAction(windowAction)
                    .constructionAndDeconstructionWindow(new WebBrowser())
                    .windowInitArgs(args)
                    .show();
        }else if((initCode & OPEN_NOTE_PAGE) == OPEN_NOTE_PAGE) {
            String path = JTools.popPathFirstDirectoryNameFromIntent(intent);
            if(path.equals(NotePage.NODE_LIST)){
                NotePage.openNodeList(this, intent);
            }else{
                wm_count++;
                Map<String, Object> args = AutoRecordConstructionAndDeconstructionWindow.createArgs(intent);
                if(intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
                    args.put(NotePage.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
                }else if(!path.equals("")){
                    args.put(NotePage.NOTE_ID, path);
                }
                if(intent.getStringExtra(NotePage.HIDE_FRAME) != null){
                    args.put(NotePage.HIDE_FRAME, Boolean.valueOf(intent.getStringExtra(NotePage.HIDE_FRAME)));
                }
                new JTools.WindowBuilderByIntent(intent).create(this, wm)
                        .windowPages(new int[]{R.layout.note_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.note)})
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                        .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                        .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                        .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new NotePage())
                        .windowInitArgs(args)
                        .show();
            }
        }else if((initCode & OPEN_CALCULATOR) == OPEN_CALCULATOR) {
            wm_count++;
            new JTools.WindowBuilderByIntent(intent)
                    .setHeight((int)(getResources().getDisplayMetrics().density * (269 + WindowParameter.getWindowButtonsHeight(this) + WindowParameter.getWindowSizeBarHeight(this))))
                    .create(this, wm)
                    .windowPages(new int[]{R.layout.calculator, R.layout.window_context, R.layout.window_conetxt2})
                    .windowPageTitles(new String[]{getResources().getString(R.string.calculator), getResources().getString(R.string.temperature_conversion), getResources().getString(R.string.BMI_conversion)})
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    //.width((int)(getResources().getDisplayMetrics().density * 200))
                    .windowAction(windowAction)
                    .constructionAndDeconstructionWindow(new Calculator())
                    .windowInitArgs(AutoRecordConstructionAndDeconstructionWindow.createArgs(intent))
                    .show();
        }else if((initCode & OPEN_WATCHED_AD) == OPEN_WATCHED_AD) {
            View messageView = LayoutInflater.from(this).inflate(R.layout.alert, null);
            ((TextView)messageView.findViewById(R.id.message)).setText(getString(R.string.watched_ad));
            messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            wm_count++;
            new WindowStruct.Builder(this, (WindowManager) this.getSystemService(Context.WINDOW_SERVICE))
                    .windowPageTitles(new String[]{getString(R.string.app_name)})
                    .windowPages(new View[]{messageView})
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                    .left((getResources().getDisplayMetrics().widthPixels / 2) - messageView.getMeasuredWidth() / 2)
                    .top((getResources().getDisplayMetrics().heightPixels / 2) - (messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))) / 2)
                    .width(messageView.getMeasuredWidth())
                    .height((messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))))
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        @Override
                        public void Construction(Context context, View pageView, int position, Map<String, Object> args, final WindowStruct ws) {
                            pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ws.close();
                                }
                            });
                        }
                    })
                    .windowAction(windowAction)
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
            if((initCode & OPEN_WINDOW_MANAGER) == OPEN_WINDOW_MANAGER) {
                shohWindowManager();
            }else if((initCode & CLOSE_FLOAT_WINDOW) == CLOSE_FLOAT_WINDOW) {
                if(com.jack8.floatwindow.Window.WindowManager.count() == 0)
                    closeFloatWindow();
                else{
                    View messageView = LayoutInflater.from(this).inflate(R.layout.alert, null);
                    ((TextView)messageView.findViewById(R.id.message)).setText(getString(R.string.close_notification_message));
                    messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    wm_count++;
                    new WindowStruct.Builder(this, (WindowManager) this.getSystemService(Context.WINDOW_SERVICE))
                            .windowPageTitles(new String[]{getString(R.string.close_notification)})
                            .windowPages(new View[]{messageView})
                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                            .left((getResources().getDisplayMetrics().widthPixels / 2) - messageView.getMeasuredWidth() / 2)
                            .top((getResources().getDisplayMetrics().heightPixels / 2) - (messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))) / 2)
                            .width(messageView.getMeasuredWidth())
                            .height((messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))))
                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                            .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                            .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                            .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                            .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                @Override
                                public void Construction(Context context, View pageView, int position, Map<String, Object> args, final WindowStruct ws) {
                                    Button closeAllWindow = pageView.findViewById(R.id.confirm);
                                    closeAllWindow.setText(getString(R.string.do_close));
                                    closeAllWindow.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ws.close();
                                            closeFloatWindow = true;
                                            FloatServer.this.stopForeground(true);
                                            for(int id: com.jack8.floatwindow.Window.WindowManager.getAllWindowNumber()){
                                                WindowStruct windowStruct = com.jack8.floatwindow.Window.WindowManager.getWindowStruct(id);
                                                if(windowStruct.getConstructionAndDeconstructionWindow() instanceof AutoRecordConstructionAndDeconstructionWindow){
                                                    ((AutoRecordConstructionAndDeconstructionWindow)windowStruct.getConstructionAndDeconstructionWindow()).doNotDeleteUri = true;
                                                }
                                                windowStruct.close();
                                            }
                                        }
                                    });
                                    Button checkAllWindow = pageView.findViewById(R.id.cancel);
                                    checkAllWindow.setVisibility(View.VISIBLE);
                                    checkAllWindow.setText(R.string.view_open_windows);
                                    checkAllWindow.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            shohWindowManager();
                                            ws.close();
                                        }
                                    });
                                }

                                @Override
                                public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct1) {

                                }

                                @Override
                                public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }

                                @Override
                                public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }
                            })
                            .windowAction(windowAction)
                            .show();
                }
            }
        }

        if(!WindowParameter.getWhatIsNewVersion(FloatServer.this).equals(BuildConfig.VERSION_NAME.replaceAll(WHAT_IS_NEW_VERSION_REGEX, ""))){
            openHelpWindow();
        }
        return START_STICKY;//START_REDELIVER_INTENT;
    }
    void openHelpWindow(){
        if(help == null) {
            wm_count++;
            help = new WindowStruct.Builder(FloatServer.this, wm)
                    .displayObject(WindowStruct.MENU_BUTTON | WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MINI_BUTTON | WindowStruct.MAX_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                    .windowPages(new int[]{R.layout.what_is_new, R.layout.help})
                    .windowPageTitles(new String[]{getResources().getString(R.string.new_functions), getResources().getString(R.string.help)})
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(FloatServer.this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(FloatServer.this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(FloatServer.this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(FloatServer.this)))
                    .windowAction(new WindowStruct.WindowAction() {
                        @Override
                        public void goHide(WindowStruct windowStruct) {

                        }

                        @Override
                        public void goClose(WindowStruct windowStruct) {
                            help = null;
                            WindowParameter.setWhatIsNewVersion(FloatServer.this, BuildConfig.VERSION_NAME.replaceAll(WHAT_IS_NEW_VERSION_REGEX, ""));
                            windowAction.goClose(windowStruct);
                        }
                    })
                    .constructionAndDeconstructionWindow(new Help())
                    .show();
        }else
            help.focusAndShowWindow();
    }
    void shohWindowManager(){
        final ListView hideMenu = new ListView(this);
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
                    .height((int) ((160 + WindowParameter.getWindowButtonsHeight(this) + WindowParameter.getWindowSizeBarHeight(this)) * this.getResources().getDisplayMetrics().density))
                    .width((int) (195 * this.getResources().getDisplayMetrics().density))
                    .displayObject(WindowStruct.SIZE_BAR | WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
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
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow())
                    .show();
            JTools.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    int windowListLength = 0;
                    while (windowManager != null) {
                        if (windowListLength != com.jack8.floatwindow.Window.WindowManager.count())
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    hma.updateWindowList();
                                    hma.notifyDataSetChanged();
                                }
                            });
                        windowListLength = com.jack8.floatwindow.Window.WindowManager.count();
                        try {
                            Thread.sleep(1l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }else
            windowManager.focusAndShowWindow();
    }

    class hideMenuAdapter extends BaseAdapter{
        private Integer[] windowNumbers;

        public hideMenuAdapter(){
            updateWindowList();
        }

        public void updateWindowList(){
            windowNumbers = com.jack8.floatwindow.Window.WindowManager.getAllWindowNumber();
        }

        @Override
        public int getCount() {
            return windowNumbers.length;
        }

        @Override
        public Object getItem(int position) {
            return com.jack8.floatwindow.Window.WindowManager.getWindowStruct(windowNumbers[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(FloatServer.this).inflate(R.layout.window_manager, null);
            if(com.jack8.floatwindow.Window.WindowManager.windowIn(windowNumbers[position])) {
                final WindowStruct windowStruct = com.jack8.floatwindow.Window.WindowManager.getWindowStruct(windowNumbers[position]);
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