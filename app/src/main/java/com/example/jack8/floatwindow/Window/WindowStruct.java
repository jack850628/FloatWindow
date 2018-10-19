package com.example.jack8.floatwindow.Window;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.jack8.floatwindow.R;

import java.util.HashMap;


public class WindowStruct implements View.OnClickListener,View.OnTouchListener,Runnable{
    private static int Index=0;//計算視窗開啟數量
    int Number;//視窗編號
    static int NOW_FOCUS_NUMBER=-1;//現在點視窗
    private int MINI_SIZE;//視窗最小化的寬度
    private int Second=500;//動畫持續時間
    //static final int START_POINT=60;//視窗預設座標
    private static int FOCUS_FLAGE=WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//上視窗超出螢幕
            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL//使可以操作視窗後方的物件
            ;
    private static int NO_FOCUS_FLAGE=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

    private int top,left,height,width;//視窗的座標及大小

    private Context context;
    private WindowColor wColor;//視窗顏色
    private WindowManager wm;
    private WindowManager.LayoutParams wmlp;
    private WindowAction windowAction;
    private View winform;//視窗外框
    private ViewGroup wincon;//視窗內容框
    private View[] winconPage;//視窗子頁面
    private int currentWindowPagePosition=0;
    private Scroller topMini,heightMini;
    private Button menu,close_button,mini,max,hide;
    private LinearLayout sizeBar;
    private TextView title;
    private String[] windowTitle;

    private Handler runUi= new Handler();

    private DisplayMetrics displayMetrics;

    private constructionAndDeconstructionWindow CDAW;

//------------可隱藏或顯示的控制項物件------------------
    private int display_oblect;
    public final static int ALL_NOT_DISPLAY = 0x00;
    public final static int MENU_BUTTON = 0x01;
    public final static int HIDE_BUTTON = 0x02;
    public final static int MINI_BUTTON = 0x04;
    public final static int MAX_BUTTON  = 0x08;
    public final static int SIZE_BAR = 0x10;
//-------------------------------------------------------

    boolean isMini=false;//是否最小化
    boolean isMax=false;//是否最大化
    boolean close=false;//是否是關閉視窗

