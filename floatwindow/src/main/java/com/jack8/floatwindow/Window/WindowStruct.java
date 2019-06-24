package com.jack8.floatwindow.Window;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import com.jack8.floatwindow.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class WindowStruct implements View.OnClickListener,View.OnTouchListener{
    private static int Index = 0;//計算視窗開啟數量

    final int Number;//視窗編號
    static int NOW_FOCUS_NUMBER = -1;//現在點視窗

    private final int parentWindowNumber;//父視窗編號
    private final HashSet<Integer> subWindowNumbers = new HashSet<>();//所有子視窗編號
    private int MINI_SIZE;//視窗最小化的寬度
    private static final int TITLE_LIFT_TO_EDGE_DISTANCE = 10;
    //static final int START_POINT = 60;//視窗預設座標
    private static final int FOCUS_FLAGE = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//讓視窗超出螢幕
            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL//使可以操作視窗後方的物件
            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH//如果你已經設置了FLAG_NOT_TOUCH_MODAL,那麼你可以設置FLAG_WATCH_OUTSIDE_TOUCH這個flag, 這樣一個點擊事件如果發生在你的window之外的範圍,你就會接收到一個特殊的MotionEvent,MotionEvent.ACTION_OUTSIDE 注意,你只會接收到點擊事件的第一下,而之後的DOWN/MOVE/UP等手勢全都不會接收到
            ;
    private static final int NO_FOCUS_FLAGE = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//讓視窗超出螢幕
            |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            ;
    private static final int NO_FOCUS_FLAGE_FOR_MINI_STATE = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

    private int top,left,height,width;//視窗的座標及大小

    private Context context;
    //private WindowColor wColor;//視窗顏色
    private WindowManager wm;
    private WindowManager.LayoutParams wmlp;
    private ScreenSize screenSize;
    private WindowAction windowAction;
    private View winform;//視窗外框
    private ViewGroup wincon;//視窗內容框
    private View[] winconPage;//視窗子頁面
    private int transitionsDuration;//動畫持續時間
    private int currentWindowPagePosition = 0;
    private Scroller topMini,heightMini;
    private Button menu,close_button,mini,max,hide;
    private LinearLayout sizeBar,titleBarAndButtons,microMaxButtonBackground;
    private TextView title;
    private String[] windowTitle;

    private final Handler runUi= new Handler(Looper.getMainLooper());

    private constructionAndDeconstructionWindow CDAW;

    //------------可隱藏或顯示的控制項物件------------------
    private int display_object;
    public static final int ALL_NOT_DISPLAY = 0x00;
    public static final int MENU_BUTTON = 0x01;
    public static final int HIDE_BUTTON = 0x02;
    public static final int MINI_BUTTON = 0x04;
    public static final int MAX_BUTTON = 0x08;
    public static final int CLOSE_BUTTON = 0x10;
    public static final int SIZE_BAR = 0x20;
    public static final int TITLE_BAR_AND_BUTTONS = 0x40;
//-------------------------------------------------------

    public enum State{MAX,MINI,HIDE,GENERAL,CLOSE}
    public State nowState = State.GENERAL;//當前狀態
    public State previousState = null;//前一次的狀態

    static final HashMap<Integer,WindowStruct> windowList = new HashMap<>();

    /**
     * 建構WindowStruct的工廠模式
     */
    public static class Builder{
        private Context context;
        private WindowManager windowManager;
        private View[] windowPages;
        private String[] windowPageTitles = new String[]{""};
        private Object[][] windowInitArgs = new Object[0][0];
        private int top = 60;
        private int left = 60;
        private int height;
        private int width;
        private int displayObject = TITLE_BAR_AND_BUTTONS | MENU_BUTTON | HIDE_BUTTON | MINI_BUTTON | MAX_BUTTON | CLOSE_BUTTON | SIZE_BAR;
        private int transitionsDuration = 500;
        private int parentWindowNumber = -1;
        private WindowAction windowAction = new WindowAction() {
            @Override
            public void goHide(WindowStruct windowStruct) {

            }

            @Override
            public void goClose(WindowStruct windowStruct) {

            }
        };
        private constructionAndDeconstructionWindow constructionAndDeconstructionWindow = new constructionAndDeconstructionWindow() {
            @Override
            public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {

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
        };
        private ScreenSize screenSize;

        public Builder(Context context, final WindowManager windowManager){
            this.context = context;
            this.windowManager = windowManager;
            this.height = (int)(context.getResources().getDisplayMetrics().density*240);
            this.width = (int)(context.getResources().getDisplayMetrics().density*200);
            this.windowPages = new View[]{new LinearLayout(context)};
            this.screenSize = new ScreenSize(context){
                @Override
                public int getWidth() {
                    return super.context.getResources().getDisplayMetrics().widthPixels;
                }

                @Override
                public int getHeight() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
                        if(super.context.getResources().getDisplayMetrics().heightPixels > super.context.getResources().getDisplayMetrics().widthPixels)
                            return displayMetrics.heightPixels - getStatusBarHeight() - getNavigationBarHeight();
                    }
                    return super.context.getResources().getDisplayMetrics().heightPixels - getStatusBarHeight();
                }
            };
        }
        public Builder windowPages(View[] windowPages){
            this.windowPages = windowPages;
            return this;
        }
        public Builder windowPageTitles(String[] windowPageTitles){
            this.windowPageTitles = windowPageTitles;
            return this;
        }
        public Builder windowInitArgs(Object[][] windowInitArgs){
            this.windowInitArgs = windowInitArgs;
            return  this;
        }
        public Builder top(int top){
            this.top = top;
            return  this;
        }
        public Builder left(int left){
            this.left = left;
            return  this;
        }
        public  Builder height(int height){
            this.height = height;
            return  this;
        }
        public Builder width(int width){
            this.width = width;
            return this;
        }
        public Builder displayObject(int displayObject){
            this.displayObject = displayObject;
            return this;
        }
        public Builder transitionsDuration(int transitionsDuration){
            this.transitionsDuration = transitionsDuration;
            return this;
        }
        public Builder windowAction(WindowAction windowAction){
            this.windowAction = windowAction;
            return this;
        }
        public Builder constructionAndDeconstructionWindow(constructionAndDeconstructionWindow constructionAndDeconstructionWindow){
            this.constructionAndDeconstructionWindow = constructionAndDeconstructionWindow;
            return this;
        }
        public Builder windowPages(int[] windowPagesForLayoutResources){
            this.windowPages = new View[windowPagesForLayoutResources.length];
            View winform= LayoutInflater.from(context).inflate(R.layout.window,null);
            for(int i = 0;i<windowPages.length;i++) {
                this.windowPages[i] = LayoutInflater.from(context).inflate(windowPagesForLayoutResources[i], (ViewGroup) winform, false);
                //windowPages[i].setTag(windowPageTitles[i]);
            }
            return this;
        }

        public Builder screenSize(ScreenSize screenSize){
            this.screenSize = screenSize;
            return this;
        }

        public Builder parentWindow(WindowStruct parentWindow){
            this.parentWindowNumber = parentWindow.Number;
            return this;
        }
        public WindowStruct show(){
            return new WindowStruct(context, windowManager, windowPages, windowPageTitles, windowInitArgs, top, left, height, width, displayObject, transitionsDuration, screenSize, windowAction, constructionAndDeconstructionWindow, parentWindowNumber);
        }
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
     * @param  transitionsDuration 過場動畫持續時間
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, WindowManager wm, View[] windowPages, String[] windowPageTitles , Object[][] windowInitArgs , int Top, int Left, int Height, int Width, final int display_object, int transitionsDuration, ScreenSize screenSize, WindowAction windowAction, constructionAndDeconstructionWindow CDAW, int parentWindowNumber){
        if(windowList.containsKey(WindowStruct.NOW_FOCUS_NUMBER)){
            WindowStruct WS = windowList.get(WindowStruct.NOW_FOCUS_NUMBER);
            WS.unFocusWindow();
        }
        WindowStruct.NOW_FOCUS_NUMBER = this.Number = Index++;
        this.context = context;
        this.wm = wm;
        this.winconPage = windowPages;
        this.windowAction = windowAction;
        this.windowTitle = windowPageTitles;
        this.CDAW = CDAW;
        this.display_object = display_object;
        this.transitionsDuration = transitionsDuration;
        this.screenSize = screenSize;
        windowList.put(Number,this);
        topMini = new Scroller(context);
        heightMini = new Scroller(context);

        //wColor = new WindowColor(context);

        wmlp = new WindowManager.LayoutParams();
        wmlp.type = (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                ?WindowManager.LayoutParams.TYPE_PHONE
                :WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//類型
        wmlp.format = PixelFormat.TRANSPARENT;//背景(透明)
        wmlp.flags = FOCUS_FLAGE;
        wmlp.gravity = Gravity.LEFT | Gravity.TOP;//設定重力(初始位置)
        wmlp.x = Top;//設定原點座標
        wmlp.y = Left;
        wmlp.width = WindowManager.LayoutParams.WRAP_CONTENT;//設定視窗大小
        wmlp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        winform = LayoutInflater.from(context).inflate(R.layout.window,null);
        ((WindowFrom)winform).setWindowStruct(this);
        winform.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {//當點擊視窗以外的地方時，必須配和WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH以及WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL一同使用
                    WindowStruct.this.unFocusWindow();
                    return true;
                }
                return false;
            }
        });
        /*winform.setOnTouchListener(new View.OnTouchListener() {
            View titleBar = winform.findViewById(R.id.title_bar),
                    microMaxButtonBackground = winform.findViewById(R.id.micro_max_button_background),
                    closeButtonBackground = winform.findViewById(R.id.close_button_background);
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
        wincon = (ViewGroup) winform.findViewById(R.id.wincon);

        top = Top;
        left = Left;
        width = Width;
        height = Height;
        MINI_SIZE = winform.findViewById(R.id.close_button).getLayoutParams().width;

        menu = winform.findViewById(R.id.menu);
        close_button = winform.findViewById(R.id.close_button);
        mini = winform.findViewById(R.id.mini);
        max = winform.findViewById(R.id.max);
        hide = winform.findViewById(R.id.hide);
        title = winform.findViewById(R.id.title);
        sizeBar = winform.findViewById(R.id.size);
        titleBarAndButtons = winform.findViewById(R.id.title_bar_and_buttons);
        microMaxButtonBackground = winform.findViewById(R.id.micro_max_button_background);

        //-------------------------建立視窗內容畫面----------------------------------------------------
        wincon.addView(winconPage[0]);
        title.setText(windowPageTitles[0]);
        title.setOnTouchListener(this);
        title.setOnClickListener(this);//還原視窗大小
        close_button.setOnClickListener(this);
        winform.findViewById(R.id.size).setOnTouchListener(new View.OnTouchListener() {
            float W =- 1,H =- 1;
            @Override
            public boolean onTouch(View v, MotionEvent event) {//調整視窗大小
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (H == -1 || W == -1) {
                        W = event.getRawX();
                        H = event.getRawY();
                        return true;
                    }
                    setWidth(getWidth() - (int) (W - event.getRawX()));
                    setHeight(getHeight() - (int) (H - event.getRawY()));
                    W = event.getRawX();
                    H = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    W = -1;
                    H = -1;
                }
                return true;
            }
        });
        //-----------------------------------------------------------------------------
        //-------------------------建立Menu-------------------------------
        /*final PopupMenu pm = new PopupMenu(context,menu);
        Menu m = pm.getMenu();
        for(int i = 0;i<winconPage.length;i++)
            //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
            m.add(0,i,i,(String)winconPage[i].getTag());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            int ViweIndex = 0;
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                currentWindowPagePosition = item.getItemId();
                wincon.removeView(winconPage[ViweIndex]);
                wincon.addView(winconPage[item.getItemId()]);
                title.setText(windowTitle[item.getItemId()]);
                ViweIndex = item.getItemId();
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
        for(int i = 0;i<winconPage.length;i++)
            CDAW.Construction(context,winconPage[i],i,(windowInitArgs != null && i < windowInitArgs.length) ? windowInitArgs[i] : new Object[0],this);
        CDAW.onResume(context,winconPage[0],0,this);
        //---------------------------------------------------------------------------------------------
        //---------------------------隱藏不顯示的控制項物件------------------------------------------
        setDisplayObject();
        //-------------------------------------------------------------------------------------
        //---------------------------視窗開啟動畫------------------------------------------------------
        topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2 ,left - screenSize.getWidth() / 2, top - screenSize.getHeight() / 2, transitionsDuration);
        heightMini.startScroll(0, 0, width, height, transitionsDuration);
        runUi.post(new runTransitions(nowState));
        //---------------------------------------------------------------------------------------------
        this.parentWindowNumber = parentWindowNumber;
        if(parentWindowNumber != -1)
            windowList.get(parentWindowNumber).subWindowNumbers.add(Number);
    }

    public interface WindowAction{
        void goHide(WindowStruct windowStruct);//當按下隱藏視窗按鈕
        void goClose(WindowStruct windowStruct);//當按下關閉視窗按鈕
    }

    public interface constructionAndDeconstructionWindow{
        void Construction(Context context, View pageView, int position,Object[] args , WindowStruct windowStruct);
        void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct);

        void onResume(Context context, View pageView, int position, WindowStruct windowStruct);
        void onPause(Context context, View pageView, int position, WindowStruct windowStruct);
    }

    public static abstract class ScreenSize{
        public  abstract int getWidth();
        public  abstract int getHeight();

        protected Context context;

        public ScreenSize(Context context){
            this.context = context;
        }

        /**
             * 取得狀態列的高度
             * @return 狀態列的高度
             */
        protected int getStatusBarHeight() {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return  0;
        }

        /**
             * 取得導覽的高度
             * @return 狀態列的高度
             */
        protected int getNavigationBarHeight() {
            if(!hasNavigationBar())
                return 0;
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return 0;
        }

        /**
             * 判斷是否有導覽列(在虛擬機上會回傳false，來源：https://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically)
             * @return 是否有導覽列
             */
        protected boolean hasNavigationBar(){
            int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
            return id > 0 && context.getResources().getBoolean(id);
        }
    }
    public class menuList implements View.OnClickListener,AdapterView.OnItemClickListener,Runnable{
        private ListView menu;
        private LinearLayout menuListAndContext;
        private Scroller scroller = new Scroller(context);
        private boolean isOpen = false;
        public menuList(final String[] menuItems){
            menuListAndContext = (LinearLayout) winform.findViewById(R.id.menu_list_and_context);
            menu = (ListView) winform.findViewById(R.id.menu_list);
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
                    if(convertView == null){
                        convertView = LayoutInflater.from(context).inflate(R.layout.hide_menu_item,null,false);
                        TextView itenText = (TextView) convertView.findViewById(R.id.item_text);
                        itenText.setText(menuItems[position]);
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
                isOpen = true;
            }else {
                closeMenu();
                isOpen = false;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            isOpen = false;
            CDAW.onPause(context,winconPage[currentWindowPagePosition],currentWindowPagePosition,WindowStruct.this);
            wincon.removeView(winconPage[currentWindowPagePosition]);
            wincon.addView(winconPage[position]);
            CDAW.onResume(context,winconPage[position],position,WindowStruct.this);
            title.setText(windowTitle[position]);
            currentWindowPagePosition = position;
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
    private float H =- 1,W =- 1;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(nowState == State.GENERAL || nowState == State.MINI) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (H == -1 || W == -1) {
                    W = event.getRawX();
                    H = event.getRawY();
                    return true;
                }
                setPosition(getPositionX()-(int) (W-event.getRawX()),getPositionY()-(int) (H-event.getRawY()));
                W = event.getRawX();
                H = event.getRawY();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                W = -1;
                H= -1;
            }
    }
        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.mini) {//最小化
            mini();
        } else if (i == R.id.max) {//最大化或一般大小
            switch (nowState) {
                case MAX:
                    general();
                    break;
                default:
                    max();
            }
        } else if (i == R.id.title) {//還原視窗大小
            if (nowState == State.MINI) {
                if (previousState != null && previousState == State.MAX)
                    max();
                else
                    general();
            }
        } else if (i == R.id.close_button) {//關閉視窗
            close();
        } else if (i == R.id.hide) {//隱藏視窗
            hide();
        }
    }

    private class runTransitions implements Runnable{//執行轉場動畫
        private State state;

        public runTransitions(State state){
            this.state = state;
        }

        @Override
        public void run() {
            if (transitionsDuration == 0) {
                //abortAnimation可以使Scroller終止動畫跳過滾動過程直接給出最後數值
                topMini.abortAnimation();
                heightMini.abortAnimation();
                wmlp.x = topMini.getCurrX();
                wmlp.y = topMini.getCurrY();
                winform.getLayoutParams().width = heightMini.getCurrX();
                winform.getLayoutParams().height = heightMini.getCurrY();
                wm.updateViewLayout(winform, wmlp);
            }
            if (!topMini.isFinished() || !heightMini.isFinished()) {
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
            } else if (state == State.HIDE) {
                wmlp.alpha = 0.0f;
                wm.updateViewLayout(winform, wmlp);
            } else if (state == State.CLOSE) {
                for (int i = 0; i < winconPage.length; i++)
                    CDAW.Deconstruction(context, winconPage[i], i, WindowStruct.this);
                windowAction.goClose(WindowStruct.this);
                wm.removeView(winform);
                windowList.remove(Number);
            }
        }
    }

    /**
     * 視窗最小化
     */
    public void mini() {
        if (nowState != State.CLOSE && nowState != State.MINI) {
            previousState = nowState;
            nowState = State.MINI;
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            wmlp.flags = NO_FOCUS_FLAGE_FOR_MINI_STATE;
            wmlp.alpha = 1.0f;
            wm.updateViewLayout(winform, wmlp);
            if (previousState == State.MAX) {
                int dy;
                topMini.startScroll(0, 0, (screenSize.getWidth() - MINI_SIZE), 0, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(),
                        dy = screenSize.getHeight(),
                        MINI_SIZE - screenSize.getWidth(), -(dy - title.getLayoutParams().height), transitionsDuration);
            } else if (previousState == State.GENERAL) {
                topMini.startScroll(left, top, (screenSize.getWidth() - MINI_SIZE) - left, -top, transitionsDuration);
                heightMini.startScroll(width, height, MINI_SIZE - width, -(height - title.getLayoutParams().height), transitionsDuration);
            } else if (previousState == State.HIDE) {
                topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2,
                        screenSize.getWidth() / 2 - MINI_SIZE, -(screenSize.getHeight() / 2), transitionsDuration);
                heightMini.startScroll(0, 0, MINI_SIZE, title.getLayoutParams().height, transitionsDuration);
            }
            hideButtons();
            runUi.post(new runTransitions(nowState));
        }
    }

    /**
     * 視窗最大化
     */
    public void max(){
        if(nowState != State.CLOSE && nowState != State.MAX) {
            previousState = nowState;
            nowState = State.MAX;
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            wmlp.flags = FOCUS_FLAGE;
            wmlp.alpha =1.0f;
            wm.updateViewLayout(winform, wmlp);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                max.setBackground(context.getResources().getDrawable(R.drawable.mini_window));
            else
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mini_window));
            if (previousState == State.GENERAL) {
                topMini.startScroll(left, top, -left, -top, transitionsDuration);
                heightMini.startScroll(width, height, screenSize.getWidth() - width,
                        screenSize.getHeight() - height, transitionsDuration);
                if ((display_object & SIZE_BAR) == SIZE_BAR)
                    sizeBar.setVisibility(View.GONE);
            } else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, -wmlp.x, -wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                        , screenSize.getWidth() - winform.getLayoutParams().width,
                        screenSize.getHeight() - winform.getLayoutParams().height, transitionsDuration);
            } else if(previousState == State.HIDE){
                nowState = State.MAX;
                topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2 , -(screenSize.getWidth() / 2), -(screenSize.getHeight() / 2) , transitionsDuration);
                heightMini.startScroll(0,0, screenSize.getWidth(),screenSize.getHeight(), transitionsDuration);
            }
            recoveryButtons();
            sizeBar.setVisibility(View.GONE);
            runUi.post(new runTransitions(nowState));
        }
    }

    /**
     * 還原視窗大小
     */
    public void general(){
        if(nowState != State.CLOSE && nowState != State.GENERAL){
            previousState = nowState;
            nowState = State.GENERAL;
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            wmlp.flags = FOCUS_FLAGE;
            wmlp.alpha =1.0f;
            wm.updateViewLayout(winform, wmlp);
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                max.setBackground(context.getResources().getDrawable(R.drawable.max_window));
            else
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.max_window));
            if(previousState == State.MAX) {
                int dy;
                topMini.startScroll(0, 0, left, top, transitionsDuration);
                heightMini.startScroll( screenSize.getWidth(),
                        dy = screenSize.getHeight(),
                        width-screenSize.getWidth(), height-dy, transitionsDuration);
            }else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, left - wmlp.x, top - wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                        , width - winform.getLayoutParams().width,
                        height - winform.getLayoutParams().height, transitionsDuration);
            }else if(previousState == State.HIDE){
                topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2 ,left - screenSize.getWidth() / 2, top - screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(0, 0, width, height, transitionsDuration);
            }
            recoveryButtons();
            runUi.post(new runTransitions(nowState));
        }
    }

    /**
     * 視窗隱藏
     */
    public void hide(){
        if(nowState != State.CLOSE && nowState != State.HIDE) {
            previousState = nowState;
            nowState = State.HIDE;
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            wmlp.flags = NO_FOCUS_FLAGE;
            wm.updateViewLayout(winform, wmlp);
            if (previousState == State.MAX) {
                topMini.startScroll(0, 0, screenSize.getWidth() / 2, screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(), screenSize.getHeight()
                        , -screenSize.getWidth(), -(screenSize.getHeight()), transitionsDuration);
            } else if(previousState == State.GENERAL){
                topMini.startScroll(left, top, screenSize.getWidth() / 2 - left, screenSize.getHeight() / 2 - top, transitionsDuration);
                heightMini.startScroll(width, height,
                        -width, -height, transitionsDuration);
            }else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, screenSize.getWidth() / 2 - wmlp.x, screenSize.getHeight() / 2 - wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height,
                        -winform.getLayoutParams().width, -winform.getLayoutParams().height, transitionsDuration);
            }
            windowAction.goHide(this);
            runUi.post(new runTransitions(nowState));
        }
    }

    /**
     * 視窗關閉
     */
    public void close(){
        if(nowState != State.CLOSE) {
            previousState = nowState;
            nowState = State.CLOSE;
            if(parentWindowNumber != -1)
                windowList.get(this.parentWindowNumber).subWindowNumbers.remove(Number);
            for(int key : this.subWindowNumbers.toArray(new Integer[this.subWindowNumbers.size()]))
                if(windowList.containsKey(key))
                    windowList.get(key).close();
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            if (previousState == State.MAX) {
                topMini.startScroll(0, 0, screenSize.getWidth() / 2, screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(), screenSize.getHeight()
                        , -screenSize.getWidth(), -(screenSize.getHeight()), transitionsDuration);
            } else if(previousState == State.GENERAL){
                topMini.startScroll(left, top, screenSize.getWidth() / 2 - left, screenSize.getHeight() / 2 - top, transitionsDuration);
                heightMini.startScroll(width, height,
                        -width, -height, transitionsDuration);
            }else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, screenSize.getWidth() / 2 - wmlp.x, screenSize.getHeight() / 2 - wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height,
                        -winform.getLayoutParams().width, -winform.getLayoutParams().height, transitionsDuration);
            }
            runUi.post(new runTransitions(nowState));
        }
    }

    /**
     * 隱藏所有按鈕控制項
     */
    private void hideButtons(){
        if((display_object & MENU_BUTTON) == MENU_BUTTON)
            menu.setVisibility(View.GONE);
        else
            title.setPadding(0,0,0,0);
        close_button.setVisibility(View.GONE);
        microMaxButtonBackground.setVisibility(View.GONE);
        if((display_object & TITLE_BAR_AND_BUTTONS) != TITLE_BAR_AND_BUTTONS)
            titleBarAndButtons.setVisibility(View.VISIBLE);
        if((display_object & MINI_BUTTON) == MINI_BUTTON)
            mini.setVisibility(View.GONE);
        if((display_object & MAX_BUTTON) == MAX_BUTTON)
            max.setVisibility(View.GONE);
        if((display_object & HIDE_BUTTON) == HIDE_BUTTON)
            hide.setVisibility(View.GONE);
        if((display_object & CLOSE_BUTTON) == CLOSE_BUTTON)
            close_button.setVisibility(View.GONE);
        if((display_object & SIZE_BAR) == SIZE_BAR)
            sizeBar.setVisibility(View.GONE);
    }

    /**
     * 顯示所有按鈕控制項
     */
    private void recoveryButtons(){
        if((display_object & MENU_BUTTON) == MENU_BUTTON)
            menu.setVisibility(View.VISIBLE);
        else
            title.setPadding((int)(context.getResources().getDisplayMetrics().density*TITLE_LIFT_TO_EDGE_DISTANCE),0,0,0);
        close_button.setVisibility(View.VISIBLE);
        microMaxButtonBackground.setVisibility(View.VISIBLE);
        if((display_object & TITLE_BAR_AND_BUTTONS) == TITLE_BAR_AND_BUTTONS)
            titleBarAndButtons.setVisibility(View.VISIBLE);
        else
            titleBarAndButtons.setVisibility(View.GONE);
        if((display_object & MINI_BUTTON) == MINI_BUTTON)
            mini.setVisibility(View.VISIBLE);
        if((display_object & MAX_BUTTON) == MAX_BUTTON)
            max.setVisibility(View.VISIBLE);
        if((display_object & HIDE_BUTTON) == HIDE_BUTTON)
            hide.setVisibility(View.VISIBLE);
        if((display_object & CLOSE_BUTTON) == CLOSE_BUTTON)
            close_button.setVisibility(View.VISIBLE);
        if((display_object & SIZE_BAR) == SIZE_BAR)
            sizeBar.setVisibility(View.VISIBLE);
    }

    /**
     * 隱藏或顯示的控制項物件
     */
    private void setDisplayObject(){
        if(nowState == State.MINI)
            return;
        if((display_object & MENU_BUTTON) != MENU_BUTTON) {
            menu.setVisibility(View.GONE);
            title.setPadding((int)(context.getResources().getDisplayMetrics().density*TITLE_LIFT_TO_EDGE_DISTANCE),0,0,0);
        }else {
            menu.setVisibility(View.VISIBLE);
            title.setPadding(0,0,0,0);
        }
        if((display_object & TITLE_BAR_AND_BUTTONS) != TITLE_BAR_AND_BUTTONS)
            titleBarAndButtons.setVisibility(View.GONE);
        else
            titleBarAndButtons.setVisibility(View.VISIBLE);
        if((display_object & HIDE_BUTTON) != HIDE_BUTTON)
            hide.setVisibility(View.GONE);
        else
            hide.setVisibility(View.VISIBLE);
        if((display_object & MINI_BUTTON) != MINI_BUTTON)
            mini.setVisibility(View.GONE);
        else
            mini.setVisibility(View.VISIBLE);
        if((display_object & MAX_BUTTON) != MAX_BUTTON)
            max.setVisibility(View.GONE);
        else
            max.setVisibility(View.VISIBLE);
        if((display_object & CLOSE_BUTTON) != CLOSE_BUTTON)
            close_button.setVisibility(View.GONE);
        else
            close_button.setVisibility(View.VISIBLE);
        if((display_object & SIZE_BAR) != SIZE_BAR || nowState == State.MAX)
            sizeBar.setVisibility(View.GONE);
        else
            sizeBar.setVisibility(View.VISIBLE);
    }


    /**
     * 讓該視窗獲得焦點
     */
    public void focusWindow(){
        if(nowState != State.CLOSE && WindowStruct.NOW_FOCUS_NUMBER != this.Number) {//如果視窗編號不是現在焦點視窗編號
            if (windowList.containsKey(WindowStruct.NOW_FOCUS_NUMBER)) {//如果現在焦點視窗編號在有視窗清單裡
                WindowStruct WS = windowList.get(WindowStruct.NOW_FOCUS_NUMBER);
                WS.unFocusWindow();
            }
            WindowStruct.NOW_FOCUS_NUMBER = this.Number;
            ((WindowFrom) winform).setWindowStyleOfFocus();
            wmlp.flags = (nowState == State.MINI) ? NO_FOCUS_FLAGE_FOR_MINI_STATE : FOCUS_FLAGE;
            wmlp.alpha =1.0f;
            wm.removeView(winform);
            wm.addView(winform,wmlp);
        }
        for(int key : this.subWindowNumbers)
            windowList.get(key).focusWindow();
    }

    /**
     * 讓該視窗獲得焦點並顯示視窗
     */
    public void focusAndShowWindow(){
        focusWindow();
        if(nowState == State.MINI || nowState == State.HIDE) {
            if (previousState != null && previousState == State.MAX)
                max();
            else
                general();
        }
        for(int key : this.subWindowNumbers)
            windowList.get(key).focusAndShowWindow();
    }

    /**
     * 讓該視窗失去焦點
     */
    public void unFocusWindow(){
        if(nowState != State.CLOSE && WindowStruct.NOW_FOCUS_NUMBER == this.Number) {//如果視窗編號是現在焦點視窗編號
            WindowStruct.NOW_FOCUS_NUMBER = -1;
            if(nowState == State.MINI)
                wmlp.flags = NO_FOCUS_FLAGE_FOR_MINI_STATE;
            else {
                ((WindowFrom) winform).setWindowStyleOfUnFocus();
                wmlp.flags = NO_FOCUS_FLAGE;
            }
            wm.updateViewLayout(winform,wmlp);
        }
    }

    /**
     * 隱藏或顯示的控制項物件
     * @param display_object 要顯示的控制物件
     */
    public void setDisplayObject(int display_object){
        this.display_object = display_object;
        setDisplayObject();
    }

    /**
     * 取得的控制項物件
     * @return  顯示的控制物件
     */
    public int getDisplayObject(){
        return display_object;
    }


    /**
     * 設定視窗位置
     * @param x X座標
     * @param y Y座標
     */
    public void setPosition(int x,int y){
        if(nowState != State.CLOSE) {
            y = Math.max(y,0);
            if (nowState != State.MINI) {
                left = x;
                top = y;
            }
            if (nowState == State.GENERAL || nowState == State.MINI) {
                wmlp.x = x;
                wmlp.y = y;
                wm.updateViewLayout(winform, wmlp);
            }
        }
    }

    /**
     * 視窗X座標
     * @return X座標
     */
    public int getPositionX(){
        return wmlp.x;
    }

    /**
     * 視窗Y座標
     * @return Y座標
     */
    public int getPositionY(){
        return wmlp.y;
    }

    /**
     * 設定視窗寬度
     * @param width 寬度
     */
    public void setWidth(int width){
        if(nowState == State.GENERAL) {
            this.width = Math.max(width,30);
            winform.getLayoutParams().width = this.width;
            wm.updateViewLayout(winform, wmlp);
        }
    }

    /**
     * 取得視窗寬度
     * @return  寬度
     */
    public int getWidth(){
        return wmlp.width;
    }

    /**
     * 設定視窗高度
     * @param height 高度
     */
    public void setHeight(int height){
        if(nowState == State.GENERAL) {
            this.height = Math.max(height,winform.getHeight()-wincon.getHeight());
            winform.getLayoutParams().height = this.height;
            wm.updateViewLayout(winform, wmlp);
        }
    }

    /**
     * 取得視窗高度
     * @return  高度
     */
    public int getHeight(){
        return wmlp.height;
    }

    /**
     * 取得該視窗的所有子視窗
     * @return 所有子視窗
     */
    public ArrayList<WindowStruct> getSubWindow(){
        ArrayList<WindowStruct> subWindows = new ArrayList<>();
        for(int key : this.subWindowNumbers)
            subWindows.add(windowList.get(key));
        return subWindows;
    }

    /**
     * 設定視窗標題
     * @param position 表示要設定標題的子頁面編號
     * @param titleText 標題文字
     */
    public void setWindowTitle(int position, String titleText){
        windowTitle[position] = titleText;
        if(currentWindowPagePosition == position)
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
     * 設定過場動畫持續時間
     * @param transitionsDuration 過場動畫持續時間
     */
    public void setTransitionsDuration(int transitionsDuration){
        this.transitionsDuration = transitionsDuration;
    }

    /**
     * 取得過場動畫持續時間
     * @return  transitionsDuration 過場動畫持續時間
     */
    public int getTransitionsDuration(){
        return transitionsDuration;
    }

    /**
     * 取得視窗的View
     * @return 視窗的View
     */
    public WindowFrom getWindowFrom(){
        return (WindowFrom)winform;
    }

    /**
     * 取得視窗編號
     * @return 視窗編號
     */
    public int getNumber(){
        return Number;
    }

    /**
     * 取得視窗實體
     * @param number 視窗編號'
     * @return 視窗實體
     */
    public static WindowStruct getWindowStruct(int number){
        return windowList.get(number);
    }

    /**
     * 取得視窗內容實作
     * @return 視窗內容實作
     */
    public constructionAndDeconstructionWindow getConstructionAndDeconstructionWindow(){
        return CDAW;
    }
}