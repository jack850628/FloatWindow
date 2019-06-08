package com.example.jack8.floatwindow;

import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jack8.floatwindow.Window.HistoryList;
import com.example.jack8.floatwindow.Window.WindowStruct;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 初始化視窗內容
 */
public class initWindow implements WindowStruct.constructionAndDeconstructionWindow {
    /**
     * 初始化視窗子頁面內容
     * @param context 視窗所在的Activity或Service的Context
     * @param pageView 子頁面的View
     * @param position 表示是第幾個子頁面
     * @param  args 初始化視窗用的參數
     * @param windowStruct  子頁面所在的視窗本體
     */
    public void Construction(Context context, View pageView, int position,Object[] args, WindowStruct windowStruct){
        switch (position){
            case 0:
                initWindow1(context,pageView,position,args,windowStruct);
                break;
            case 1:
                initWindow_Note_Page(context,pageView,position,args,windowStruct);
                break;
            case 2:
                initWindow2(context,pageView,windowStruct);
                break;
            case 3:
                initWindow3(context,pageView,windowStruct);
                break;
        }
    }

    EditText path;
    Button go;
    Button goBack;
    Button menu;
    WebView web;
    ProgressBar PB;

    public void loadUrl(String url){
        PB.setVisibility(View.VISIBLE);
        PB.setProgress(0);
        path.setText(url);
        web.loadUrl(url);
    }

