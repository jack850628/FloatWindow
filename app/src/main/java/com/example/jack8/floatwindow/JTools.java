package com.example.jack8.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.jack8.floatwindow.Window.WindowStruct;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class JTools {
    public static final class WindowParameter{
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String TOP = "top";
        public static final String LEFT = "left";
        public static final String STATE = "state";
        public static final Set<String> parametersSet;
        static {
            parametersSet = new HashSet<>();
            parametersSet.add(WIDTH);
            parametersSet.add(HEIGHT);
            parametersSet.add(TOP);
            parametersSet.add(LEFT);
            parametersSet.add(STATE);
        }

        private WindowParameter(){}
    }

    public static final class IntentParameter{
        public static final String PATH = "path";

        private IntentParameter(){}
    }

    public static final class WindowBuilderByIntent{
        private Intent intent;
        private int top = Integer.MAX_VALUE;
        private int left = Integer.MAX_VALUE;
        private int height = Integer.MAX_VALUE;
        private int width = Integer.MAX_VALUE;
        private int state = Integer.MAX_VALUE;
        public WindowBuilderByIntent(Intent intent){
            this.intent = intent;
        }

        public WindowBuilderByIntent setTop(int top){
            this.top = top;
            return this;
        }
        public WindowBuilderByIntent setLeft(int left){
            this.left = left;
            return this;
        }
        public WindowBuilderByIntent setWidth(int width){
            this.width = width;
            return this;
        }
        public WindowBuilderByIntent setHeight(int height){
            this.height = height;
            return this;
        }
        public WindowBuilderByIntent setState(int state){
            this.state = state;
            return this;
        }

        public WindowStruct.Builder create(Context context, WindowManager windowManager){
            WindowStruct.Builder builder = new WindowStruct.Builder(context, windowManager);
            int intParam = intent.getIntExtra(WindowParameter.TOP, top);
            if(intParam != Integer.MAX_VALUE){
                builder.top(intParam);
            }
            intParam = intent.getIntExtra(WindowParameter.LEFT, left);
            if(intParam != Integer.MAX_VALUE){
                builder.left(intParam);
            }
            intParam = intent.getIntExtra(WindowParameter.WIDTH, width);
            if(intParam != Integer.MAX_VALUE){
                builder.width(intParam);
            }
            intParam = intent.getIntExtra(WindowParameter.HEIGHT, height);
            if(intParam != Integer.MAX_VALUE){
                builder.height(intParam);
            }
            intParam = intent.getIntExtra(WindowParameter.STATE, state);
            if(intParam != Integer.MAX_VALUE){
                builder.openState(WindowStruct.State.getStateByTypeNumber(intParam));
            }
            return builder;
        }
    }

    public static final ExecutorService threadPool = Executors.newCachedThreadPool();
    public static Handler uiThread = new Handler(Looper.getMainLooper());

    public static String createAppUri(String[] path, WindowStruct windowStruct, Map<String, String> outerParameter){
        Uri.Builder builder = new Uri.Builder();
        for(String p: path){
            builder.appendEncodedPath(p);
        }
        builder.appendQueryParameter(WindowParameter.TOP, String.valueOf(windowStruct.getGeneralPositionY()));
        builder.appendQueryParameter(WindowParameter.LEFT, String.valueOf(windowStruct.getGeneralPositionX()));
        builder.appendQueryParameter(WindowParameter.WIDTH, String.valueOf(windowStruct.getWidth()));
        builder.appendQueryParameter(WindowParameter.HEIGHT, String.valueOf(windowStruct.getHeight()));
        builder.appendQueryParameter(WindowParameter.STATE, String.valueOf(windowStruct.nowState.getType()));
        for(String k: outerParameter.keySet()){
            builder.appendQueryParameter(k, outerParameter.get(k));
        }
        return builder.build().toString();
    }

    public static Intent uriToIntent(Uri uri){
        Intent intent = new Intent();
        intent.putExtra(IntentParameter.PATH, uri.getPath());
        Set<String> queryParameterKeys = uri.getQueryParameterNames();
        for(String key: queryParameterKeys){
            if(WindowParameter.parametersSet.contains(key)){
                intent.putExtra(key, Integer.valueOf(Objects.requireNonNull(uri.getQueryParameter(key))));
            }else{
                intent.putExtra(key, uri.getQueryParameter(key));
            }
        }
        return intent;
    }

    public static String popPathFirstDirectoryNameFromIntent(Intent intent){
        String pathStr = intent.getStringExtra(IntentParameter.PATH);
        if(pathStr == null) return "";
        File path = new File(pathReverse(pathStr));
        String firstDirectoryName = path.getName();
        intent.putExtra(IntentParameter.PATH,
                firstDirectoryName.equals("")
                        ? path.getPath()
                        : pathReverse(Objects.requireNonNull(path.getParent()))
        );
        return firstDirectoryName;
    }

    public static String pathReverse(String pathStr){
        List<String> list = new LinkedList<String>(Arrays.asList(pathStr.split("/")));
        if(list.size() == 0) return pathStr;
        list.remove(0);
        Collections.reverse(list);
        return  "/" + TextUtils.join("/", list);
    }

    public static void intentExtraCopyToIntent(Intent from, Intent to){
        if(to == null || from == null || from.getExtras() == null) return;
        to.putExtras(from.getExtras());
    }

    public static void workPhaseRecover(Context context, String[] uris){
        for(String uri: uris) {
            try {
                Intent i = JTools.uriToIntent(Uri.parse(uri));
                i.setClass(context, Class.forName(JTools.popPathFirstDirectoryNameFromIntent(i)));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                }else{
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                context.startActivity(i);
            } catch (ClassNotFoundException e) {
                Log.e("workPhaseRecover", e.toString());
                e.printStackTrace();
            }
        }
    }

    private JTools(){}
}
