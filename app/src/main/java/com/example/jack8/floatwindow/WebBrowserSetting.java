package com.example.jack8.floatwindow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.Switch;

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
import java.util.Stack;

public class WebBrowserSetting {
    private static WebBrowserSetting webBrowserSetting = null;

    public enum WebBrowserStatus{
        INIT,
        COMPLETE,
        CLOSE
    }
    public static WebBrowserStatus webBrowserStatus = WebBrowserStatus.CLOSE;
    private static Stack<Operated> operatedStack = new Stack<>();

    public enum AdServerListStatus{
        INIT,
        UPDATE,
        COMPLETE,
        NOT_USE
    }
    public AdServerListStatus adServerListStatus = AdServerListStatus.NOT_USE;
    public ArrayList<DataBaseForBrowser.AdServerData> adServerDatas = new ArrayList<>();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    private final LinkedHashSet<Integer> webBrowserWindowList = new LinkedHashSet<>();
    private DataBaseForBrowser dataBaseForBrowser;
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dataBaseForBrowser.settingDao().updateSetting(setting);
                        }
                    }).start();
                }else{
                    Log.i("adsBlock", "沒新資料");
                    databaseReference.removeEventListener(valueEventListener);
                    databaseReference.onDisconnect();
                    firebaseDatabase.goOffline();
                    databaseReference = null;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(!adServerDatas.isEmpty())
                                adServerDatas.clear();
                            adServerDatas.addAll(dataBaseForBrowser.adServerDataDao().getAdServerDataList());
                            for(DataBaseForBrowser.AdServerData adServerData : adServerDatas)
                                Log.i("adsBlock", adServerData.adServer);
                            adServerListStatus = AdServerListStatus.COMPLETE;
                        }
                    }).start();
                }
            }else if(adServerListStatus == AdServerListStatus.UPDATE) {//更新資料
                Log.i("adsBlock", "更新資料中");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!adServerDatas.isEmpty())
                            adServerDatas.clear();
                        for (DataSnapshot childData : dataSnapshot.getChildren()) {
                            Log.i("adsBlock", childData.getValue().toString());
                            adServerDatas.add(new DataBaseForBrowser.AdServerData(childData.getValue().toString()));
                        }
                        dataBaseForBrowser.adServerDataDao().deleteAll();
                        dataBaseForBrowser.adServerDataDao().addAdServerDataList(adServerDatas);
                        databaseReference.removeEventListener(valueEventListener);
                        databaseReference.onDisconnect();
                        firebaseDatabase.goOffline();
                        databaseReference = null;
                        adServerListStatus = AdServerListStatus.COMPLETE;
                    }
                }).start();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    public interface Operated{
        void operated(WebBrowserSetting webBrowserSetting);
    }

     static WebBrowserSetting init(DataBaseForBrowser dataBaseForBrowser, int windoiwId, Operated operated){
         if(webBrowserStatus == WebBrowserStatus.CLOSE){
             synchronized (WebBrowserSetting.class){
                 if(webBrowserStatus == WebBrowserStatus.CLOSE){
                     webBrowserStatus = WebBrowserStatus.INIT;
                     operatedStack.push(operated);
                     webBrowserSetting = new WebBrowserSetting(dataBaseForBrowser, operated);
                 }else if(webBrowserStatus == WebBrowserStatus.INIT)
                     operatedStack.push(operated);
                 else
                     operated.operated(webBrowserSetting);
             }
         }else if(webBrowserStatus == WebBrowserStatus.INIT)
             operatedStack.push(operated);
         else
             operated.operated(webBrowserSetting);
         webBrowserSetting.webBrowserWindowList.add(windoiwId);
         return webBrowserSetting;
    }

    static WebBrowserSetting getInit(){
         return webBrowserSetting;
    }

    private WebBrowserSetting(final DataBaseForBrowser dataBaseForBrowser, final Operated operated){
        this.dataBaseForBrowser = dataBaseForBrowser;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DataBaseForBrowser.Setting> list = dataBaseForBrowser.settingDao().getSetting();
                if (list.size() == 0) {
                    setting = new DataBaseForBrowser.Setting("https://www.google.com", true, true, false, false, 0);
                    setting.id = dataBaseForBrowser.settingDao().setSetting(setting);
                } else
                    setting = dataBaseForBrowser.settingDao().getSetting().get(0);
                setting.displayZoomControls = false;

                if(setting.adsBlock)
                    loadAdServerList();
                else
                    adServerListStatus = AdServerListStatus.NOT_USE;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        while(!operatedStack.empty())
                            operatedStack.pop().operated(WebBrowserSetting.this);
                        webBrowserStatus = WebBrowserStatus.COMPLETE;
                    }
                });
            }
        }).start();
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
                    .windowAction(((FloatServer) context).windowAction)
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MINI_BUTTON | WindowStruct.MAX_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        private AdView mAdView;

                        @Override
                        public void Construction(final Context context, final View pageView, int position, Object[] args, final WindowStruct windowStruct) {
                            MobileAds.initialize(context, context.getString(R.string.AD_ID));
                            mAdView = pageView.findViewById(R.id.adView);
                            AdRequest adRequest = new AdRequest.Builder()
                                    .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                                    .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
                                    .addTestDevice("F4734F4691C588DB93799277888EA573")
                                    .build();
                            mAdView.loadAd(adRequest);
                            ((EditText)pageView.findViewById(R.id.home_link)).setText(setting.homeLink);
                            ((Switch)pageView.findViewById(R.id.enable_js)).setChecked(setting.javaScriptEnabled);
                            ((Switch)pageView.findViewById(R.id.enable_zoom)).setChecked(setting.supportZoom);
//                            ((Switch)pageView.findViewById(R.id.enable_ads_block)).setChecked(setting.adsBlock);
//                            ((Switch)pageView.findViewById(R.id.display_zoom_buttom)).setChecked(setting.displayZoomControls);//無法提供顯示縮放按鈕，因為切換視窗時WebView會出現嚴重錯誤signal 4 (SIGILL), code 2 (ILL_ILLOPC), fault addr 0xd878e0d0
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                dataBaseForBrowser.settingDao().updateSetting(setting);
                if(operated != null)
                    new Handler(Looper.getMainLooper()).post(operated);
            }
        }).start();
        for(int id : webBrowserWindowList){
            WebSettings webSettings = ((WebBrowser)WindowStruct.getWindowStruct(id).getConstructionAndDeconstructionWindow()).web.getSettings();
            webSettings.setJavaScriptEnabled(setting.javaScriptEnabled);
            webSettings.setSupportZoom(setting.supportZoom);
//            webSettings.setDisplayZoomControls(setting.displayZoomControls);//無法提供顯示縮放按鈕，因為切換視窗時WebView會出現嚴重錯誤signal 4 (SIGILL), code 2 (ILL_ILLOPC), fault addr 0xd878e0d0
        }
    }

    private void onDestroy(){
        webBrowserSetting = null;
        adServerDatas.clear();
        webBrowserStatus = WebBrowserStatus.CLOSE;
        Log.i("WebBrowserSetting", "onDestroy");
    }

    static boolean haveRuningBrowser(){
         return webBrowserSetting != null;
    }

    void closeWebWindow(int windowId){
        webBrowserWindowList.remove(windowId);
        if(webBrowserWindowList.isEmpty())
            onDestroy();
    }
}
