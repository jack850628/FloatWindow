package com.jack8.floatwindow.Window;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utilities {
    public static final ExecutorService threadPool = Executors.newCachedThreadPool();
    public static final Handler uiThread = new Handler(Looper.getMainLooper());
}
