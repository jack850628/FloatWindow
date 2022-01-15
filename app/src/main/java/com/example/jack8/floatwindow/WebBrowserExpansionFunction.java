package com.example.jack8.floatwindow;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.RequiresApi;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;

public class WebBrowserExpansionFunction {
    public static final String NAME = "FloatWindowExpansionFunction";

    private static final String OPTION_ITEM_STRING_NAME = "itemString";
    private static final String OPTION_ITEM_VALUE_NAME = "value";


    private final Handler runUi= new Handler(Looper.getMainLooper());

    private WebView webView;
    private Context context;

    private static String jsSelectOptionClicked(int index){
        return "window.FloatWindowClickingSelectElement.selectedIndex = "+ index +"; "+
                "window.FloatWindowClickingSelectElement.dispatchEvent(new Event('change', {bubbles: true})); "+
                "window.FloatWindowClickingSelectElement = null";
    }

    public WebBrowserExpansionFunction(Context context, WebView webView){
        this.context = context;
        this.webView = webView;
    }

    @JavascriptInterface
    public void selectClick(String optionsJSON){
        final ListView listView = new ListView(context);
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(listView).create();
        try {
            final JSONArray optionArray = new JSONArray(optionsJSON);
            final String[] options = new String[optionArray.length()];
            for(int i = 0; i < optionArray.length(); i++){
                options[i] = optionArray.getJSONObject(i).getString(OPTION_ITEM_STRING_NAME);
            }
            listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, options));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    runUi.post(new Runnable() {
                        @Override
                        public void run() {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                webView.evaluateJavascript(jsSelectOptionClicked(position), null);
                            }
                        }
                    });
                    alertDialog.dismiss();
                }
            });
            alertDialog.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            alertDialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().log(String.format("解析網頁下拉選單選項失敗 %s\n", e.getStackTrace()));
        }
    }

    public static final String JS_FIND_SELECT = "if(!window.FloatWindowFindSelect){\n" +
            "    window.FloatWindowFindSelect = new MutationObserver(([{target: doc}], observer) => {\n" +
            "        var selectElements = document.querySelectorAll('select');\n" +
            "        for(let i = 0; i < selectElements.length; i++){\n" +
            "            if(!selectElements[i].dataset['floatWindowSelectListened']){\n" +
            "                selectElements[i].dataset['floatWindowSelectListened'] = \"true\";\n" +
            "                selectElements[i].addEventListener('click', function({target: selectElement}){\n" +
            "                    window.FloatWindowClickingSelectElement = selectElement;\n" +
            "                    var optionElement = selectElement.options;\n" +
            "                    var options = [];\n" +
            "                    for(let i = 0; i < optionElement.length; i++){\n" +
            "                        options.push({\n" +
            "                            " + OPTION_ITEM_STRING_NAME + ": optionElement[i].innerText,\n" +
            "                            " + OPTION_ITEM_VALUE_NAME + ": optionElement[i].value\n" +
            "                        });\n" +
            "                    }\n" +
            "                    FloatWindowExpansionFunction.selectClick(JSON.stringify(options));\n" +
            "                });\n" +
            "            }\n" +
            "        }\n" +
            "    });\n" +
            "    window.FloatWindowFindSelect.observe(\n" +
            "        document,\n" +
            "        {\n" +
            "            childList: true,\n" +
            "            subtree: true\n" +
            "        }\n" +
            "    );\n" +
            "}";

    public static final String YOUTUBE_CONTINUE_PLAY= "(function() {\n" +
            "    if(window.youtubeContinuePlay) return;\n" +
            "    window.youtubeContinuePlay = true;\n" +
            "    let pausedFun = function({target: videoPlayer}){\n" +
            "        console.debug('暫停播放');\n" +
            "        setTimeout(function(){\n" +
            "            let ytConfirmDialog = document.querySelector('yt-confirm-dialog-renderer') || document.querySelector('dialog');\n" +
            "            if(\n" +
            "                ytConfirmDialog &&\n" +
            "                (\n" +
            "                    ytConfirmDialog.parentElement.style.display != 'none' ||\n" +
            "                    (\n" +
            "                        document.hidden &&\n" +
            "                        videoPlayer.currentTime < videoPlayer.duration//防止重複播放\n" +
            "                    )//當網頁不可見時，DOM元件不會即時渲染，所以對話方塊的display還會是none\n" +
            "                )\n" +
            "            ){\n" +
            "                console.debug('被暫停了，但是我要繼續播放');\n" +
            "                //ytConfirmDialog.querySelector('yt-button-renderer[dialog-confirm]').click();//當網頁不可見時，觸發click是不會繼續播放的，因為要等到網頁可見時觸發UI渲染後才會把對話方塊關掉，對話方塊關掉後才會出發video的play事件\n" +
            "                videoPlayer.play();\n" +
            "                console.debug('按下\"是\"');\n" +
            "            }else console.debug('對話方塊找不到或是隱藏了', ytConfirmDialog && ytConfirmDialog.parentElement, document.hidden, videoPlayer.currentTime, videoPlayer.duration);\n" +
            "        }, 500);//確保在暫停時對話方塊一定找得到\n" +
            "    }\n" +
            "    let listenerVideoPlayer = function(doc){\n" +
            "        let pathname = new URL(location.href).pathname;\n" +
            "        let videoPlayer = doc.querySelector('video');\n" +
            " \n" +
            "        console.debug(pathname, pathname.startsWith('/watch'))\n" +
            "        if(!videoPlayer || !pathname.startsWith('/watch')){\n" +
            "            console.debug('找不到播放器');\n" +
            "            return false;\n" +
            "        }\n" +
            "        videoPlayer.addEventListener('pause', pausedFun);\n" +
            "        console.debug('找到播放器，開始監聽', videoPlayer);\n" +
            "        return true;\n" +
            "    }\n" +
            "    let ycpObserver = new MutationObserver(([{target: doc}], observer) => {\n" +
            "        console.debug('頁面更動', ycpObserver);\n" +
            "        if(listenerVideoPlayer(doc)) ycpObserver.disconnect();\n" +
            "    });\n" +
            "    ycpObserver.observe(\n" +
            "        document,\n" +
            "        {\n" +
            "            childList: true,\n" +
            "            subtree: true\n" +
            "        }\n" +
            "    );\n" +
            "})();";
}
