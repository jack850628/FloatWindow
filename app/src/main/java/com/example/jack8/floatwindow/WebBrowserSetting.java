package com.example.jack8.floatwindow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.Switch;

import com.jack8.floatwindow.Window.WindowStruct;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.LinkedHashSet;
import java.util.List;

public class WebBrowserSetting {
    private static WebBrowserSetting webBrowserSetting = null;

    private final LinkedHashSet<Integer> webBrowserWindowList = new LinkedHashSet<>();
    private DataBaseForBrowser.Setting setting;
    private WindowStruct settingPage = null;

    public interface Operated{
        void operated(WebBrowserSetting webBrowserSetting);
    }

     static WebBrowserSetting init(DataBaseForBrowser dataBaseForBrowser, int windoiwId, Operated operated){
         if(webBrowserSetting == null){
             synchronized (WebBrowserSetting.class){
                 if(webBrowserSetting == null){
                     webBrowserSetting = new WebBrowserSetting(dataBaseForBrowser, operated);
                 }
                 else
                     operated.operated(webBrowserSetting);
             }
         }else
             operated.operated(webBrowserSetting);
         webBrowserSetting.webBrowserWindowList.add(windoiwId);
         return webBrowserSetting;
    }

    static WebBrowserSetting getInit(){
         return webBrowserSetting;
    }

    private WebBrowserSetting(final DataBaseForBrowser dataBaseForBrowser, final Operated operated){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DataBaseForBrowser.Setting> list = dataBaseForBrowser.settingDao().getSetting();
                if (list.size() == 0) {
                    setting = new DataBaseForBrowser.Setting("https://www.google.com", true, true, false);
                    setting.id = dataBaseForBrowser.settingDao().setSetting(setting);
                } else
                    setting = dataBaseForBrowser.settingDao().getSetting().get(0);
                setting.displayZoomControls = false;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        operated.operated(WebBrowserSetting.this);
                    }
                });
            }
        }).start();
    }

    void showSettingWindow(Context context, final DataBaseForBrowser dataBaseForBrowser, final Runnable operated){
        if(settingPage == null) {
            FloatServer.wm_count++;
            settingPage = new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                    .windowPages(new int[]{R.layout.web_browser_setting})
                    .windowPageTitles(new String[]{context.getString(R.string.web_browser_setting)})
                    .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
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
                                    .build();
                            mAdView.loadAd(adRequest);
                            ((EditText)pageView.findViewById(R.id.home_link)).setText(setting.homeLink);
                            ((Switch)pageView.findViewById(R.id.enable_js)).setChecked(setting.javaScriptEnabled);
                            ((Switch)pageView.findViewById(R.id.enable_zoom)).setChecked(setting.supportZoom);
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
                                    saveSetting(dataBaseForBrowser, operated);
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
            settingPage.focusWindow();
    }

    DataBaseForBrowser.Setting getSetting(){
         return setting;
    }

    void saveSetting(final DataBaseForBrowser dataBaseForBrowser, final Runnable operated){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dataBaseForBrowser.settingDao().updateSetting(setting);
                if(operated != null)
                    new Handler(Looper.getMainLooper()).post(operated);
            }
        }).start();
        for(int id : webBrowserWindowList){
            WebSettings webSettings = ((initWindow)WindowStruct.getWindowStruct(id).getConstructionAndDeconstructionWindow()).web.getSettings();
            webSettings.setJavaScriptEnabled(setting.javaScriptEnabled);
            webSettings.setSupportZoom(setting.supportZoom);
//            webSettings.setDisplayZoomControls(setting.displayZoomControls);//無法提供顯示縮放按鈕，因為切換視窗時WebView會出現嚴重錯誤signal 4 (SIGILL), code 2 (ILL_ILLOPC), fault addr 0xd878e0d0
        }
    }

    boolean haveRuningBrowser(){
         return webBrowserWindowList.size() != 0;
    }

    void closeWebWindow(int windowId){
        webBrowserWindowList.remove(windowId);
    }
}
