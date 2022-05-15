package com.example.jack8.floatwindow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jack8.floatwindow.Window.WindowStruct;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class WebBrowserSetting {
    private static WebBrowserSetting webBrowserSetting = null;

    public enum WebBrowserStatus{
        INIT,
        COMPLETE,
        CLOSE
    }
    private static WebBrowserStatus webBrowserStatus = WebBrowserStatus.CLOSE;
    public static WebBrowserStatus getStatus(){
        return webBrowserStatus;
    }
    private static Stack<Operated> operatedStack = new Stack<>();

    public enum AdServerListStatus{
        INIT,
        UPDATE,
        COMPLETE,
        NOT_USE
    }
    public AdServerListStatus adServerListStatus = AdServerListStatus.NOT_USE;
    public ArrayList<DataBaseForBrowser.AdServerData> adServerDatas = new ArrayList<>();

    public enum BrowserMode{
        DEFAULT(0),
        DESKTOP(1);

        private int id;
        BrowserMode(int id){
            this.id = id;
        }

        public int getId(){
            return id;
        }
    }

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    private final LinkedHashSet<Integer> webBrowserWindowList = new LinkedHashSet<>();
    private DataBaseForBrowser.SettingDao settingDao;
    private DataBaseForBrowser.AdServerDataDao adServerDataDao;
    private DataBaseForBrowser.Setting setting;
    private WindowStruct settingPage = null;

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {//取得adsBlock所需要的廣告網站資料
            if(adServerListStatus == AdServerListStatus.INIT){
                int adServerDataVersion;
                try{
                    adServerDataVersion = dataSnapshot.getValue(Integer.class);
                }catch (NullPointerException e){
                    adServerDataVersion = -1;
                }
                Log.i("adsBlock version", String.valueOf(adServerDataVersion));
                //判斷資料庫中的資料版本與Firebase上的是否一致
                if(adServerDataVersion != setting.adServerDataVersion && adServerDataVersion != -1) {//不一致
                    Log.i("adsBlock", "有新資料");
                    setting.adServerDataVersion = adServerDataVersion;
                    adServerListStatus = AdServerListStatus.UPDATE;
                    databaseReference.removeEventListener(valueEventListener);
                    databaseReference.onDisconnect();
                    databaseReference = firebaseDatabase.getReference(DataBaseForBrowser.AdServerData.TABLE_NAME+"/list");
                    databaseReference.addValueEventListener(valueEventListener);
                    JTools.threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            settingDao.updateSetting(setting);
                        }
                    });
                }else{
                    Log.i("adsBlock", "沒新資料");
                    databaseReference.removeEventListener(valueEventListener);
                    databaseReference.onDisconnect();
                    firebaseDatabase.goOffline();
                    databaseReference = null;
                    JTools.threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(!adServerDatas.isEmpty())
                                adServerDatas.clear();
                            adServerDatas.addAll(adServerDataDao.getAdServerDataList());
                            for(DataBaseForBrowser.AdServerData adServerData : adServerDatas)
                                Log.i("adsBlock", adServerData.adServer);
                            adServerListStatus = AdServerListStatus.COMPLETE;
                        }
                    });
                }
            }else if(adServerListStatus == AdServerListStatus.UPDATE) {//更新資料
                Log.i("adsBlock", "更新資料中");
                JTools.threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(!adServerDatas.isEmpty())
                            adServerDatas.clear();
                        for (DataSnapshot childData : dataSnapshot.getChildren()) {
                            Log.i("adsBlock", childData.getValue().toString());
                            adServerDatas.add(new DataBaseForBrowser.AdServerData(childData.getValue().toString()));
                        }
                        adServerDataDao.deleteAll();
                        adServerDataDao.addAdServerDataList(adServerDatas);
                        databaseReference.removeEventListener(valueEventListener);
                        databaseReference.onDisconnect();
                        firebaseDatabase.goOffline();
                        databaseReference = null;
                        adServerListStatus = AdServerListStatus.COMPLETE;
                    }
                });
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    public interface Operated{
        void operated(WebBrowserSetting webBrowserSetting);
    }

     static WebBrowserSetting init(final Context context, final int windowId, final Operated operated){
         if(webBrowserStatus == WebBrowserStatus.CLOSE){
             synchronized (WebBrowserSetting.class){
                 if(webBrowserStatus == WebBrowserStatus.CLOSE){
                     webBrowserStatus = WebBrowserStatus.INIT;
                     operatedStack.push(operated);
                     webBrowserSetting = new WebBrowserSetting(context, operated);
                 }else if(webBrowserStatus == WebBrowserStatus.INIT)
                     operatedStack.push(operated);
                 else
                     operated.operated(webBrowserSetting);
             }
         }else if(webBrowserStatus == WebBrowserStatus.INIT)
             operatedStack.push(operated);
         else
             operated.operated(webBrowserSetting);
         webBrowserSetting.webBrowserWindowList.add(windowId);
         return webBrowserSetting;
    }

    static WebBrowserSetting getInit(){
         return webBrowserSetting;
    }

    private WebBrowserSetting(final Context context, final Operated operated){
        settingDao = DataBaseForBrowser.getInstance(context).settingDao();
        adServerDataDao = DataBaseForBrowser.getInstance(context).adServerDataDao();
        JTools.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                List<DataBaseForBrowser.Setting> list = settingDao.getSetting();
                if (list.size() == 0) {
                    setting = new DataBaseForBrowser.Setting("https://www.google.com", true, true, false, false, 0, BrowserMode.DEFAULT.getId());
                    setting.id = settingDao.setSetting(setting);
                } else
                    setting = settingDao.getSetting().get(0);
                setting.displayZoomControls = false;

                if(setting.adsBlock)
                    loadAdServerList();
                else
                    adServerListStatus = AdServerListStatus.NOT_USE;

                JTools.uiThread.post(new Runnable() {
                    @Override
                    public void run() {
                        while(!operatedStack.empty())
                            operatedStack.pop().operated(WebBrowserSetting.this);
                        webBrowserStatus = WebBrowserStatus.COMPLETE;
                    }
                });
            }
        });
    }

    private void loadAdServerList(){//啟動adsBlock
        Log.i("adsBlock", "初始化中");
        adServerListStatus = AdServerListStatus.INIT;
        if(firebaseDatabase == null)
            firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.goOnline();
        databaseReference = firebaseDatabase.getReference(DataBaseForBrowser.AdServerData.TABLE_NAME+"/version");
        databaseReference.addValueEventListener(valueEventListener);
    }

    void showSettingWindow(Context context, final Runnable operated){
        if(settingPage == null) {
            FloatServer.wm_count++;
            settingPage = new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                    .windowPages(new int[]{R.layout.web_browser_setting})
                    .windowPageTitles(new String[]{context.getString(R.string.web_browser_setting)})
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                    .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                    .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                    .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                    .windowButtonHeightForMiniState((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getButtonHeightForMiniState(context)))
                    .windowButtonWidthForMiniState((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getButtonWidthForMiniState(context)))
                    .windowAction(((FloatServer) context).windowAction)
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MINI_BUTTON | WindowStruct.MAX_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        private AdView mAdView;

                        @Override
                        public void Construction(final Context context, final View pageView, int position, Map<String, Object> args, final WindowStruct windowStruct) {
                            mAdView = pageView.findViewById(R.id.adView);
                            mAdView.loadAd(new AdRequest.Builder().build());
                            ((EditText)pageView.findViewById(R.id.home_link)).setText(setting.homeLink);
                            ((Switch)pageView.findViewById(R.id.enable_js)).setChecked(setting.javaScriptEnabled);
                            ((Switch)pageView.findViewById(R.id.enable_zoom)).setChecked(setting.supportZoom);
//                            ((Switch)pageView.findViewById(R.id.enable_ads_block)).setChecked(setting.adsBlock);
//                            ((Switch)pageView.findViewById(R.id.display_zoom_buttom)).setChecked(setting.displayZoomControls);//無法提供顯示縮放按鈕，因為切換視窗時WebView會出現嚴重錯誤signal 4 (SIGILL), code 2 (ILL_ILLOPC), fault addr 0xd878e0d0
                            final Spinner browserMode = (Spinner) pageView.findViewById(R.id.browser_mode);
                            browserMode.setAdapter(ArrayAdapter.createFromResource(context, R.array.browser_mode, android.R.layout.simple_list_item_1));
                            browserMode.setSelection(setting.browserMode);
                            pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    windowStruct.close();
                                }
                            });
                            pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    setting.homeLink = ((EditText)pageView.findViewById(R.id.home_link)).getText().toString();
                                    setting.javaScriptEnabled = ((Switch)pageView.findViewById(R.id.enable_js)).isChecked();
                                    setting.supportZoom = ((Switch)pageView.findViewById(R.id.enable_zoom)).isChecked();
