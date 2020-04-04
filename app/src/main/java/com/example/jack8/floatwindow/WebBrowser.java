package com.example.jack8.floatwindow;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.jack8.floatwindow.Window.WindowFrom;
import com.jack8.floatwindow.Window.WindowStruct;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebBrowser implements WindowStruct.constructionAndDeconstructionWindow {
    public Handler handler = new Handler(Looper.getMainLooper());

    EditText path;
    Button go;
    Button goBack;
    Button menu;
    WebView web;
    ProgressBar PB;
    BookmarkList bookmarkList;
    HistoryList historyList;
    DataBaseForBrowser dataBaseForBrowser = null;

    public void loadUrl(String url){
        PB.setVisibility(View.VISIBLE);
        PB.setProgress(0);
        path.setText(url);
        web.loadUrl(url);
    }

    @Override
    public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
        switch (position) {
            case 0:
                JackLog.writeLog(context, String.format("WebBrowser ID: \"%d\" Window Open\n", windowStruct.getNumber()));
                Crashlytics.log(String.format("WebBrowser ID: \"%d\" Window Open\n", windowStruct.getNumber()));
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
        final ViewGroup controlsBar = (ViewGroup)pageView.findViewById(R.id.controls_bar);
        final Clipboard clipboard = new Clipboard(context);
        dataBaseForBrowser = Room.databaseBuilder(context, DataBaseForBrowser.class, DataBaseForBrowser.DATABASE_NAME)
                .addMigrations(DataBaseForBrowser.MIGRATION_1_2)
                .addMigrations(DataBaseForBrowser.MIGRATION_2_3)
                .build();
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
                    if(WebBrowserSetting.getInit().getSetting().adsBlock && WebBrowserSetting.getInit().adServerListStatus == WebBrowserSetting.AdServerListStatus.COMPLETE) {
                        for (DataBaseForBrowser.AdServerData adServerData : WebBrowserSetting.getInit().adServerDatas) {
                            if (uri.getHost().contains(adServerData.adServer))
                                return new WebResourceResponse(null, null, null);
                        }
                    }
                    return super.shouldInterceptRequest(webView, url);
                }catch (Exception e){
                    JackLog.writeLog(context, String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    Crashlytics.log(String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    JackLog.writeLog(context, String.format("異常發生時WebBrowser請求網頁: %s\n",url));
                    Crashlytics.log(String.format("異常發生時WebBrowser請求網頁: %s\n",url));
                    JackLog.writeLog(context, String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    Crashlytics.log(String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    Crashlytics.logException(e);
                    return new WebResourceResponse(null, null, null);
                }
            }
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {//廣告過濾
                try {
                    if (WebBrowserSetting.getInit().getSetting().adsBlock && WebBrowserSetting.getInit().adServerListStatus == WebBrowserSetting.AdServerListStatus.COMPLETE) {
                        for (DataBaseForBrowser.AdServerData adServerData : WebBrowserSetting.getInit().adServerDatas) {
                            if (request.getUrl().getHost().contains(adServerData.adServer))
                                return new WebResourceResponse(null, null, null);
                        }
                    }
                    return super.shouldInterceptRequest(webView, request);
                }catch (Exception e){
                    JackLog.writeLog(context, String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    Crashlytics.log(String.format("異常發生 WebBrowserID: %d\n", windowStruct.getNumber()));
                    JackLog.writeLog(context, String.format("異常發生時WebBrowser請求網頁: %s\n",request.getUrl().toString()));
                    Crashlytics.log(String.format("異常發生時WebBrowser請求網頁: %s\n",request.getUrl().toString()));
                    JackLog.writeLog(context, String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    Crashlytics.log(String.format("還有運行中的WebBrowser? %b\n",WebBrowserSetting.haveRuningBrowser()));
                    Crashlytics.logException(e);
                    return new WebResourceResponse(null, null, null);
                }
            }
            @Override
            public void onPageFinished(WebView webView, final String url) {
                final String title = webView.getTitle();
                //pageView.setTag(title);
                windowStruct.setWindowTitle(position, title);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dataBaseForBrowser.historyDao().addHistory(new DataBaseForBrowser.History(title, url, new Date()));
                    }
                }).start();
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

            /*------------全螢幕播放--------------
             * 參考:https://www.jianshu.com/p/8b4df0f902db
             */
            private View customView;
            private CustomViewCallback customViewCallback;
            @Override
            public void onShowCustomView(View view/*全螢幕撥放器的view*/, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                ((ViewGroup)pageView).addView(customView);
                customViewCallback = callback;
                web.setVisibility(View.GONE);
                controlsBar.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                web.setVisibility(View.VISIBLE);
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
                    ListView listView=new ListView(context);
                    final AlertDialog alertDialog=new AlertDialog.Builder(context).setView(listView).create();
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
                String url = webBrowserSetting.getSetting().homeLink;
                if(args != null && args.length != 0 && args[0] instanceof String) {
                    url = (String) args[0];
                    Pattern pattern = Pattern.compile("https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b(?:[-a-zA-Z0-9@:%_\\+.~#?&\\/=]*)");
                    Matcher matcher = pattern.matcher(url);
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
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView menu_list = new ListView(context);
                menu_list.setAdapter(new ArrayAdapter<String>(context, R.layout.list_item, R.id.item_text, new String[]{context.getString(R.string.home_page), context.getString(R.string.add_to_bookmarks), context.getString(R.string.share_the_website),context.getString(R.string.open_to_other_browser), context.getString(R.string.web_browser_setting)}));
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
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            dataBaseForBrowser.bookmarksDao().upDataBookmark(result.id, title_box.getText().toString(), url_box.getText().toString());
                                                                        }catch (SQLiteConstraintException e){
                                                                            dataBaseForBrowser.bookmarksDao().deleteBookmark(url_box.getText().toString());
                                                                            dataBaseForBrowser.bookmarksDao().upDataBookmark(result.id, title_box.getText().toString(), url_box.getText().toString());
                                                                        }
                                                                    }
                                                                }).start();
                                                                windowStruct.close();
                                                            }
                                                        });
                                                        pageView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        dataBaseForBrowser.bookmarksDao().deleteBookmark(result);
                                                                    }
                                                                }).start();
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
                            case 2: {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, web.getUrl());
                                sendIntent.setType("text/plain");
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_APP));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                            case 3: {
                                Intent sendIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(web.getUrl()));
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_browser));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                            case 4:
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
            Crashlytics.log(String.format("WebBrowser ID: \"%d\" Window Close\n", windowStruct.getNumber()));
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
        if(position == 1)
            bookmarkList.onResume();
        else if (position == 2)
            historyList.onResume();
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
        if(position == 1)
            bookmarkList.bookmarkList.clear();
        else if (position == 2)
            historyList.historyList.clear();
    }
}
