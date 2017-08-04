package com.example.jack8.floatwindow;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * 浮動視窗服務
 */
public class FloatServer extends Service {
    WindowManager wm;
    Notification NF;
    final int NOTIFY_ID=851262;
    final int SECOND=500;//動畫持續時間
    static final int START_POINT=60;//視窗預設座標
    int wm_count=0;//計算FloatServer總共開了多少次
    Handler runUi= new Handler();
    @Override
    public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        NF=new Notification.Builder(getApplicationContext()).
                setSmallIcon(R.drawable.menu_icom).
                setContentTitle("浮動視窗").
                setContentText("浮動視窗已啟用").build();
        startForeground(NOTIFY_ID,NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        Log.i("WMStrver","Create");
    }
    /*
    關於onStartCommand的說明
    http://www.cnblogs.com/not-code/archive/2011/05/21/2052713.html
     */
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        final WindowManager.LayoutParams wmlp;
        final View winform;//視窗外框
        final ViewGroup _wincon;
        final View[] wincon;
        final TextView Title;
        final WindowInfo windowInfo;

        wm_count++;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        wmlp = new WindowManager.LayoutParams();
        wmlp.type= WindowManager.LayoutParams.TYPE_PHONE;//類型
        wmlp.format = PixelFormat.RGBA_8888;//背景(透明)
        //wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//設定焦點(不聚交)，否則背後介面將不可操作，因為會有一層透明的圖層
        wmlp.gravity = Gravity.LEFT | Gravity.TOP;//設定重力(初始位置)
        wmlp.x=START_POINT;//設定原點座標
        wmlp.y=START_POINT;
        wmlp.width = WindowManager.LayoutParams.WRAP_CONTENT;//設定視窗大小
        wmlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        winform= LayoutInflater.from(getApplicationContext()).inflate(R.layout.window,null);
        ((WindowFrom)winform).setLayoutParams(wmlp);
        winform.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect rect = new Rect();
                v.getGlobalVisibleRect(rect);//取得視窗所在的範圍
                if (!rect.contains((int) event.getX(), (int) event.getY())) {//當Touch的點在視窗範圍外
                    wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                    wm.updateViewLayout(winform, wmlp);
                }
                return false;
            }
        });
        wm.addView(winform,wmlp);
        _wincon=(ViewGroup) winform.findViewById(R.id.wincon);
        windowInfo=new WindowInfo(wmlp,winform,_wincon,START_POINT,START_POINT,_wincon.getLayoutParams().width,_wincon.getLayoutParams().height);
        //-------------------------建立視窗內容畫面----------------------------------------------------
        Title=(TextView)winform.findViewById(R.id.title);
        int[] ids=intent.getExtras().getIntArray("Layouts");//頁面id
        String[] titles=intent.getExtras().getStringArray("Titles");//頁面標題
        wincon=new View[ids.length];
        wincon[0]=LayoutInflater.from(getApplicationContext()).inflate(ids[0],(ViewGroup) winform,false);//視窗內容實例
        _wincon.addView(wincon[0]);
        wincon[0].setTag(titles[0]);
        Title.setText(titles[0]);
        for(int i=1;i<wincon.length;i++) {
            wincon[i] = LayoutInflater.from(getApplicationContext()).inflate(ids[i], (ViewGroup) winform, false);
            wincon[i].setTag(titles[i]);
        }
        Title.setOnTouchListener(new MoveWindow(wmlp,windowInfo,winform));
        Title.setOnClickListener(windowInfo);//還原視窗大小
        /*Log.i("formwidth",winform.getWidth()+"");
        Title.getLayoutParams().width=winform.getWidth()-160;*/
        ((Button)winform.findViewById(R.id.close_button)).setOnClickListener(windowInfo);
        winform.findViewById(R.id.size).setOnTouchListener(new View.OnTouchListener() {
            float Wlength=-1,Hlength=-1;
            @Override
            public boolean onTouch(View v, MotionEvent event) {//調整視窗大小
                wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    if(Wlength==-1||Hlength==-1){
                        Wlength=_wincon.getWidth()-event.getX();//取得點擊X點到視窗最右側的距離
                        Hlength=(event.getRawY()-wmlp.y)-_wincon.getHeight();//取得視窗標題列高度以及視窗大小調整列被點擊以上的高度，視窗內部框架高度-(點擊座標Y-視窗Top座標)
                        return true;
                    }
                    Log.i("size",(int)(event.getX()+Wlength)+"   "+(int)(event.getRawY()-wmlp.y+Hlength));
                    int temp;
                    _wincon.getLayoutParams().width=windowInfo.width=(temp=(int)(event.getX()+Wlength))>30?temp:30;
                    _wincon.getLayoutParams().height=windowInfo.height=(temp=(int)(event.getRawY()-wmlp.y-Hlength))>=0?temp:0;//Touch的Y減去視窗的Top再減去Hlength就是視窗內容區要調整的高度
                    Log.i("size2",_wincon.getLayoutParams().width+"   "+_wincon.getLayoutParams().height);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    Wlength=-1;
                    Hlength=-1;
                }
                wm.updateViewLayout(winform, wmlp);
                return true;
            }
        });
        //-----------------------------------------------------------------------------
        //-------------------------建立Menu-------------------------------
        final Button menu=(Button)winform.findViewById(R.id.menu);
        final PopupMenu pm=new PopupMenu(getApplicationContext(),menu);
        Menu m=pm.getMenu();
        for(int i=0;i<wincon.length;i++)
            //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
            m.add(0,i,i,(String)wincon[i].getTag());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            int ViweIndex=0;
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                _wincon.removeView(wincon[ViweIndex]);
                _wincon.addView(wincon[item.getItemId()]);
                Title.setText((String)wincon[item.getItemId()].getTag());
                ViweIndex=item.getItemId();
                return true;
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm.show();
            }
        });
        //Title.getLayoutParams().width=_wincon.getLayoutParams().width-(menu.getLayoutParams().width*3);
        //------------------------------------------------------------------
        //---------------------------縮到最小與最大按鈕---------------------------
        ((Button)winform.findViewById(R.id.mini)).setOnClickListener(windowInfo);
        ((Button)winform.findViewById(R.id.max)).setOnClickListener(windowInfo);
        //------------------------------------------------------------------
        //---------------------------初始化視窗內容-------------------------------
        for(int i=0;i<wincon.length;i++)
            initWindow.init(wincon[i],i,winform,wm,wmlp);
        //---------------------------------------------------------------------------------------------
        //---------------------------視窗開啟動畫------------------------------------------------------
        final Scroller topMini=new Scroller(FloatServer.this),heightMini=new Scroller(FloatServer.this);
        topMini.startScroll(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2 ,START_POINT - displayMetrics.widthPixels / 2, START_POINT - displayMetrics.heightPixels / 2, SECOND);
        heightMini.startScroll(0, 0, winform.getLayoutParams().width, winform.getLayoutParams().height, SECOND);
        runUi.post(new Runnable() {
            @Override
            public void run() {
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
                }
            }
        });
        //---------------------------------------------------------------------------------------------

        return START_REDELIVER_INTENT;
    }

    /**
     * 視窗資訊
     */
    class WindowInfo implements View.OnClickListener,Runnable{
         int top,left,height,width;//視窗的座標及大小
        WindowManager.LayoutParams wmlp;
        View winform;//視窗外框
        ViewGroup wincon;//視窗內容框
        boolean isMini=false;//是否最小化
        boolean isMax=false;//是否最大化
        boolean close=false;//是否是關閉視窗
        Scroller topMini=new Scroller(FloatServer.this),heightMini=new Scroller(FloatServer.this);
        final int MINI_SIZE;//視窗最小化的寬度
        Button menu,close_button,mini,max;
        LinearLayout size;
        TextView title;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        public WindowInfo(WindowManager.LayoutParams wmlp,View winform,ViewGroup wincon,int top,int left,int width,int height){
            this.wmlp=wmlp;
            this.winform=winform;
            this.wincon=wincon;
            this.top=top;
            this.left=left;
            this.height=height;
            this.width=width;
            MINI_SIZE=winform.findViewById(R.id.close_button).getLayoutParams().width;

            menu=(Button) winform.findViewById(R.id.menu);
            close_button=(Button) winform.findViewById(R.id.close_button);
            mini=(Button) winform.findViewById(R.id.mini);
            max=(Button) winform.findViewById(R.id.max);
            size=(LinearLayout) winform.findViewById(R.id.size);
            title=(TextView) winform.findViewById(R.id.title);
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mini://最小化
                    if (!isMini) {
                        isMini = true;
                        if(!isMax) {
                            topMini.startScroll(left, top, (displayMetrics.widthPixels - MINI_SIZE) - left, -top, SECOND);
                            heightMini.startScroll(width, height, MINI_SIZE - width, -height, SECOND);
                        }else{
                            int dy;
                            topMini.startScroll(0, 0, (displayMetrics.widthPixels - MINI_SIZE),0, SECOND);
                            heightMini.startScroll( displayMetrics.widthPixels,
                                    dy=displayMetrics.heightPixels -
                                            title.getLayoutParams().height - getStatusBarHeight(),
                                    MINI_SIZE -displayMetrics.widthPixels, -dy,SECOND);
                        }
                        wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                        menu.setVisibility(View.GONE);
                        close_button.setVisibility(View.GONE);
                        mini.setVisibility(View.GONE);
                        max.setVisibility(View.GONE);
                        size.setVisibility(View.GONE);
                    }
                    break;
                case R.id.max: //最大化
                    if(!isMax) {
                        isMax=true;
                        topMini.startScroll(left, top, -left, -top, SECOND);
                        heightMini.startScroll(width, height, displayMetrics.widthPixels - width,
                                displayMetrics.heightPixels -
                                        title.getLayoutParams().height - height - getStatusBarHeight(), SECOND);
                        size.setVisibility(View.GONE);
                    }else{
                        isMax=false;
                        int dy;
                        topMini.startScroll(0, 0, left, top, SECOND);
                        heightMini.startScroll( displayMetrics.widthPixels,
                                dy=displayMetrics.heightPixels -
                                        title.getLayoutParams().height - getStatusBarHeight(),
                                width-displayMetrics.widthPixels, height-dy,SECOND);
                        size.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.title://還原視窗大小
                    if(isMini){
                        isMini = false;
                        if(!isMax) {
                            topMini.startScroll(wmlp.x, wmlp.y, left - wmlp.x, top - wmlp.y, SECOND);
                            heightMini.startScroll(wincon.getLayoutParams().width, wincon.getLayoutParams().height
                                    , width - wincon.getLayoutParams().width, height - wincon.getLayoutParams().height, SECOND);
                            size.setVisibility(View.VISIBLE);
                        }else{
                            topMini.startScroll(wmlp.x, wmlp.y, -wmlp.x, -wmlp.y, SECOND);
                            heightMini.startScroll(0, 0, displayMetrics.widthPixels,
                                    displayMetrics.heightPixels -
                                            title.getLayoutParams().height - getStatusBarHeight(), SECOND);
                        }
                        wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;//讓視窗聚焦
                        menu.setVisibility(View.VISIBLE);
                        close_button.setVisibility(View.VISIBLE);
                        mini.setVisibility(View.VISIBLE);
                        max.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.close_button://關閉視窗
                    close=true;
                    if(!isMax) {
                        int dy;
                        topMini.startScroll(left, top, displayMetrics.widthPixels / 2 - left, displayMetrics.heightPixels / 2 - top, SECOND);
                        heightMini.startScroll(width, dy=height+title.getLayoutParams().height+size.getLayoutParams().height,
                                - width, -dy, SECOND);
                    }else{
                        topMini.startScroll(0, 0, displayMetrics.widthPixels / 2 , displayMetrics.heightPixels / 2 , SECOND);
                        heightMini.startScroll(displayMetrics.widthPixels, displayMetrics.heightPixels - getStatusBarHeight()
                                , - displayMetrics.widthPixels, -(displayMetrics.heightPixels - getStatusBarHeight()), SECOND);
                    }
            }
            runUi.post(this);
        }

        @Override
        public void run() {//放大縮小動畫
            if(!topMini.isFinished()||!heightMini.isFinished()) {
                if (topMini.computeScrollOffset()) {
                    wmlp.x = topMini.getCurrX();
                    wmlp.y = topMini.getCurrY();
                }
                if (heightMini.computeScrollOffset()) {
                    if(!close) {
                        wincon.getLayoutParams().width = heightMini.getCurrX();
                        wincon.getLayoutParams().height = heightMini.getCurrY();
                    }else{
                        winform.getLayoutParams().width = heightMini.getCurrX();
                        winform.getLayoutParams().height = heightMini.getCurrY();
                    }
                }
                wm.updateViewLayout(winform, wmlp);
                runUi.post(this);
            }else if(close){
                wm.removeView(winform);
                if(--wm_count==0) {
                    FloatServer.this.stopForeground(true);
                    stopSelf();
                }
            }
        }
    }
    class MoveWindow implements View.OnTouchListener{
        WindowManager.LayoutParams wmlp;
        WindowInfo windowInfo;
        View winform;
        float H=-1,W=-1;
        Button close_button;
        public MoveWindow(WindowManager.LayoutParams wmlp,WindowInfo windowInfo,View winform){
            this.wmlp=wmlp;
            this.windowInfo=windowInfo;
            this.winform=winform;

            close_button=(Button) winform.findViewById(R.id.close_button);
        }
        /*
        移動視窗
        關於getX,getY等等的含數用法:
        http://blog.csdn.net/u013872857/article/details/53750682
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_MOVE) {
                if(H==-1||W==-1){
                    H=event.getX();//取得點擊的X座標到視窗頂點的距離
                    W=event.getY();//取得點擊的Y座標到視窗頂點的距離
                    return true;
                }
                wmlp.x = (int) (event.getRawX()-H);
                wmlp.y = (int) (event.getRawY()-W-getStatusBarHeight());//60為狀態列高度
                if(!windowInfo.isMini&&!windowInfo.isMax){
                    windowInfo.left=wmlp.x-=close_button.getLayoutParams().width;
                    windowInfo.top=wmlp.y;
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                if(windowInfo.isMini)
                    wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                H=-1;
                W=-1;
            }
            wm.updateViewLayout(winform, wmlp);
            return false;
        }
    }
    private int getStatusBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

