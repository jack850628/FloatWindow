package com.jack8.floatwindow.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WindowManager {
    static final int NON_FOCUSED_WINDOW = -1;
    static final HashMap<Integer,WindowStruct> windowList = new HashMap<>();
    static int focusedWindowNumber = NON_FOCUSED_WINDOW;//現在焦點視窗

    public static int getMiniFreeNumber(){
        return getMiniFreeNumber(getAllWindowNumber());
    }

    public static int getMiniFreeNumber(Integer[] numbers){
        int offset = 0, l = 0, n = numbers.length, u = n -1;
        while (n > 0){
            int m = (l + u) / 2;
            int left = 0, right = 0;
            for(; right < n; right++){
                if(numbers[offset + right] <= m){
                    int temp = numbers[offset + left];
                    numbers[offset + left] = numbers[offset + right];
                    numbers[offset + right] = temp;
                    left++;
                }
            }
            if(left == m - l + 1){
                offset += left;
                n -= left;
                l = m + 1;
            }else{
                n = left;
                u = m;
            }
        }
        return l;
    }

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
