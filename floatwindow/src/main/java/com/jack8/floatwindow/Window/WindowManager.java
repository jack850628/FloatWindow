package com.jack8.floatwindow.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WindowManager {
    static final int NON_FOCUSED_WINDOW = -1;
    static final HashMap<Integer,WindowStruct> windowList = new HashMap<>();
    static int focusedWindowNumber = NON_FOCUSED_WINDOW;//現在焦點視窗

    public static WindowStruct getWindowStruct(int number){
        return windowList.get(number);
    }

     static void addWindowStruct(WindowStruct windowStruct){
        windowList.put(windowStruct.Number, windowStruct);
    }

    static void removeWindowStruct(WindowStruct windowStruct){
        windowList.remove(windowStruct.Number);
    }

    public static int count(){
        return windowList.size();
    }

    public static boolean windowIn(int windowsNumber){
        return windowList.containsKey(windowsNumber);
    }

    public static Integer[] getAllWindowNumber(){
        return windowList.keySet().toArray(new Integer[windowList.size()]);
    }

    public static int getFocusedWindowNumber(){
        return focusedWindowNumber;
    }

    public static Set<Map.Entry<Integer, WindowStruct>> entrySet(){
        return windowList.entrySet();
    }
}