    public void initWindow1(final Context context, final View pageView, final int position,final Object[] args, final WindowStruct windowStruct){
        path = (EditText)pageView.findViewById(R.id.webpath);
        go = (Button)pageView.findViewById(R.id.go);
        goBack = (Button)pageView.findViewById(R.id.goback);
        menu = (Button) pageView.findViewById(R.id.menu);
        web = (WebView)pageView.findViewById(R.id.web);
        PB = (ProgressBar) pageView.findViewById(R.id.progressBar);
        final ViewGroup controlsBar = (ViewGroup)pageView.findViewById(R.id.controls_bar);
        final Clipboard clipboard = new Clipboard(context);
        final DataBaseForBrowser dataBaseForBrowser = Room.databaseBuilder(context, DataBaseForBrowser.class, DataBaseForBrowser.DATABASE_NAME).build();

        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);

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
            public void onPageFinished(WebView wed, final String url) {
                final String title = web.getTitle();
                pageView.setTag(title);
                windowStruct.setWindowTitle(position,web.getTitle());
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
            public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
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
               new WindowStruct.Builder(context,  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                       .parentWindowNumber(windowStruct)
                       .windowPageTitles(new String[]{context.getString(R.string.web_say)})
                       .windowPages(new View[]{messageView})
                       .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS)
                       .left(windowStruct.getWidth() / 2 + windowStruct.getPositionX() - messageView.getMeasuredWidth() / 2)
                       .top(windowStruct.getHeight() / 2 + windowStruct.getPositionY() - (messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density*30)) / 2)
                       .width(messageView.getMeasuredWidth())
                       .height((messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density*30)))
                       .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
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
                           public void Deconstruction(Context context, View pageView, int position) {

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
            public boolean onJsConfirm(WebView view, String url, String message,final JsResult result) {
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
                        .parentWindowNumber(windowStruct)
                        .windowPageTitles(new String[]{context.getString(R.string.web_say)})
                        .windowPages(new View[]{messageView})
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS)
                        .left(windowStruct.getWidth() / 2 + windowStruct.getPositionX() - messageView.getMeasuredWidth() / 2)
                        .top(windowStruct.getHeight() / 2 + windowStruct.getPositionY() - (messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density*30)) / 2)
                        .width(messageView.getMeasuredWidth())
                        .height((messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density*30)))
                        .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
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
                            public void Deconstruction(Context context, View pageView, int position) {

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
            public boolean onJsPrompt(WebView view, String url, String message,String defaultValue, final JsPromptResult result) {
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
                        .parentWindowNumber(windowStruct)
                        .windowPageTitles(new String[]{context.getString(R.string.web_say)})
                        .windowPages(new View[]{messageView})
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS)
                        .left(windowStruct.getWidth() / 2 + windowStruct.getPositionX() - messageView.getMeasuredWidth() / 2)
                        .top(windowStruct.getHeight() / 2 + windowStruct.getPositionY() - (messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density*30)) / 2)
                        .width(messageView.getMeasuredWidth())
                        .height((messageView.getMeasuredHeight() + (int)(context.getResources().getDisplayMetrics().density*30)))
                        .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
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
                            public void Deconstruction(Context context, View pageView, int position) {

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
            public void onProgressChanged(WebView view, int newProgress){
                PB.setProgress(newProgress);
                if(newProgress==100)
                    PB.setVisibility(View.GONE);
                super.onProgressChanged(view,newProgress);
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
                                            .windowPages(new int[]{R.layout.webpage})
                                            .windowPageTitles(new String[]{context.getString(R.string.web_browser)})
                                            .windowInitArgs(new Object[][]{new String[]{result.getExtra()}})
                                            .windowAction(((FloatServer)context).windowAction)
                                            .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
                                            .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                                @Override
                                                public void Construction(Context context, View pageView, int position,Object[] args , WindowStruct windowStruct) {
                                                    initWindow.this.initWindow1(context,pageView,position,args,windowStruct);
                                                }

                                                @Override
                                                public void Deconstruction(Context context, View pageView, int position) {
                                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                                        ((WebView)pageView.findViewById(R.id.web)).onPause();
                                                }

                                                @Override
                                                public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                                                }

                                                @Override
                                                public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                                                }
                                            }).show();
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
        String url;
        if(args != null && args.length != 0 && args[0] instanceof String) {
            url = (String) args[0];
            Pattern pattern = Pattern.compile("https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b(?:[-a-zA-Z0-9@:%_\\+.~#?&\\/=]*)");
            Matcher matcher = pattern.matcher(url);
            if(matcher.find())
                url = matcher.group();
        }else
            url = "https://www.google.com.tw/?gws_rd=ssl";
        path.setText(url);
        web.loadUrl(url);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.loadUrl(path.getText().toString());
            }
        });
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebBackForwardList WBFL = web.copyBackForwardList();
                if(WBFL.getCurrentIndex()==0)//當目前顯示的是WebView第一個顯示的網址
                    return;
                path.setText(WBFL.getItemAtIndex(WBFL.getCurrentIndex()-1).getUrl());//取得上一頁的網址連結
                web.goBack();

            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView menu_list = new ListView(context);
                menu_list.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_selectable_list_item,new String[]{context.getString(R.string.history), context.getString(R.string.share_the_website),context.getString(R.string.open_to_other_browser)}));
                final PopupWindow popupWindow =new PopupWindow(context);
                popupWindow.setWidth(((View)v.getParent()).getWidth());//好像是因為menu_list內部item文字的關西，在這使用menu_list.measure取到寬度很窄
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(menu_list);
                popupWindow.setFocusable(true);
                popupWindow.showAsDropDown(v,-popupWindow.getWidth() + v.getWidth(),0);//需要-popupWindow.getWidth() + v.getWidth()是因為在Android 6上PopupWindow的anchor view下方以anchor view最左邊往右算的寬度如果不足popupWindow的寬度，popupWindow就會跑到anchor view的上方
                menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:{
                                HistoryList.show(context, initWindow.this, windowStruct, dataBaseForBrowser.historyDao());
                                break;
                            }
                            case 1: {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, web.getUrl());
                                sendIntent.setType("text/plain");
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_APP));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                            case 2: {
                                Intent sendIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(web.getUrl()));
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_browser));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                        }
                        popupWindow.dismiss();
                    }
                });
            }
        });
    }


    final static int ADD_NOTE = 0,OPEN_NOTE = 1;
    final static String NOTE = "Note",NOTES = "Notes";
    static LinkedList<String> showingNoteIdList = new LinkedList<>();
    static WindowStruct otherNoteList = null;//其他便條紙清單視窗
    static class OtherNodeListAdapter extends BaseAdapter{//其他便條紙清單所使用的Adapter

        public ArrayList<String[]> noteList = null;

        Context context;

        public OtherNodeListAdapter(Context context,LinkedList<String> showingNoteIdList){
            this.context = context;
            updateNodeList(showingNoteIdList);
        }

        public void update(LinkedList<String> showingNoteIdList){
            updateNodeList(showingNoteIdList);
            this.notifyDataSetChanged();
        }

        public void updateNodeList(LinkedList<String> showingNoteIdList){
            if(noteList == null)
                noteList = new ArrayList();
            else
                noteList.clear();
            noteList.add(new String[]{"ADD_NEW",context.getString(R.string.create_new_note)});
            try {
                JSONObject notes = new JSONObject(context.getSharedPreferences(NOTE,0).getString(NOTES, "{}"));
                Iterator<String> keys = notes.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    if (!showingNoteIdList.contains(key)) {
                        noteList.add(new String[]{key,notes.getString(key)});
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getCount() {
            return noteList.size();
        }

        @Override
        public Object getItem(int position) {
            return noteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.note_list_item, parent, false);
            TextView item_text = convertView.findViewById(R.id.item_text);
            Button removeNode = convertView.findViewById(R.id.remove_node);
            item_text.setText(noteList.get(position)[1]);
            if(position != 0) {
                item_text.setGravity(Gravity.NO_GRAVITY);
                item_text.setPadding(0,0,0,(int)(15*context.getResources().getDisplayMetrics().density));
                removeNode.setVisibility(View.VISIBLE);
                removeNode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences noteSpf = context.getSharedPreferences(initWindow.NOTE,0);
                        try {
                            JSONObject notes = new JSONObject(noteSpf.getString(NOTES,"{}"));
                            notes.remove(noteList.get(position)[0]);
                            SharedPreferences.Editor spfe=noteSpf.edit();
                            spfe.putString(NOTES,notes.toString());
                            spfe.apply();
                            noteList.remove(position);
                            OtherNodeListAdapter.this.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else {
                item_text.setPadding(0,(int)(15*context.getResources().getDisplayMetrics().density),0,(int)(15*context.getResources().getDisplayMetrics().density));
                item_text.setGravity(Gravity.CENTER_HORIZONTAL);
                removeNode.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
    static OtherNodeListAdapter otherNodeListAdapter = null;
    String noteId=null;
    Date dNow = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
    Button nodePageMenuButton;
    MoveWindow moveWindow;
    int nodePageDisplayObj;
    public void initWindow_Note_Page(final Context context, final View pageView, final int position,final Object[] args, final WindowStruct windowStruct){
        final EditText note=(EditText) pageView.findViewById(R.id.note);
        final Clipboard clipboard=new Clipboard(context);
        final View toolsBar = pageView.findViewById(R.id.tools_bar);
        final ImageView copy = toolsBar.findViewById(R.id.copy);
        final ImageView paste = toolsBar.findViewById(R.id.paste);
        final ImageView showFrame = toolsBar.findViewById(R.id.show_frame);
        final ImageView close = toolsBar.findViewById(R.id.close);
        final SharedPreferences noteSpf = context.getSharedPreferences(NOTE,0);

        note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    JSONObject notes = new JSONObject(noteSpf.getString(NOTES,"{}"));
                    if(!s.toString().matches("^\\s*$"))
                        notes.put(noteId, s);
                    else
                        notes.remove(noteId);
                    SharedPreferences.Editor spfe=noteSpf.edit();
                    spfe.putString(NOTES,notes.toString());
                    spfe.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        moveWindow = new MoveWindow(context,windowStruct);
        nodePageMenuButton = new Button(context);
        nodePageMenuButton.setLayoutParams(new ViewGroup.LayoutParams((int)(30*context.getResources().getDisplayMetrics().density),(int)(30*context.getResources().getDisplayMetrics().density)));
        nodePageMenuButton.setPadding(0,0,0,0);
        nodePageMenuButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.menu));
        nodePageMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupWindow popupWindow =new PopupWindow(context);
                ListView listView = new ListView(context);
                listView.setAdapter(new ArrayAdapter(context,android.R.layout.simple_list_item_1,new String[]{context.getString(R.string.other_notes),context.getString(R.string.share),context.getString(R.string.hide_frame)}));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:{
                                if(otherNoteList == null) {
                                    ListView nodeList = new ListView(context);
                                    nodeList.setAdapter(otherNodeListAdapter);
                                    nodeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            FloatServer.wm_count++;
                                            new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                                    .windowPageTitles(new String[]{context.getString(R.string.note)})
                                                    .windowPages(new int[]{R.layout.note_page})
                                                    .windowInitArgs(
                                                            position == 0
                                                            ?new Object[][]{{initWindow.ADD_NOTE,""}}
                                                            :new Object[][]{{initWindow.OPEN_NOTE,otherNodeListAdapter.noteList.get(position)[0]}}
                                                    )
                                                    .windowAction(((FloatServer)context).windowAction)
                                                    .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
                                                    .constructionAndDeconstructionWindow(new initWindow(){
                                                        @Override
                                                        public void Construction(Context context, View pageView, int position,Object[] args , WindowStruct windowStruct) {
                                                            super.Construction(context,pageView,1,args,windowStruct);
                                                        }

                                                        @Override
                                                        public void Deconstruction(Context context, View pageView, int position) {
                                                            super.Deconstruction(context, pageView, 1);
                                                        }

                                                        @Override
                                                        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {
                                                            super.onResume(context, pageView, 1, windowStruct);
                                                        }

                                                        @Override
                                                        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
                                                            super.onPause(context, pageView, 1, windowStruct);
                                                        }
                                                    })
                                                    .show();
                                        }
                                    });
                                    FloatServer.wm_count++;
                                    otherNoteList = new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                            .windowPages(new View[]{nodeList})
                                            .windowPageTitles(new String[]{context.getString(R.string.other_notes)})
                                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.SIZE_BAR)
                                            .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
                                            .windowAction(new WindowStruct.WindowAction() {
                                                @Override
                                                public void goHide(WindowStruct windowStruct) {

                                                }

                                                @Override
                                                public void goClose(WindowStruct windowStruct) {
                                                    otherNoteList = null;
                                                    ((FloatServer)context).windowAction.goClose(windowStruct);
                                                }
                                            })
                                            .show();
                                }else
                                    otherNoteList.focusAndShowWindow();
                                break;
                            }
                            case 1:{
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, note.getText().toString());
                                sendIntent.setType("text/plain");
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_APP));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                            case 2:
                                nodePageDisplayObj = windowStruct.getDisplayObject();
                                windowStruct.setDisplayObject(WindowStruct.ALL_NOT_DISPLAY);
                                showFrame.setVisibility(View.VISIBLE);
                                note.setOnTouchListener(moveWindow);
                                break;
                        }
                        popupWindow.dismiss();
                    }
                });
                listView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);//先量測
                popupWindow.setWidth(listView.getMeasuredWidth());//再取寬度
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(listView);
                popupWindow.setFocusable(true);
                popupWindow.showAsDropDown(v);
            }
        });

        note.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toolsBar.setVisibility(View.VISIBLE);
                return true;
            }
        });
        View.OnClickListener copy_paste = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.copy:
                        clipboard.copyToClipboard(note.getText().toString());
                        Toast.makeText(context,context.getString(R.string.copyed),Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.paste:
                        note.setText(note.getText()+clipboard.copyFromClipboard());
                        break;
                    case R.id.show_frame:
                        windowStruct.setDisplayObject(nodePageDisplayObj);
                        showFrame.setVisibility(View.GONE);
                        note.setOnTouchListener(null);
                }
                toolsBar.setVisibility(View.GONE);
            }
        };
        copy.setOnClickListener(copy_paste);
        paste.setOnClickListener(copy_paste);
        showFrame.setOnClickListener(copy_paste);
        close.setOnClickListener(copy_paste);

        if(otherNodeListAdapter == null)
            otherNodeListAdapter = new OtherNodeListAdapter(context,showingNoteIdList);
        if(args == null || args.length == 0) {
            try {
                JSONObject notes = new JSONObject(noteSpf.getString(NOTES, "{}"));
                Iterator<String> keys = notes.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    if (!showingNoteIdList.contains(key)) {
                        noteId = key;
                        showingNoteIdList.add(noteId);
                        break;
                    }
                }
                if (noteId != null) {
                    note.setText(notes.getString(noteId));
                    otherNodeListAdapter.update(showingNoteIdList);
                }else{
                    noteId = formatter.format(dNow);
                    showingNoteIdList.add(noteId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            int flag = (int)args[0];
            switch (flag){
                case ADD_NOTE: {
                    noteId = formatter.format(dNow);
                    showingNoteIdList.add(noteId);
                    note.setText((String) args[1]);
                    break;
                }
                case OPEN_NOTE:{
                    noteId = (String)args[1];
                    showingNoteIdList.add(noteId);
                    try {
                        JSONObject notes = new JSONObject(noteSpf.getString(NOTES, "{}"));
                        note.setText(notes.getString(noteId));
                        otherNodeListAdapter.update(showingNoteIdList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void initWindow2(Context context, View pageView, final WindowStruct windowStruct){
        final EditText et=(EditText)pageView.findViewById(R.id.Temperature);
        View.OnClickListener oc=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et.getText().toString().matches("^\\s*$"))
                    return;
                switch (v.getId()) {
                    case R.id.toC:
                        et.setText(String.valueOf((Float.parseFloat(et.getText().toString()) - 32) * 5f / 9f));
                        break;
                    case R.id.toF:
                        et.setText(String.valueOf(Float.parseFloat(et.getText().toString())*(9f/5f)+32));
                        break;
                }
            }
        };
        ((Button)pageView.findViewById(R.id.toC)).setOnClickListener(oc);
        ((Button)pageView.findViewById(R.id.toF)).setOnClickListener(oc);
    }
    public void initWindow3(Context context, View pageView, final WindowStruct windowStruct){
        final EditText H=(EditText)pageView.findViewById(R.id.H),W=(EditText)pageView.findViewById(R.id.W);
        final TextView BMI=(TextView)pageView.findViewById(R.id.BMI);
        ((Button)pageView.findViewById(R.id.CH)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(H.getText().toString().matches("^\\s*$")||W.getText().toString().matches("^\\s*$"))
                    return;
                float h=Float.parseFloat(H.getText().toString())/100f;
                BMI.setText(String.valueOf(Float.parseFloat(W.getText().toString())/(h*h)));
            }
        });
    }

    public void Deconstruction(Context context, View pageView, int position){
        if(position==0){
            ((WebView)pageView.findViewById(R.id.web)).getSettings().setJavaScriptEnabled(false);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                ((WebView)pageView.findViewById(R.id.web)).onPause();
        }else if(position==1){
            showingNoteIdList.remove(noteId);
            otherNodeListAdapter.update(showingNoteIdList);
        }
    }

    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {
        if(position == 1) {
            ViewGroup micro_max_button = pageView.getRootView().findViewById(R.id.micro_max_button_background);
            micro_max_button.addView(nodePageMenuButton,0);
        }
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
        if(position == 1){
            ViewGroup micro_max_button = pageView.getRootView().findViewById(R.id.micro_max_button_background);
            micro_max_button.removeView(nodePageMenuButton);
        }
    }
}
