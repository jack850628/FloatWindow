package com.example.jack8.floatwindow;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.RequiresApi;

import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.JsonUtils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jack8.floatwindow.Window.WindowFrom;
import com.jack8.floatwindow.Window.WindowStruct;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebBrowser implements WindowStruct.constructionAndDeconstructionWindow {
    public Handler handler = new Handler(Looper.getMainLooper());
    WebView web;

    private EditText path;
    private Button go;
    private Button goBack;
    private Button menu;
    private Button showControlsBar;
    private Button windowFormMenu;
    private ProgressBar PB;
    private BookmarkList bookmarkList;
    private HistoryList historyList;
    private DataBaseForBrowser dataBaseForBrowser = null;
    private boolean desktopMode = false;
    private int browserModeForOpen = -1;
    private String defaultUserAgentString;
    private String desktopModeUserAgentString;
    private boolean thisPageHaveIcon;
    private boolean hiddenControlsBar = false;

    private final FirebaseCrashlytics crashlytics;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private boolean canSendHistory = false;//因為onReceivedTitle會比doUpdateVisitedHistory慢調用，所以在doUpdateVisitedHistory送出紀錄的話標題會是上一個網頁的標題，但單純在onReceivedTitle送標題會導致只要網頁使用javascript改標題，就會送出一次歷史紀錄。

    public WebBrowser(){
        crashlytics = FirebaseCrashlytics.getInstance();
    }

    public void loadUrl(String url){
        PB.setVisibility(View.VISIBLE);
        PB.setProgress(0);
        web.loadUrl(url);
    }

    @Override
    public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
        switch (position) {
            case 0:
                JackLog.writeLog(context, String.format("WebBrowser ID: \"%d\" Window Open\n", windowStruct.getNumber()));
                crashlytics.log(String.format("WebBrowser ID: \"%d\" Window Open\n", windowStruct.getNumber()));
                windowStruct.getWindowFrom().setWindowKeyEvent(new WindowFrom.WindowKeyEvent() {
                    @Override
                    public boolean dispatchKeyEvent(KeyEvent event) {
                    if(
                        windowStruct.nowState != WindowStruct.State.CLOSE &&
                        windowStruct.getCurrentPagePosition() == 0 &&
                        event.getKeyCode() == KeyEvent.KEYCODE_BACK
                    ){
                        if(event.getAction() == KeyEvent.ACTION_UP) {
                            web.goBack();
                            return true;
                        }
                    }
                    return false;
                    }
                });
                page0(context, pageView, position, args, windowStruct);
                break;
            case 1:
                bookmarkList = new BookmarkList(context, pageView, this, dataBaseForBrowser.bookmarksDao(), windowStruct);
                break;
            case 2:
                historyList = new HistoryList(context, pageView, this, dataBaseForBrowser.historyDao(), windowStruct);
                break;
        }
    }

    private void page0(final Context context, final View pageView, final int position, final Object[] args, final WindowStruct windowStruct){
        path = (EditText)pageView.findViewById(R.id.webpath);
        go = (Button)pageView.findViewById(R.id.go);
        goBack = (Button)pageView.findViewById(R.id.goback);
        menu = (Button) pageView.findViewById(R.id.menu);
        web = (WebView)pageView.findViewById(R.id.web);
        PB = (ProgressBar) pageView.findViewById(R.id.progressBar);
        windowFormMenu = (Button)windowStruct.getWindowFrom().findViewById(R.id.menu);
        final ViewGroup controlsBar = (ViewGroup)pageView.findViewById(R.id.controls_bar);
        final Clipboard clipboard = new Clipboard(context);

        showControlsBar = new Button(context);
        showControlsBar.setLayoutParams(new ViewGroup.LayoutParams(windowStruct.getWindowButtonsWidth(),windowStruct.getWindowButtonsHeight()));
        showControlsBar.setPadding(0,0,0,0);
        showControlsBar.setBackground(context.getResources().getDrawable(R.drawable.out_to_full_screen));
        new Handler(Looper.getMainLooper()).post(new Runnable() {//在下一幀畫面時動作
            @Override
            public void run() {
                showControlsBar.setVisibility(View.GONE);
            }
        });
        showControlsBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenControlsBar = false;
                if (customView == null)
                    controlsBar.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
            }
        });
        ((ViewGroup)pageView.getRootView().findViewById(R.id.micro_max_button_background)).addView(showControlsBar, 0);

        dataBaseForBrowser = Room.databaseBuilder(context, DataBaseForBrowser.class, DataBaseForBrowser.DATABASE_NAME)
                .addMigrations(DataBaseForBrowser.MIGRATION_1_2)
                .addMigrations(DataBaseForBrowser.MIGRATION_2_3)
                .addMigrations(DataBaseForBrowser.MIGRATION_3_4)
                .build();

        if(args.length >= 2){
            browserModeForOpen = (int)args[1];
        }

        web.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){//當點擊WebView內的連結時處理，參考:https://dotblogs.com.tw/newmonkey48/2013/12/26/136486
                loadUrl(url);
                return true;
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){//當點擊WebView內的連結時處理
                loadUrl(request.getUrl().toString());
                return true;
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String url) {//廣告過濾
                Uri uri = Uri.parse(url);
                try {
                    if(WebBrowserSetting.getStatus() == WebBrowserSetting.WebBrowserStatus.CLOSE){
                        return new WebResourceResponse(null, null, null);
                    }
                    if(WebBrowserSetting.getInit().getSetting().adsBlock && WebBrowserSetting.getInit().adServerListStatus == WebBrowserSetting.AdServerListStatus.COMPLETE) {
                        for (DataBaseForBrowser.AdServerData adServerData : WebBrowserSetting.getInit().adServerDatas) {
                            if (uri.getHost().contains(adServerData.adServer))
                                return new WebResourceResponse(null, null, null);
                        }
                    }
                    return super.shouldInterceptRequest(webView, url);
                }catch (Exception e){
                    JackLog.writeLog(context, String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    crashlytics.log(String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    JackLog.writeLog(context, String.format("異常發生時WebBrowser請求網頁: %s\n",url));
                    crashlytics.log(String.format("異常發生時WebBrowser請求網頁: %s\n",url));
                    JackLog.writeLog(context, String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    crashlytics.log(String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    crashlytics.recordException(e);
                    return new WebResourceResponse(null, null, null);
                }
            }
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {//廣告過濾
                try {
                    if(WebBrowserSetting.getStatus() == WebBrowserSetting.WebBrowserStatus.CLOSE){
                        return new WebResourceResponse(null, null, null);
                    }
                    if (WebBrowserSetting.getInit().getSetting().adsBlock && WebBrowserSetting.getInit().adServerListStatus == WebBrowserSetting.AdServerListStatus.COMPLETE) {
                        for (DataBaseForBrowser.AdServerData adServerData : WebBrowserSetting.getInit().adServerDatas) {
                            if (request.getUrl().getHost().contains(adServerData.adServer))
                                return new WebResourceResponse(null, null, null);
                        }
                    }
                    return super.shouldInterceptRequest(webView, request);
                }catch (Exception e){
                    JackLog.writeLog(context, String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    crashlytics.log(String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    JackLog.writeLog(context, String.format("異常發生時WebBrowser請求網頁: %s\n",request.getUrl().toString()));
                    crashlytics.log(String.format("異常發生時WebBrowser請求網頁: %s\n",request.getUrl().toString()));
                    JackLog.writeLog(context, String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    crashlytics.log(String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    crashlytics.recordException(e);
                    return new WebResourceResponse(null, null, null);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                thisPageHaveIcon = false;
                windowFormMenu.setBackgroundResource(R.drawable.menu_icom);
//                windowFormMenu.setPadding(0, 0, 0, 0);
            }

//            @Override
//            public void onPageFinished(WebView webView, final String url) {
//
//            }

            @Override
            public void doUpdateVisitedHistory(WebView webView, String url, boolean isReload) {
                path.setText(url);
                canSendHistory = true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView webView, String url, final String message, final JsResult result) {
//                AlertDialog Alert=new AlertDialog.Builder(context).setTitle(context.getString(R.string.web_say)).setMessage(message).
//                        setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                result.confirm();
//                            }
//                        }).create();
//                Alert.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//                Alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        result.cancel();
//                    }
//                });
//                Alert.show();
                if(windowStruct.nowState == WindowStruct.State.CLOSE)
                    return false;
                View messageView = LayoutInflater.from(context).inflate(R.layout.alert, null);
                ((TextView)messageView.findViewById(R.id.message)).setText(message);
                messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .parentWindow(windowStruct)
                        .windowPageTitles(new String[]{context.getString(R.string.web_say)})
                        .windowPages(new View[]{messageView})
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                        .left(windowStruct.getRealWidth() / 2 + windowStruct.getRealPositionX() - messageView.getMeasuredWidth() / 2)
                        .top(windowStruct.getRealHeight() / 2 + windowStruct.getRealPositionY() - (messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))) / 2)
                        .width(messageView.getMeasuredWidth())
                        .height((messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))))
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                        .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                        .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                        .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                        .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                            @Override
                            public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct ws) {
                                pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
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
                        .windowAction(new WindowStruct.WindowAction() {
                            @Override
                            public void goHide(WindowStruct windowStruct) {

                            }
                            @Override
                            public void goClose(WindowStruct windowStruct) {
                                result.cancel();
                            }
                        })
                        .show();
                //return true後絕對不能少了result.confirm()或result.cancel()，不然網頁會卡住
                //Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                //return super.onJsAlert(view, url, message, result);
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView webView, String url, String message,final JsResult result) {
//                AlertDialog Confirm=new AlertDialog.Builder(context).setTitle(context.getString(R.string.web_say)).setMessage(message).
//                        setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                result.confirm();
//                            }
//                        }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        result.cancel();
//                    }
//                }).create();
//                Confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        result.cancel();
//                    }
//                });
//                Confirm.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//                Confirm.show();
                if(windowStruct.nowState == WindowStruct.State.CLOSE)
                    return false;
                View messageView = LayoutInflater.from(context).inflate(R.layout.alert, null);
                ((TextView)messageView.findViewById(R.id.message)).setText(message);
                messageView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
                messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                new WindowStruct.Builder(context,  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .parentWindow(windowStruct)
                        .windowPageTitles(new String[]{context.getString(R.string.web_say)})
                        .windowPages(new View[]{messageView})
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                        .left(windowStruct.getRealWidth() / 2 + windowStruct.getRealPositionX() - messageView.getMeasuredWidth() / 2)
                        .top(windowStruct.getRealHeight() / 2 + windowStruct.getRealPositionY() - (messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))) / 2)
                        .width(messageView.getMeasuredWidth())
                        .height((messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))))
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                        .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                        .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                        .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                        .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                            @Override
                            public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct ws) {
                                pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        result.confirm();
                                        ws.close();
                                    }
                                });
                                pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
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
                        .windowAction(new WindowStruct.WindowAction() {
                            @Override
                            public void goHide(WindowStruct windowStruct) {

                            }
                            @Override
                            public void goClose(WindowStruct windowStruct) {
                                result.cancel();
                            }
                        })
                        .show();
                //return super.onJsConfirm(view,url,message,result);
                return true;
            }
            @Override
            public boolean onJsPrompt(WebView webView, String url, String message,String defaultValue, final JsPromptResult result) {
//                final EditText editText=new EditText(context);
//                editText.setText(defaultValue);
//                AlertDialog Prompt=new AlertDialog.Builder(context).setTitle(message).setView(editText).
//                        setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                result.confirm(editText.getText().toString());
//                            }
//                        }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        result.cancel();
//                    }
//                }).create();
//                Prompt.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        result.cancel();
//                    }
//                });
//                Prompt.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//                Prompt.show();
                if(windowStruct.nowState == WindowStruct.State.CLOSE)
                    return false;
                View messageView = LayoutInflater.from(context).inflate(R.layout.alert, null);
                ((TextView)messageView.findViewById(R.id.message)).setText(message);
                messageView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
                messageView.findViewById(R.id.input_text).setVisibility(View.VISIBLE);
                messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                new WindowStruct.Builder(context,  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .parentWindow(windowStruct)
                        .windowPageTitles(new String[]{context.getString(R.string.web_say)})
                        .windowPages(new View[]{messageView})
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                        .left(windowStruct.getRealWidth() / 2 + windowStruct.getRealPositionX() - messageView.getMeasuredWidth() / 2)
                        .top(windowStruct.getRealHeight() / 2 + windowStruct.getRealPositionY() - (messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))) / 2)
                        .width(messageView.getMeasuredWidth())
                        .height((messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))))
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                        .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                        .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                        .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                        .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                            @Override
                            public void Construction(Context context, final View pageView, int position, Object[] args, final WindowStruct ws) {
                                pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        result.confirm(((EditText)pageView.findViewById(R.id.input_text)).getText().toString());
                                        ws.close();
                                    }
                                });
                                pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
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
                        .windowAction(new WindowStruct.WindowAction() {
                            @Override
                            public void goHide(WindowStruct windowStruct) {

                            }
                            @Override
                            public void goClose(WindowStruct windowStruct) {
                                result.cancel();
                            }
                        })
                        .show();
                //return super.onJsPrompt(view,url,message,defaultValue,result);
                return true;
            }
            @Override
            public void onProgressChanged(WebView webView, int newProgress){
                PB.setProgress(newProgress);
                if(newProgress==100)
                    PB.setVisibility(View.GONE);
                super.onProgressChanged(webView,newProgress);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                thisPageHaveIcon = true;
                windowFormMenu.setBackground(new BitmapDrawable(context.getResources(), icon));
//                windowFormMenu.setPadding(5, 5, 5, 5);
            }

            @Override
            public void onReceivedTitle(WebView webView, String title) {
                windowStruct.setWindowTitle(position, title);
                if(canSendHistory) {
                    canSendHistory = false;
                    final String url = webView.getUrl();
                    JTools.threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            dataBaseForBrowser.historyDao().addHistory(new DataBaseForBrowser.History(title, url, new Date()));
                        }
                    });
                }
            }

            /*------------全螢幕播放--------------
             * 參考:https://www.jianshu.com/p/8b4df0f902db
             */
            @Override
            public void onShowCustomView(View view/*全螢幕撥放器的view*/, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                customView = view;
                ((ViewGroup)pageView).addView(customView);
                customViewCallback = callback;
                web.setVisibility(View.GONE);
                controlsBar.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                web.setVisibility(View.VISIBLE);
                if(!hiddenControlsBar)
                    controlsBar.setVisibility(View.VISIBLE);
                if (customView == null)
                    return;
                customView.setVisibility(View.GONE);
                ((ViewGroup)pageView).removeView(customView);
                customViewCallback.onCustomViewHidden();
                customView = null;
                super.onHideCustomView();
            }
            //----------------------------------------
        });
        web.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                int resultType = result.getType();
                if (resultType == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                        resultType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE ||
                        resultType == WebView.HitTestResult.ANCHOR_TYPE) {
                    ListView listView = new ListView(context);
                    final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(listView).create();
                    alertDialog.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    alertDialog.show();
                    listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_selectable_list_item,new String[]{context.getString(R.string.open_link),context.getString(R.string.open_link_in_new_window),context.getString(R.string.copy_link)}));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            switch (position){
                                case 0:
                                    web.loadUrl(result.getExtra());
                                    break;
                                case 1:
                                    FloatServer.wm_count++;
                                    new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                            .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                                            .windowPageTitles(new String[]{context.getString(R.string.web_browser), context.getString(R.string.bookmarks), context.getString(R.string.history)})
                                            .windowInitArgs(new Object[][]{new String[]{result.getExtra()}})
                                            .windowAction(((FloatServer)context).windowAction)
                                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                            .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                                            .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                                            .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                                            .constructionAndDeconstructionWindow(new WebBrowser())
                                            .show();
                                    break;
                                case 2:
                                    clipboard.copyToClipboard(result.getExtra());
                                    Toast.makeText(context,context.getString(R.string.copyed),Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            alertDialog.dismiss();
                        }
                    });
                    return true;
                }else if(resultType == WebView.HitTestResult.EDIT_TEXT_TYPE){
                    if(windowStruct.nowState != WindowStruct.State.FULLSCREEN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                        ListView listView = new ListView(context);
                        final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(listView).create();
                        alertDialog.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        alertDialog.show();
                        listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_selectable_list_item,new String[]{context.getString(android.R.string.cut),context.getString(android.R.string.copy),context.getString(android.R.string.paste)}));
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                switch (position){
                                    case 0:
                                        web.evaluateJavascript("(()=>{let val = document.activeElement.value; document.activeElement.value = ''; return {value: val}})()", new ValueCallback<String>() {
                                            @Override
                                            public void onReceiveValue(String value) {
                                                try {
                                                    JSONObject jsonObject = new JSONObject(value);
                                                    clipboard.copyToClipboard(jsonObject.getString("value"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    crashlytics.log(String.format("文字剪下失敗 %s\n", e.getStackTrace()));
                                                }
                                            }
                                        });
                                        break;
                                    case 1:
                                        web.evaluateJavascript("(()=>({value: document.activeElement.value}))()", new ValueCallback<String>() {
                                            @Override
                                            public void onReceiveValue(String value) {
                                                try {
                                                    JSONObject jsonObject = new JSONObject(value);
                                                    clipboard.copyToClipboard(jsonObject.getString("value"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    crashlytics.log(String.format("文字複製失敗 %s\n", e.getStackTrace()));
                                                }
                                            }
                                        });
                                        break;
                                    case 2:
                                        web.evaluateJavascript("document.activeElement.value += \"" + JsonUtils.escapeString(clipboard.copyFromClipboard()) + "\"", null);
                                        break;
                                }
                                alertDialog.dismiss();
                            }
                        });
                        return true;
                    }
                }
                return false;
            }
        });
        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, final String mimetype, final long contentLength) {
                int h = (int)(context.getResources().getDisplayMetrics().density * 135) + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context));
                int w = (int)(context.getResources().getDisplayMetrics().density * 280);
                new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .parentWindow(windowStruct)
                        .windowPages(new int[]{R.layout.web_download_file})
                        .windowPageTitles(new String[]{context.getString(R.string.are_you_sure_download)})
                        .displayObject(WindowStruct.CLOSE_BUTTON | WindowStruct.TITLE_BAR_AND_BUTTONS)
                        .left(windowStruct.getRealWidth() / 2 + windowStruct.getRealPositionX() - w / 2)
                        .top(windowStruct.getRealHeight() / 2 + windowStruct.getRealPositionY() - h / 2)
                        .width(w)
                        .height(h)
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                        .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                        .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                        .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                        .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                            @Override
                            public void Construction(final Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
                                TextView tv = ((TextView)pageView.findViewById(R.id.file_type));
                                tv.setText(tv.getText() + ": " + mimetype);
                                tv = ((TextView)pageView.findViewById(R.id.file_size));
                                tv.setText(tv.getText() + ": " + (contentLength / 1024 / 1024) + "MB");
                                pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        windowStruct.close();
                                    }
                                });
                                pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent sendIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                                        Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.download_method));
                                        chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                            context.startActivity(chooser);
                                        windowStruct.close();
                                    }
                                });
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
                        })
                        .show();
            }
        });

        WebBrowserSetting.init(dataBaseForBrowser, windowStruct.getNumber(), new WebBrowserSetting.Operated() {
            @Override
            public void operated(WebBrowserSetting webBrowserSetting) {
                web.getSettings().setJavaScriptEnabled(webBrowserSetting.getSetting().javaScriptEnabled);
                web.getSettings().setSupportZoom(webBrowserSetting.getSetting().supportZoom);
                web.getSettings().setBuiltInZoomControls(true);
                web.getSettings().setDisplayZoomControls(webBrowserSetting.getSetting().displayZoomControls);
                web.getSettings().setUseWideViewPort(true);
                web.getSettings().setDomStorageEnabled(true);
                web.getSettings().setDatabaseEnabled(true);
                defaultUserAgentString = web.getSettings().getUserAgentString();
                desktopModeUserAgentString = Pattern.compile("^(.*?)Linux; Android (?:[0-9.]+);.+?( Build\\/.+?\\))(.+?)(?:Mobile )?(Safari.*)$").matcher(defaultUserAgentString).replaceAll("$1X11; U; Linux i686;$2$3$4");
                /*
                以U12+為例
                Mozilla/5.0 (Linux; Android 9; HTC 2Q55100 Build/PQ2A.190205.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/87.0.4280.86 Mobile Safari/537.36
                將會取代成
                Mozilla/5.0 (X11; U; Linux i686; Build/PQ2A.190205.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/87.0.4280.86 Safari/537.36
                 */
                if(
                        browserModeForOpen == WebBrowserSetting.BrowserMode.DESKTOP.getId()
                        ||
                        (browserModeForOpen == -1 && webBrowserSetting.getSetting().browserMode == WebBrowserSetting.BrowserMode.DESKTOP.getId())
                ){
                    desktopMode = true;
                    web.getSettings().setUserAgentString(desktopModeUserAgentString);
                }
                String url = webBrowserSetting.getSetting().homeLink;
                if(args != null && args.length != 0 && args[0] instanceof String) {
                    url = (String) args[0];
                    Matcher matcher = Pattern.compile("https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b(?:[-a-zA-Z0-9@:%_\\+.~#?&\\/=]*)").matcher(url);
                    if(matcher.find())
                        url = matcher.group();
                }
                path.setText(url);
                web.loadUrl(url);
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.loadUrl(path.getText().toString());
            }
        });
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebBackForwardList webBackForwardList = web.copyBackForwardList();
                WebHistoryItem webHistoryItem = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - 1);
                if(webHistoryItem != null){
                    path.setText(webHistoryItem.getUrl());//取得上一頁的網址連結
                    web.goBack();
                }
            }
        });
        path.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    web.loadUrl(path.getText().toString());
                    return true;
                }
                return false;
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView menu_list = new ListView(context);
                MenuAdapter.Item[] items = new MenuAdapter.Item[]{
                        new MenuAdapter.Item(context.getString(R.string.home_page)),
                        new MenuAdapter.Item(context.getString(R.string.add_to_bookmarks)),
                        new MenuAdapter.Item(context.getString(R.string.hidden_controls_bar)),
                        new MenuAdapter.Item(context.getString(R.string.share_the_website)),
                        new MenuAdapter.Item(context.getString(R.string.desktop_mode), desktopMode? 1 : 0),
                        new MenuAdapter.Item(context.getString(R.string.add_to_home_screen)),
                        new MenuAdapter.Item(context.getString(R.string.open_to_other_browser)),
                        new MenuAdapter.Item(context.getString(R.string.web_browser_setting)),
                };
                menu_list.setAdapter(new MenuAdapter(context, items));
                final PopupWindow popupWindow = new PopupWindow(context);
                popupWindow.setWidth(((View)v.getParent()).getWidth());//好像是因為menu_list內部item文字的關西，在這使用menu_list.measure取到寬度很窄
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(menu_list);
                popupWindow.setFocusable(true);
                popupWindow.showAsDropDown(v,-popupWindow.getWidth() + v.getWidth(),0);//需要-popupWindow.getWidth() + v.getWidth()是因為在Android 6上PopupWindow的anchor view下方以anchor view最左邊往右算的寬度如果不足popupWindow的寬度，popupWindow就會跑到anchor view的上方
                menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:
                                loadUrl(WebBrowserSetting.getInit().getSetting().homeLink);
                                break;
                            case 1:{
                                final String title = web.getTitle(), url = web.getUrl();
                                new AsyncTask<DataBaseForBrowser.Bookmark, Void, DataBaseForBrowser.Bookmark>(){

                                    @Override
                                    protected DataBaseForBrowser.Bookmark doInBackground(DataBaseForBrowser.Bookmark... bookmark) {
                                        List<DataBaseForBrowser.Bookmark> oldBookmark = dataBaseForBrowser.bookmarksDao().getBookmark(bookmark[0].url);
                                        if(oldBookmark.size() == 0){
                                            bookmark[0].id = dataBaseForBrowser.bookmarksDao().addBookmark(bookmark[0]);
                                            return bookmark[0];
                                        }else
                                            return oldBookmark.get(0);
                                    }

                                    @Override
                                    protected void onPostExecute(final DataBaseForBrowser.Bookmark result){
                                        new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                                .parentWindow(windowStruct)
                                                .windowPageTitles(new String[]{context.getString(R.string.bookmark_added)})
                                                .windowPages(new int[]{R.layout.add_to_bookmark})
                                                .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                                .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                                                .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                                                .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                                                .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                                                .left(windowStruct.getRealWidth() / 2 + windowStruct.getRealPositionX() - (int)(context.getResources().getDisplayMetrics().density*280) / 2)
                                                .top(windowStruct.getRealHeight() / 2 + windowStruct.getRealPositionY() - (int)(context.getResources().getDisplayMetrics().density*130 + WindowParameter.getWindowButtonsHeight(context)) / 2)
                                                .width((int)(context.getResources().getDisplayMetrics().density*280))
                                                .height((int)(context.getResources().getDisplayMetrics().density*(130 + WindowParameter.getWindowButtonsHeight(context))))
                                                .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                                    @Override
                                                    public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
                                                        final EditText title_box = pageView.findViewById(R.id.title);
                                                        final EditText url_box = pageView.findViewById(R.id.home_link);

                                                        title_box.setText(result.title);
                                                        url_box.setText(result.url);
                                                        pageView.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                JTools.threadPool.execute(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            dataBaseForBrowser.bookmarksDao().upDataBookmark(result.id, title_box.getText().toString(), url_box.getText().toString());
                                                                        }catch (SQLiteConstraintException e){
                                                                            dataBaseForBrowser.bookmarksDao().deleteBookmark(url_box.getText().toString());//因為url是唯一的，當upDataBookmark的url在資料庫已經存在時就會發生錯誤，因此將原有的url刪除
                                                                            dataBaseForBrowser.bookmarksDao().upDataBookmark(result.id, title_box.getText().toString(), url_box.getText().toString());
                                                                        }
                                                                    }
                                                                });
                                                                windowStruct.close();
                                                            }
                                                        });
                                                        pageView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                JTools.threadPool.execute(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        dataBaseForBrowser.bookmarksDao().deleteBookmark(result);
                                                                    }
                                                                });
                                                                windowStruct.close();
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
                                                .show();
                                    }
                                }.execute(new DataBaseForBrowser.Bookmark(title,url));
                                break;
                            }
                            case 2:{
                                hiddenControlsBar = true;
                                controlsBar.setVisibility(View.GONE);
                                showControlsBar.setVisibility(View.VISIBLE);
                                break;
                            }
                            case 3: {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, web.getUrl());
                                sendIntent.setType("text/plain");
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_APP));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                            case 4: {
                                desktopMode = !desktopMode;
                                web.getSettings().setUserAgentString(
                                        desktopMode ? desktopModeUserAgentString : defaultUserAgentString
                                );
                                web.reload();
                                break;
                            }
                            case 5: {
                                String title = web.getTitle();
                                if(desktopMode)
                                    title += String.format("(%s)", context.getString(R.string.desktop_mode));
                                View messageView = LayoutInflater.from(context).inflate(R.layout.alert, null);
                                ((TextView)messageView.findViewById(R.id.message)).setText(context.getString(R.string.shortcut_title));
                                ((TextView)messageView.findViewById(R.id.input_text)).setText(title);
                                messageView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
                                messageView.findViewById(R.id.input_text).setVisibility(View.VISIBLE);
                                messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                                new WindowStruct.Builder(context,  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                        .parentWindow(windowStruct)
                                        .windowPageTitles(new String[]{context.getString(R.string.add_to_home_screen)})
                                        .windowPages(new View[]{messageView})
                                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                                        .left(windowStruct.getRealWidth() / 2 + windowStruct.getRealPositionX() - messageView.getMeasuredWidth() / 2)
                                        .top(windowStruct.getRealHeight() / 2 + windowStruct.getRealPositionY() - (messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))) / 2)
                                        .width(messageView.getMeasuredWidth())
                                        .height((messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context))))
                                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                        .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                                        .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                                        .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                                        .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                            @Override
                                            public void Construction(final Context context, final View pageView, int position, Object[] args, final WindowStruct ws) {
                                                pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        String title = ((TextView)pageView.findViewById(R.id.input_text)).getText().toString();
                                                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                                            Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT"),
                                                                    launcher = new Intent(context , WebBrowserLauncher.class);
                                                            launcher.putExtra(Intent.EXTRA_TEXT, web.getUrl());
                                                            launcher.putExtra(FloatServer.BROWSER_MODE, desktopMode? WebBrowserSetting.BrowserMode.DESKTOP.getId(): WebBrowserSetting.BrowserMode.DEFAULT.getId());
                                                            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcher);
                                                            Parcelable icon = Intent.ShortcutIconResource.fromContext(context, R.drawable.browser_icon);
                                                            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                                                            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
                                                            shortcutIntent.putExtra("duplicate", true);//是否可以重複建立
                                                            context.sendBroadcast(shortcutIntent);
                                                        }else{
                                                            Intent shortcutIntent = new Intent(context, WebBrowserLauncher.class);
                                                            shortcutIntent.putExtra(Intent.EXTRA_TEXT, web.getUrl());
                                                            shortcutIntent.putExtra(FloatServer.BROWSER_MODE, desktopMode? WebBrowserSetting.BrowserMode.DESKTOP.getId(): WebBrowserSetting.BrowserMode.DEFAULT.getId());
                                                            shortcutIntent.setAction(Intent.ACTION_CREATE_SHORTCUT);
                                                            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
                                                            ShortcutInfo shortcut = new ShortcutInfo.Builder(context, UUID.randomUUID().toString())
                                                                    .setShortLabel(title)
                                                                    .setLongLabel(title)
                                                                    .setIcon(thisPageHaveIcon? Icon.createWithBitmap(web.getFavicon()): Icon.createWithResource(context, R.drawable.browser_icon))
                                                                    .setIntent(shortcutIntent)
                                                                    .build();
                                                            shortcutManager.requestPinShortcut(shortcut, null);
                                                        }
                                                        Toast.makeText(context, context.getString(R.string.added_to_the_home_screen),Toast.LENGTH_SHORT).show();
                                                        ws.close();
                                                    }
                                                });
                                                pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
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
                                        .windowAction(new WindowStruct.WindowAction() {
                                            @Override
                                            public void goHide(WindowStruct windowStruct) {

                                            }
                                            @Override
                                            public void goClose(WindowStruct windowStruct) {
                                            }
                                        })
                                        .show();
                                break;
                            }
                            case 6: {
                                Intent sendIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(web.getUrl()));
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_browser));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                            case 7:
                                WebBrowserSetting.getInit().showSettingWindow(context, null);
                                break;
                        }
                        popupWindow.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {
        if(position == 0) {
            JackLog.writeLog(context, String.format("WebBrowser ID: \"%d\" Window Close\n", windowStruct.getNumber()));
            crashlytics.log(String.format("WebBrowser ID: \"%d\" Window Close\n", windowStruct.getNumber()));
            web.loadUrl("about:blank");
            web.onPause();
            web.removeAllViews();
            WebBrowserSetting.getInit().closeWebWindow(windowStruct.getNumber());
            JackLog.writeLog(context, String.format("還有運行中的網頁? %b\n", WebBrowserSetting.haveRuningBrowser()));
            web.clearHistory();
//            if (WebBrowserSetting.haveRuningBrowser())
                web.clearCache(false);//清除RAM快取，傳遞true會加上清除磁碟快取，還有其他WWebViewc還有其他WebView運行中的話不建議用true
//            else {//這將會導致AdMob無法運作
//                web.clearCache(true);//清除RAM快取，傳遞true會加上清除磁碟快取，還有其他WWebViewc還有其他WebView運行中的話不建議用true
//                web.pauseTimers();//會導致其他的WebView的javascript停止執行
//            }
//            web.setWebViewClient(null);
            web.destroyDrawingCache();
            web.destroy();
            web = null;
        }else if (position == 1) {
            bookmarkList.Deconstruction();
            bookmarkList = null;
        }else if(position == 2){
            historyList.Deconstruction();
            historyList = null;
        }
    }

    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {
        switch (position){
            case 0:
                if(hiddenControlsBar)
                    showControlsBar.setVisibility(View.VISIBLE);
                break;
            case 1:
                bookmarkList.onResume();
                break;
            case 2:
                historyList.onResume();
                break;
        }
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
        switch (position){
            case 0:
                showControlsBar.setVisibility(View.GONE);
                break;
            case 1:
                bookmarkList.onPause();
                break;
            case 2:
                historyList.onPause();
                break;
        }
    }

    static class MenuAdapter extends BaseAdapter{
        public static class Item{
            public String text;
            public int checkState;

            public Item(String text, int checkState){
                this.text = text;
                this.checkState = checkState;
            }

            public Item(String text){
                this(text, -1);
            }
        }
        Item[] items;
        Context context;

        public MenuAdapter(Context context, Item[] items){
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return items[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(items[i].checkState == -1){
                view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            }else{
                view = LayoutInflater.from(context).inflate(R.layout.list_checkbox_item, null);
                ((CheckBox)view.findViewById(R.id.item_checkBox)).setChecked(items[i].checkState == 1);
            }
            ((TextView)view.findViewById(R.id.item_text)).setText(items[i].text);
            return view;
        }
    }
}