    static HashMap<Integer,WindowStruct> windowList=new HashMap<>();

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPagesForLayoutResources,windowPageTitles,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                windowAction,CDAW);
    }
    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPagesForLayoutResources,windowPageTitles,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                display_object,
                windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles ,Object[][] windowInitArgs, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPagesForLayoutResources,windowPageTitles,windowInitArgs,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                windowAction,CDAW);
    }
    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles ,Object[][] windowInitArgs, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPagesForLayoutResources,windowPageTitles,windowInitArgs,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                display_object,
                windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles ,Object[][] windowInitArgs, int Top, int Left, int Height, int Width, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        View[] windowPages=new View[windowPagesForLayoutResources.length];
        View winform= LayoutInflater.from(context).inflate(R.layout.window,null);
        for(int i=0;i<windowPages.length;i++) {
            windowPages[i] = LayoutInflater.from(context).inflate(windowPagesForLayoutResources[i], (ViewGroup) winform, false);
            //windowPages[i].setTag(windowPageTitles[i]);
        }
        initWindow(context,wm,windowPages,windowPageTitles,windowInitArgs,Top,Left,Height,Width,MENU_BUTTON|HIDE_BUTTON|MINI_BUTTON|MAX_BUTTON|SIZE_BAR,windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles ,Object[][] windowInitArgs, int Top, int Left, int Height, int Width, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        View[] windowPages=new View[windowPagesForLayoutResources.length];
        View winform= LayoutInflater.from(context).inflate(R.layout.window,null);
        for(int i=0;i<windowPages.length;i++) {
            windowPages[i] = LayoutInflater.from(context).inflate(windowPagesForLayoutResources[i], (ViewGroup) winform, false);
            //windowPages[i].setTag(windowPageTitles[i]);
        }
        initWindow(context,wm,windowPages,windowPageTitles,windowInitArgs,Top,Left,Height,Width,display_object,windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles, int Top, int Left, int Height, int Width, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        View[] windowPages=new View[windowPagesForLayoutResources.length];
        View winform= LayoutInflater.from(context).inflate(R.layout.window,null);
        for(int i=0;i<windowPages.length;i++) {
            windowPages[i] = LayoutInflater.from(context).inflate(windowPagesForLayoutResources[i], (ViewGroup) winform, false);
            //windowPages[i].setTag(windowPageTitles[i]);
        }
        initWindow(context,wm,windowPages,windowPageTitles,new Object[windowPages.length][0],Top,Left,Height,Width,MENU_BUTTON|HIDE_BUTTON|MINI_BUTTON|MAX_BUTTON|SIZE_BAR,windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPagesForLayoutResources 浮動視窗所有子頁面的View的Resources ID
     * @param windowPageTitles 所有子頁面的標題
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, int[] windowPagesForLayoutResources, String[] windowPageTitles, int Top, int Left, int Height, int Width, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        View[] windowPages=new View[windowPagesForLayoutResources.length];
        View winform= LayoutInflater.from(context).inflate(R.layout.window,null);
        for(int i=0;i<windowPages.length;i++) {
            windowPages[i] = LayoutInflater.from(context).inflate(windowPagesForLayoutResources[i], (ViewGroup) winform, false);
            //windowPages[i].setTag(windowPageTitles[i]);
        }
        initWindow(context,wm,windowPages,windowPageTitles,new Object[windowPages.length][0],Top,Left,Height,Width,display_object,windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPages,windowPageTitles,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPages,windowPageTitles,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                display_object,
                windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles ,Object[][] windowInitArgs , WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPages,windowPageTitles,windowInitArgs,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                MENU_BUTTON|HIDE_BUTTON|MINI_BUTTON|MAX_BUTTON|SIZE_BAR,
                windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles ,Object[][] windowInitArgs, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        this(context,wm,windowPages,windowPageTitles,windowInitArgs,60,60,
                (int)(context.getResources().getDisplayMetrics().density*240),(int)(context.getResources().getDisplayMetrics().density*200),
                display_object,
                windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles, int Top, int Left, int Height, int Width, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        initWindow(context,wm,windowPages,windowPageTitles,new Object[windowPages.length][0],Top,Left,Height,Width,MENU_BUTTON|HIDE_BUTTON|MINI_BUTTON|MAX_BUTTON|SIZE_BAR,windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles, int Top, int Left, int Height, int Width, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        initWindow(context,wm,windowPages,windowPageTitles,new Object[windowPages.length][0],Top,Left,Height,Width,display_object,windowAction,CDAW);
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param  display_object 表示要顯示那些windows控制物件
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles ,Object[][] windowInitArgs , int Top, int Left, int Height, int Width, int display_object, WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        initWindow(context,wm,windowPages,windowPageTitles,windowInitArgs,Top,Left,Height,Width,display_object,windowAction,CDAW);
    }

    private void initWindow(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles,Object[][] windowInitArgs , int Top, int Left, int Height, int Width, int display_object,WindowAction windowAction,constructionAndDeconstructionWindow CDAW){
        if(windowList.containsKey(WindowStruct.NOW_FOCUS_NUMBER)){
            WindowStruct WS=windowList.get(WindowStruct.NOW_FOCUS_NUMBER);
            if(!WS.isMini)
                WS.getWindowFrom().unFocusWindow();
        }
        this.Number=Index++;
        WindowStruct.NOW_FOCUS_NUMBER=this.Number;
        this.context=context;
        this.wm=wm;
        this.winconPage=windowPages;
        this.windowAction=windowAction;
        this.windowTitle=windowPageTitles;
        this.CDAW=CDAW;
        this.Second=context.getSharedPreferences(WindowConfig.WINDOW_CONF,0).getInt(WindowConfig.SECOND,500);
        this.display_oblect=display_object;
        windowList.put(Number,this);
        topMini=new Scroller(context);
        heightMini=new Scroller(context);
        displayMetrics = context.getResources().getDisplayMetrics();

        wColor=new WindowColor(context);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        wmlp = new WindowManager.LayoutParams();
        wmlp.type= WindowManager.LayoutParams.TYPE_PHONE;//類型
        wmlp.format = PixelFormat.RGBA_8888;//背景(透明)
        wmlp.flags=FOCUS_FLAGE;
        wmlp.gravity = Gravity.LEFT | Gravity.TOP;//設定重力(初始位置)
        wmlp.x=Top;//設定原點座標
        wmlp.y=Left;
        wmlp.width = WindowManager.LayoutParams.WRAP_CONTENT;//設定視窗大小
        wmlp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        winform= LayoutInflater.from(context).inflate(R.layout.window,null);
        ((WindowFrom)winform).setLayoutParams(wmlp,this);
        /*winform.setOnTouchListener(new View.OnTouchListener() {
            View titleBar=winform.findViewById(R.id.title_bar),
                    microMaxButtonBackground=winform.findViewById(R.id.micro_max_button_background),
                    closeButtonBackground=winform.findViewById(R.id.close_button_background);
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect rect = new Rect();
                v.getGlobalVisibleRect(rect);//取得視窗所在的範圍
                if (!rect.contains((int) event.getX(), (int) event.getY())) {//當Touch的點在視窗範圍外
                    wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                    WindowStruct.this.wm.updateViewLayout(winform, wmlp);
                    titleBar.setBackgroundColor(wColor.getWindowNotFoucs());
                    sizeBar.setBackgroundColor(wColor.getWindowNotFoucs());
                    microMaxButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
                    closeButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
                }
                return false;
            }
        });*/
        wm.addView(winform,wmlp);
        wincon=(ViewGroup) winform.findViewById(R.id.wincon);

        top=Top;
        left=Left;
        width=Width;
        height=Height;
        MINI_SIZE=winform.findViewById(R.id.close_button).getLayoutParams().width;

        menu=(Button) winform.findViewById(R.id.menu);
        close_button=(Button) winform.findViewById(R.id.close_button);
        mini=(Button) winform.findViewById(R.id.mini);
        max=(Button) winform.findViewById(R.id.max);
        hide=(Button) winform.findViewById(R.id.hide);
        sizeBar=(LinearLayout) winform.findViewById(R.id.size);
        title=(TextView) winform.findViewById(R.id.title);

        //-------------------------建立視窗內容畫面----------------------------------------------------
        wincon.addView(winconPage[0]);
        title.setText(windowPageTitles[0]);
        title.setOnTouchListener(this);
        title.setOnClickListener(this);//還原視窗大小
        close_button.setOnClickListener(this);
        winform.findViewById(R.id.size).setOnTouchListener(new View.OnTouchListener() {
            float Wlength=-1,Hlength=-1;
            @Override
            public boolean onTouch(View v, MotionEvent event) {//調整視窗大小
                //wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    if(Wlength==-1||Hlength==-1){
                        Wlength=wincon.getWidth()-event.getX();//取得點擊X點到視窗最右側的距離
                        Hlength=(event.getRawY()-wmlp.y)-wincon.getHeight();//取得視窗標題列高度以及視窗大小調整列被點擊以上的高度，視窗內部框架高度-(點擊座標Y-視窗Top座標)
                        return true;
                    }
                    Log.i("size",(int)(event.getX()+Wlength)+"   "+(int)(event.getRawY()-wmlp.y+Hlength));
                    int temp;
                    winform.getLayoutParams().width=width=(temp=(int)(event.getX()+Wlength))>30?temp:30;
                    winform.getLayoutParams().height=height=((temp=(int)(event.getRawY()-wmlp.y-Hlength))>=0?temp:0)
                            +title.getLayoutParams().height+sizeBar.getLayoutParams().height;//Touch的Y減去視窗的Top再減去Hlength就是視窗內容區要調整的高度
                    Log.i("size2",wincon.getLayoutParams().width+"   "+wincon.getLayoutParams().height);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    Wlength=-1;
                    Hlength=-1;
                }
                WindowStruct.this.wm.updateViewLayout(winform, wmlp);
                return true;
            }
        });
        //-----------------------------------------------------------------------------
        //-------------------------建立Menu-------------------------------
        /*final PopupMenu pm=new PopupMenu(context,menu);
        Menu m=pm.getMenu();
        for(int i=0;i<winconPage.length;i++)
            //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
            m.add(0,i,i,(String)winconPage[i].getTag());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            int ViweIndex=0;
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                currentWindowPagePosition=item.getItemId();
                wincon.removeView(winconPage[ViweIndex]);
                wincon.addView(winconPage[item.getItemId()]);
                title.setText(windowTitle[item.getItemId()]);
                ViweIndex=item.getItemId();
                return true;
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm.show();
            }
        });*/
        menu.setOnClickListener(new menuList(windowPageTitles));
        //------------------------------------------------------------------
        //---------------------------縮到最小與最大按鈕---------------------------
        hide.setOnClickListener(this);
        mini.setOnClickListener(this);
        max.setOnClickListener(this);
        //------------------------------------------------------------------
        //---------------------------初始化視窗內容-------------------------------
        for(int i=0;i<winconPage.length;i++)
            CDAW.Construction(context,winconPage[i],i,(windowInitArgs != null && i < windowInitArgs.length) ? windowInitArgs[i] : new Object[0],this);
        //---------------------------------------------------------------------------------------------
        //---------------------------隱藏不顯示的控制項物件------------------------------------------
        if((display_object & MENU_BUTTON) != MENU_BUTTON)
            menu.setVisibility(View.GONE);
        if((display_object & HIDE_BUTTON) != HIDE_BUTTON)
            hide.setVisibility(View.GONE);
        if((display_object & MINI_BUTTON) != MINI_BUTTON)
            mini.setVisibility(View.GONE);
        if((display_object & MAX_BUTTON) != MAX_BUTTON)
            max.setVisibility(View.GONE);
        if((display_object & SIZE_BAR) != SIZE_BAR)
            sizeBar.setVisibility(View.GONE);
        //-------------------------------------------------------------------------------------
        //---------------------------視窗開啟動畫------------------------------------------------------
        topMini.startScroll(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2 ,left - displayMetrics.widthPixels / 2, top - displayMetrics.heightPixels / 2, Second);
        heightMini.startScroll(0, 0, width, height, Second);
        runUi.post(this);
        //---------------------------------------------------------------------------------------------
    }

    public interface WindowAction{
        void goHide(WindowStruct windowStruct);//當按下隱藏視窗按鈕
        void goClose();//當按下關閉視窗按鈕
    }

    public interface constructionAndDeconstructionWindow{
        void Construction(Context context, View pageView, int position,Object[] args , WindowStruct windowStruct);
        void Deconstruction(Context context, View pageView, int position);
    }

    public class menuList implements View.OnClickListener,AdapterView.OnItemClickListener,Runnable{
        private int ViweIndex=0;
        private ListView menu;
        private LinearLayout menuListAndContext;
        private Scroller scroller=new Scroller(context);
        private boolean isOpen=false;
        public menuList(final String[] menuItems){
            menuListAndContext=(LinearLayout) winform.findViewById(R.id.menu_list_and_context);
            menu=(ListView) winform.findViewById(R.id.menu_list);
            menu.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return menuItems.length;
                }

                @Override
                public Object getItem(int position) {
                    return menuItems[position];
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView==null){
                        convertView=LayoutInflater.from(context).inflate(R.layout.hide_menu_item,null,false);
                        TextView itenText=(TextView) convertView.findViewById(R.id.item_text);
                        itenText.setText(menuItems[position]);
                        itenText.setTextColor(Color.BLACK);
                    }
                    return convertView;
                }
            });
            menu.setOnItemClickListener(this);
        }
        public void showMenu(){
            scroller.startScroll(0,0,-menu.getLayoutParams().width,0);
            runUi.post(this);
        }
        public void closeMenu(){
            scroller.startScroll(-menu.getLayoutParams().width,0,menu.getLayoutParams().width,0);
            runUi.post(this);
        }
        @Override
        public void onClick(View v) {
            Log.i("startMenu",isOpen+","+menu.getLayoutParams().width);
            if(!isOpen) {
                showMenu();
                isOpen=true;
            }else {
                closeMenu();
                isOpen=false;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            isOpen=false;
            currentWindowPagePosition=position;
            wincon.removeView(winconPage[ViweIndex]);
            wincon.addView(winconPage[position]);
            title.setText(windowTitle[position]);
            ViweIndex=position;
            closeMenu();
        }

        @Override
        public void run() {
            if(scroller.computeScrollOffset()) {
                menuListAndContext.scrollTo(scroller.getCurrX(), scroller.getCurrY());
                runUi.post(this);
            }else
                menuListAndContext.invalidate();
        }
    }

    /*
    移動視窗
    關於getX,getY等等的含數用法:
    http://blog.csdn.net/u013872857/article/details/53750682
     */
    private float H=-1,W=-1;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!isMax||isMini) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (H == -1 || W == -1) {
                    H = event.getX();//取得點擊的X座標到視窗頂點的距離
                    W = event.getY();//取得點擊的Y座標到視窗頂點的距離
                    return true;
                }
                wmlp.x = (int) (event.getRawX() - H);
                wmlp.y = (int) (event.getRawY() - W - getStatusBarHeight());//60為狀態列高度
                if (!isMini && !isMax) {
                    if((display_oblect & MENU_BUTTON) == MENU_BUTTON)
                        wmlp.x -= menu.getLayoutParams().width;
                    left = wmlp.x;
                    top = wmlp.y;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                H = -1;
                W = -1;
            }
            wm.updateViewLayout(winform, wmlp);
        }
        return false;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mini://最小化
                mini();
                break;
            case R.id.max: //最大化或一般大小
                maxOrUnMax();
                break;
            case R.id.title://還原視窗大小
                reSize();
                break;
            case R.id.close_button://關閉視窗
                close();
                break;
            case R.id.hide://隱藏視窗
                hide();
                break;
        }
    }

    @Override
    public void run() {//放大縮小動畫
        if(!topMini.isFinished()||!heightMini.isFinished()) {
            if (topMini.computeScrollOffset()) {
                wmlp.x = topMini.getCurrX();
                wmlp.y = topMini.getCurrY();
            }
            if (heightMini.computeScrollOffset()) {
                winform.getLayoutParams().width = heightMini.getCurrX();
                winform.getLayoutParams().height = heightMini.getCurrY();
            }
            wm.updateViewLayout(winform, wmlp);
            runUi.post(this);
        }else if(close){
            wm.removeView(winform);
            for(int i=0;i<winconPage.length;i++)
                CDAW.Deconstruction(context,winconPage[i],i);
            windowAction.goClose();
        }
    }

    /**
     * 視窗最小化
     */
    public void mini(){
        wmlp.flags=NO_FOCUS_FLAGE;
        wm.updateViewLayout(winform,wmlp);
        if (!isMini) {
            isMini = true;
            if(!isMax) {
                topMini.startScroll(left, top, (displayMetrics.widthPixels - MINI_SIZE) - left, -top, Second);
                heightMini.startScroll(width, height, MINI_SIZE - width, -(height-title.getLayoutParams().height), Second);
            }else{
                int dy;
                topMini.startScroll(0, 0, (displayMetrics.widthPixels - MINI_SIZE),0, Second);
                heightMini.startScroll( displayMetrics.widthPixels,
                        dy=displayMetrics.heightPixels - getStatusBarHeight(),
                        MINI_SIZE -displayMetrics.widthPixels, -(dy-title.getLayoutParams().height),Second);
            }
            if((display_oblect & MENU_BUTTON) == MENU_BUTTON)
                menu.setVisibility(View.GONE);
            close_button.setVisibility(View.GONE);
            if((display_oblect & MINI_BUTTON) == MINI_BUTTON)
                mini.setVisibility(View.GONE);
            if((display_oblect & MAX_BUTTON) == MAX_BUTTON)
                max.setVisibility(View.GONE);
            if((display_oblect & HIDE_BUTTON) == HIDE_BUTTON)
                hide.setVisibility(View.GONE);
            if((display_oblect & SIZE_BAR) == SIZE_BAR)
                sizeBar.setVisibility(View.GONE);
        }
        runUi.post(this);
    }

    /**
     * 視窗最大化或一般大小
     */
    public void maxOrUnMax(){
        if(!isMax) {
            isMax=true;
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                max.setBackground(context.getResources().getDrawable(R.drawable.mini_window));
            else
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mini_window));
            topMini.startScroll(left, top, -left, -top, Second);
            heightMini.startScroll(width, height, displayMetrics.widthPixels - width,
                    displayMetrics.heightPixels - height - getStatusBarHeight(), Second);
            if((display_oblect & SIZE_BAR) == SIZE_BAR)
                sizeBar.setVisibility(View.GONE);
        }else{
            isMax=false;
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                max.setBackground(context.getResources().getDrawable(R.drawable.max_window));
            else
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.max_window));
            int dy;
            topMini.startScroll(0, 0, left, top, Second);
            heightMini.startScroll( displayMetrics.widthPixels,
                    dy=displayMetrics.heightPixels - getStatusBarHeight(),
                    width-displayMetrics.widthPixels, height-dy,Second);
            if((display_oblect & SIZE_BAR) == SIZE_BAR)
                sizeBar.setVisibility(View.VISIBLE);
        }
        runUi.post(this);
    }

    /**
     * 還原視窗大小
     */
    public void reSize(){
        wmlp.flags=FOCUS_FLAGE;
        wm.updateViewLayout(winform,wmlp);
        if(isMini){
            isMini = false;
            if(!isMax) {
                topMini.startScroll(wmlp.x, wmlp.y, left - wmlp.x, top - wmlp.y, Second);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                        , width - winform.getLayoutParams().width,
                        height - winform.getLayoutParams().height, Second);
                if((display_oblect & SIZE_BAR) == SIZE_BAR)
                    sizeBar.setVisibility(View.VISIBLE);
            }else{
                topMini.startScroll(wmlp.x, wmlp.y, -wmlp.x, -wmlp.y, Second);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                        , displayMetrics.widthPixels - winform.getLayoutParams().width,
                        displayMetrics.heightPixels - winform.getLayoutParams().height - getStatusBarHeight(), Second);
            }
            if((display_oblect & MENU_BUTTON) == MENU_BUTTON)
                menu.setVisibility(View.VISIBLE);
            close_button.setVisibility(View.VISIBLE);
            if((display_oblect & MINI_BUTTON) == MINI_BUTTON)
            mini.setVisibility(View.VISIBLE);
            if((display_oblect & MAX_BUTTON) == MAX_BUTTON)
            max.setVisibility(View.VISIBLE);
            if((display_oblect & HIDE_BUTTON) == HIDE_BUTTON)
            hide.setVisibility(View.VISIBLE);
        }
        runUi.post(this);
    }

    /**
     * 視窗關閉
     */
    public void close(){
        windowList.remove(Number);
        close=true;
        if(!isMax) {
            topMini.startScroll(left, top, displayMetrics.widthPixels / 2 - left, displayMetrics.heightPixels / 2 - top, Second);
            heightMini.startScroll(width, height,
                    - width, -height, Second);
        }else{
            topMini.startScroll(0, 0, displayMetrics.widthPixels / 2 , displayMetrics.heightPixels / 2 , Second);
            heightMini.startScroll(displayMetrics.widthPixels, displayMetrics.heightPixels - getStatusBarHeight()
                    , - displayMetrics.widthPixels, -(displayMetrics.heightPixels - getStatusBarHeight()), Second);
        }
        runUi.post(this);
    }

    /**
     * 視窗隱藏
     */
    public void hide(){
        wmlp.flags=NO_FOCUS_FLAGE;
        wm.updateViewLayout(winform,wmlp);
        windowAction.goHide(this);
        if(!isMax) {
            topMini.startScroll(left, top, displayMetrics.widthPixels / 2 - left, displayMetrics.heightPixels / 2 - top, Second);
            heightMini.startScroll(width, height,
                    - width, -height, Second);
        }else{
            topMini.startScroll(0, 0, displayMetrics.widthPixels / 2 , displayMetrics.heightPixels / 2 , Second);
            heightMini.startScroll(displayMetrics.widthPixels, displayMetrics.heightPixels - getStatusBarHeight()
                    , - displayMetrics.widthPixels, -(displayMetrics.heightPixels - getStatusBarHeight()), Second);
        }
        runUi.post(this);
    }

    /**
     * 取消視窗隱藏
     */
    public void unHide(){
        wmlp.flags=FOCUS_FLAGE;
        wm.updateViewLayout(winform,wmlp);
        if(windowList.containsKey(WindowStruct.NOW_FOCUS_NUMBER)) {
            WindowStruct WS=windowList.get(WindowStruct.NOW_FOCUS_NUMBER);
            if(!WS.isMini)
                WS.getWindowFrom().unFocusWindow();
        }
        WindowStruct.NOW_FOCUS_NUMBER=this.Number;
        ((WindowFrom)winform).focusWindow();
        wm.removeView(winform);
        wm.addView(winform,wmlp);
        if(!isMax) {
            topMini.startScroll(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2 ,left - displayMetrics.widthPixels / 2, top - displayMetrics.heightPixels / 2, Second);
            heightMini.startScroll(0, 0, width, height, Second);
        }else{
            topMini.startScroll(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2 , -(displayMetrics.widthPixels / 2), -(displayMetrics.heightPixels / 2) , Second);
            heightMini.startScroll(0,0, displayMetrics.widthPixels,displayMetrics.heightPixels - getStatusBarHeight(), Second);
        }
        runUi.post(this);
    }

    /**
     * 設定視窗標題
     * @param position 表示要設定標題的子頁面編號
     * @param titleText 標題文字
     */
    public void setWindowTitle(int position, String titleText){
        windowTitle[position]=titleText;
        if(currentWindowPagePosition==position)
            title.setText(titleText);
    }

    /**
     * 取得視窗標題
     * @param position 表示要取得標題的子頁面編號
     * @return 標題文字
     */
    public String getWindowTitle(int position){
        return windowTitle[position];
    }

    /**
     * 取得當前視窗標題
     * @return 標題文字
     */
    public String getWindowTitle(){
        return title.getText().toString();
    }

    /**
     * 取得狀態列的高度
     * @return 狀態列的高度
     */
    private int getStatusBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
    public WindowFrom getWindowFrom(){
        return (WindowFrom)winform;
    }
}
