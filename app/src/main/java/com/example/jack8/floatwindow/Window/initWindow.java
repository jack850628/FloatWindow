package com.example.jack8.floatwindow.Window;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jack8.floatwindow.R;
import com.example.jack8.floatwindow.Window.WindowStruct;

/**
 * 初始化視窗內容
 */
public class initWindow {
    /**
     * 初始化視窗子頁面內容
     * @param context 視窗所在的Activity或Service的Context
     * @param pageView 子頁面的View
     * @param position 表示是第幾個子頁面
     * @param windowStruct  子頁面所在的視窗本體
     */
    public static void init(Context context, View pageView, int position, WindowStruct windowStruct){
        switch (position){
            case 0:
                initWindow1(context,pageView,position,windowStruct);
                break;
            case 1:
                initWindow2(context,pageView,windowStruct);
                break;
            case 2:
                initWindow3(context,pageView,windowStruct);
                break;
        }
    }
    public static void initWindow1(final Context context, final View pageView, final int position, final WindowStruct windowStruct){
        final EditText path=(EditText)pageView.findViewById(R.id.webpath);
        path.setText("https://www.google.com.tw/?gws_rd=ssl");
        Button go=(Button)pageView.findViewById(R.id.go);
        Button goBack=(Button)pageView.findViewById(R.id.goback);
        final WebView web=(WebView)pageView.findViewById(R.id.web);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){//當點擊WebView內的連結時處理，參考:https://dotblogs.com.tw/newmonkey48/2013/12/26/136486
                path.setText(url);
                web.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView wed, String url) {
                pageView.setTag(web.getTitle());
                windowStruct.setWindowTitle(position,web.getTitle());
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,final JsResult result) {
                AlertDialog Alert=new AlertDialog.Builder(context).setTitle("網頁訊息").setMessage(message).
                        setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        }).create();
                Alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                Alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });
                Alert.show();
                //return true後絕對不能少了result.confirm()或result.cancel()，不然網頁會卡住
                //Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                //return super.onJsAlert(view, url, message, result);
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,final JsResult result) {
                AlertDialog Confirm=new AlertDialog.Builder(context).setTitle("網頁訊息").setMessage(message).
                        setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                }).create();
                Confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });
                Confirm.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                Confirm.show();
                //return super.onJsConfirm(view,url,message,result);
                return true;
            }
            @Override
            public boolean onJsPrompt(WebView view, String url, String message,String defaultValue, final JsPromptResult result) {
                final EditText editText=new EditText(context);
                editText.setText(defaultValue);
                AlertDialog Prompt=new AlertDialog.Builder(context).setTitle(message).setView(editText).
                        setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm(editText.getText().toString());
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                }).create();
                Prompt.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });
                Prompt.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                Prompt.show();
                //return super.onJsPrompt(view,url,message,defaultValue,result);
                return true;
            }
        });
        web.loadUrl("https://www.google.com.tw/?gws_rd=ssl");
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
    }
    public static void initWindow2(Context context, View pageView, final WindowStruct windowStruct){
        final EditText et=(EditText)pageView.findViewById(R.id.Temperature);
        View.OnClickListener oc=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et.getText().toString().matches("| "))
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
    public static void initWindow3(Context context, View pageView, final WindowStruct windowStruct){
        final EditText H=(EditText)pageView.findViewById(R.id.H),W=(EditText)pageView.findViewById(R.id.W);
        final TextView BMI=(TextView)pageView.findViewById(R.id.BMI);
        ((Button)pageView.findViewById(R.id.CH)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(H.getText().toString().matches("| ")||W.getText().toString().matches("| "))
                    return;
                float h=Float.parseFloat(H.getText().toString())/100f;
                BMI.setText(String.valueOf(Float.parseFloat(W.getText().toString())/(h*h)));
            }
        });
    }
}