//                                    setting.displayZoomControls = ((Switch)pageView.findViewById(R.id.display_zoom_buttom)).isChecked();//無法提供顯示縮放按鈕，因為切換視窗時WebView會出現嚴重錯誤signal 4 (SIGILL), code 2 (ILL_ILLOPC), fault addr 0xd878e0d0
//                                    setting.adsBlock = ((Switch)pageView.findViewById(R.id.enable_ads_block)).isChecked();
                                    setting.browserMode = browserMode.getSelectedItemPosition();
                                    saveSetting(operated);
                                    windowStruct.close();
                                }
                            });
                        }

                        @Override
                        public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {
                            mAdView.destroy();
                            settingPage = null;
                        }

                        @Override
                        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }
                    })
                    .show();
        }else
            settingPage.focusAndShowWindow();
    }

    DataBaseForBrowser.Setting getSetting(){
         return setting;
    }

    void saveSetting(final Runnable operated){
        if(setting.adsBlock)
            loadAdServerList();
        JTools.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                settingDao.updateSetting(setting);
                if(operated != null)
                    JTools.uiThread.post(operated);
            }
        });
        for(int id : webBrowserWindowList){
            WebSettings webSettings = ((WebBrowser) com.jack8.floatwindow.Window.WindowManager.getWindowStruct(id).getConstructionAndDeconstructionWindow()).web.getSettings();
            webSettings.setJavaScriptEnabled(setting.javaScriptEnabled);
            webSettings.setSupportZoom(setting.supportZoom);
//            webSettings.setDisplayZoomControls(setting.displayZoomControls);//無法提供顯示縮放按鈕，因為切換視窗時WebView會出現嚴重錯誤signal 4 (SIGILL), code 2 (ILL_ILLOPC), fault addr 0xd878e0d0
        }
    }

    private void onDestroy(){
        webBrowserSetting = null;
        adServerDatas.clear();
        DataBaseForBrowser.removeInstance();
        webBrowserStatus = WebBrowserStatus.CLOSE;
        Log.i("WebBrowserSetting", "onDestroy");
    }

    static boolean haveRuningBrowser(){
         return webBrowserSetting != null;
    }

    void closeWebWindow(int windowId){
        webBrowserWindowList.remove(windowId);
        if(webBrowserWindowList.isEmpty()) {
            FirebaseCrashlytics.getInstance().log(String.format("Close WebBrowser %d and WebBrowserSetting onDestroy", windowId));
            onDestroy();
        }
    }
}
