package com.example.jack8.floatwindow;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
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

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 浮動視窗服務
 */
public class FloatServer extends Service {
    WindowManager wm;
    Notification NF;
    final int NOTIFY_ID=851262;
    int wm_count=0;//計算FloatServer總共開了多少次
    Handler runUi= new Handler();
    ArrayList<WindowInfo> hideList=new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Intent toSetup=new Intent(this,Setup.class);
        Intent unHide=new Intent(this,FloatServer.class);
        unHide.putExtra("Layouts",new int[0]);
        NotificationCompat.Builder NFB = new NotificationCompat.Builder(this);
        NFB.setSmallIcon(R.drawable.mini_window).
                setContentTitle("浮動視窗").
                addAction(new NotificationCompat.Action.Builder(R.drawable.mini_window,"設定", PendingIntent.getActivity(this,0,toSetup,PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                addAction(new NotificationCompat.Action.Builder(R.drawable.mini_window,"被隱藏視窗清單", PendingIntent.getService(this,1,unHide,PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                setContentText("浮動視窗已啟用");
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN)
            NFB.setContentIntent(PendingIntent.getService(this,0,unHide,PendingIntent.FLAG_UPDATE_CURRENT));
        NF=NFB.build();
        startForeground(NOTIFY_ID,NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        Log.i("WMStrver","Create");
    }
    /*
    關於onStartCommand的說明
    http://www.cnblogs.com/not-code/archive/2011/05/21/2052713.html
     */
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        if(intent.getExtras().getIntArray("Layouts").length!=0)
            new WindowInfo(intent);
        else{
            //---------------------收起下拉選單-----------------------------
            try {
                Object statusBarManager = getSystemService("statusbar");
                Method collapse;

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    collapse = statusBarManager.getClass().getMethod("collapse");
                } else {
                    collapse = statusBarManager.getClass().getMethod("collapsePanels");
                }
                collapse.invoke(statusBarManager);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
            //-----------------------------------------------------------------------
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN){
                ListView Menu=new ListView(this);
                Menu.setAdapter(new ArrayAdapter<String>(FloatServer.this,android.R.layout.simple_selectable_list_item,new String[]{"設定","被隱藏視窗清單"}));
                final AlertDialog menu=new AlertDialog.Builder(this).setView(Menu).create();
                menu.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                menu.show();
                Menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        menu.dismiss();
                        switch (position){
                            case 0:
                                Intent intent=new Intent(FloatServer.this,Setup.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                            case 1:
                                showUnHideMenu();
                        }
                    }
                });
            }else
                showUnHideMenu();
        }

        return START_REDELIVER_INTENT;
    }
    void showUnHideMenu(){
        ListView hideMenu=new ListView(this);
        hideMenu.setAdapter(new hideMenuAdapter());
        final AlertDialog menu=new AlertDialog.Builder(this).setTitle("被隱藏視窗").setView(hideMenu).create();
        menu.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        menu.show();
        hideMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                menu.dismiss();
                hideList.remove(position).unzHide();
            }
        });
    }

    class hideMenuAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return hideList.size();
        }

        @Override
        public Object getItem(int position) {
            return hideList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=LayoutInflater.from(FloatServer.this).inflate(R.layout.hide_menu_item,parent,false);
                ((TextView)convertView.findViewById(R.id.item_text)).setText(hideList.get(position).title.getText());
            }
            return convertView;
        }
    }

    /**
     * 視窗結構
     */
    class WindowInfo implements View.OnClickListener,View.OnTouchListener,Runnable{
        final int MINI_SIZE;//視窗最小化的寬度
        static final int SECOND=500;//動畫持續時間
        static final int START_POINT=60;//視窗預設座標

        int top,left,height,width;//視窗的座標及大小

        WindowColor wColor;
        WindowManager.LayoutParams wmlp;
        View winform;//視窗外框
        ViewGroup wincon;//視窗內容框
        View[] winconPage;
        Scroller topMini=new Scroller(FloatServer.this),heightMini=new Scroller(FloatServer.this);
        Button menu,close_button,mini,max,hide;
        LinearLayout sizeBar;
        TextView title;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        boolean isMini=false;//是否最小化
        boolean isMax=false;//是否最大化
        boolean close=false;//是否是關閉視窗

        public WindowInfo(Intent intent){
            wColor=new WindowColor(FloatServer.this);

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
                View titleBar=winform.findViewById(R.id.title_bar),
                        microMaxButtonBackground=winform.findViewById(R.id.micro_max_button_background),
                        closeButtonBackground=winform.findViewById(R.id.close_button_background);
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Rect rect = new Rect();
                    v.getGlobalVisibleRect(rect);//取得視窗所在的範圍
                    if (!rect.contains((int) event.getX(), (int) event.getY())) {//當Touch的點在視窗範圍外
                        wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                        wm.updateViewLayout(winform, wmlp);
                        titleBar.setBackgroundColor(wColor.getWindowNotFoucs());
                        sizeBar.setBackgroundColor(wColor.getWindowNotFoucs());
                        microMaxButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
                        closeButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
                    }
                    return false;
                }
            });
            wm.addView(winform,wmlp);
            wincon=(ViewGroup) winform.findViewById(R.id.wincon);

            top=START_POINT;
            left=START_POINT;
            width=winform.getLayoutParams().width=(int)(displayMetrics.density*200);
            height=winform.getLayoutParams().height=(int)(displayMetrics.density*(240));
            MINI_SIZE=winform.findViewById(R.id.close_button).getLayoutParams().width;

            menu=(Button) winform.findViewById(R.id.menu);
            close_button=(Button) winform.findViewById(R.id.close_button);
            mini=(Button) winform.findViewById(R.id.mini);
            max=(Button) winform.findViewById(R.id.max);
            hide=(Button) winform.findViewById(R.id.hide);
            sizeBar=(LinearLayout) winform.findViewById(R.id.size);
            title=(TextView) winform.findViewById(R.id.title);

            //-------------------------建立視窗內容畫面----------------------------------------------------
            title=(TextView)winform.findViewById(R.id.title);
            int[] ids=intent.getExtras().getIntArray("Layouts");//頁面id
            String[] titles=intent.getExtras().getStringArray("Titles");//頁面標題
            winconPage=new View[ids.length];
            winconPage[0]=LayoutInflater.from(getApplicationContext()).inflate(ids[0],(ViewGroup) winform,false);//視窗內容實例
            wincon.addView(winconPage[0]);
            winconPage[0].setTag(titles[0]);
            title.setText(titles[0]);
            for(int i=1;i<winconPage.length;i++) {
                winconPage[i] = LayoutInflater.from(getApplicationContext()).inflate(ids[i], (ViewGroup) winform, false);
                winconPage[i].setTag(titles[i]);
            }
            title.setOnTouchListener(this);
            title.setOnClickListener(this);//還原視窗大小
        /*Log.i("formwidth",winform.getWidth()+"");
        Title.getLayoutParams().width=winform.getWidth()-160;*/
            close_button.setOnClickListener(this);
            winform.findViewById(R.id.size).setOnTouchListener(new View.OnTouchListener() {
                float Wlength=-1,Hlength=-1;
                @Override
                public boolean onTouch(View v, MotionEvent event) {//調整視窗大小
                    wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
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
                    wm.updateViewLayout(winform, wmlp);
                    return true;
                }
            });
            //-----------------------------------------------------------------------------
            //-------------------------建立Menu-------------------------------
            final PopupMenu pm=new PopupMenu(getApplicationContext(),menu);
            Menu m=pm.getMenu();
            for(int i=0;i<winconPage.length;i++)
                //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
                m.add(0,i,i,(String)winconPage[i].getTag());
            pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                int ViweIndex=0;
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    wincon.removeView(winconPage[ViweIndex]);
                    wincon.addView(winconPage[item.getItemId()]);
                    title.setText((String)winconPage[item.getItemId()].getTag());
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
            //------------------------------------------------------------------
            //---------------------------縮到最小與最大按鈕---------------------------
            hide.setOnClickListener(this);
            mini.setOnClickListener(this);
            max.setOnClickListener(this);
            //------------------------------------------------------------------
            //---------------------------初始化視窗內容-------------------------------
            for(int i=0;i<winconPage.length;i++)
                initWindow.init(FloatServer.this,winconPage[i],i,this,wm);
            //---------------------------------------------------------------------------------------------
            //---------------------------視窗開啟動畫------------------------------------------------------
            topMini.startScroll(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2 ,left - displayMetrics.widthPixels / 2, top - displayMetrics.heightPixels / 2, SECOND);
            heightMini.startScroll(0, 0, width, height, SECOND);
            runUi.post(this);
            //---------------------------------------------------------------------------------------------
        }

        /*
        移動視窗
        關於getX,getY等等的含數用法:
        http://blog.csdn.net/u013872857/article/details/53750682
         */
        float H=-1,W=-1;
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
                if(!isMini&&!isMax){
                    left=wmlp.x-=close_button.getLayoutParams().width;
                    top=wmlp.y;
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                if(isMini)
                    wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                H=-1;
                W=-1;
            }
            wm.updateViewLayout(winform, wmlp);
            return false;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mini://最小化
                    if (!isMini) {
                        isMini = true;
                        if(!isMax) {
                            topMini.startScroll(left, top, (displayMetrics.widthPixels - MINI_SIZE) - left, -top, SECOND);
                            heightMini.startScroll(width, height, MINI_SIZE - width, -(height-title.getLayoutParams().height), SECOND);
                        }else{
                            int dy;
                            topMini.startScroll(0, 0, (displayMetrics.widthPixels - MINI_SIZE),0, SECOND);
                            heightMini.startScroll( displayMetrics.widthPixels,
                                    dy=displayMetrics.heightPixels - getStatusBarHeight(),
                                    MINI_SIZE -displayMetrics.widthPixels, -(dy-title.getLayoutParams().height),SECOND);
                        }
                        wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                        menu.setVisibility(View.GONE);
                        close_button.setVisibility(View.GONE);
                        mini.setVisibility(View.GONE);
                        max.setVisibility(View.GONE);
                        hide.setVisibility(View.GONE);
                        sizeBar.setVisibility(View.GONE);
                    }
                    break;
                case R.id.max: //最大化
                    if(!isMax) {
                        isMax=true;
                        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            max.setBackground(getResources().getDrawable(R.drawable.mini_window));
                        else
                            max.setBackgroundDrawable(getResources().getDrawable(R.drawable.mini_window));
                        topMini.startScroll(left, top, -left, -top, SECOND);
                        heightMini.startScroll(width, height, displayMetrics.widthPixels - width,
                                displayMetrics.heightPixels - height - getStatusBarHeight(), SECOND);
                        sizeBar.setVisibility(View.GONE);
                    }else{
                        isMax=false;
                        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            max.setBackground(getResources().getDrawable(R.drawable.max_window));
                        else
                            max.setBackgroundDrawable(getResources().getDrawable(R.drawable.max_window));
                        int dy;
                        topMini.startScroll(0, 0, left, top, SECOND);
                        heightMini.startScroll( displayMetrics.widthPixels,
                                dy=displayMetrics.heightPixels - getStatusBarHeight(),
                                width-displayMetrics.widthPixels, height-dy,SECOND);
                        sizeBar.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.title://還原視窗大小
                    if(isMini){
                        isMini = false;
                        if(!isMax) {
                            topMini.startScroll(wmlp.x, wmlp.y, left - wmlp.x, top - wmlp.y, SECOND);
                            heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                                    , width - winform.getLayoutParams().width,
                                    height - winform.getLayoutParams().height, SECOND);
                            sizeBar.setVisibility(View.VISIBLE);
                        }else{
                            topMini.startScroll(wmlp.x, wmlp.y, -wmlp.x, -wmlp.y, SECOND);
                            heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                                    , displayMetrics.widthPixels - winform.getLayoutParams().width,
                                    displayMetrics.heightPixels - getStatusBarHeight(), SECOND);
                        }
                        wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;//讓視窗聚焦
                        menu.setVisibility(View.VISIBLE);
                        close_button.setVisibility(View.VISIBLE);
                        mini.setVisibility(View.VISIBLE);
                        max.setVisibility(View.VISIBLE);
                        hide.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.close_button://關閉視窗
                case R.id.hide:
                    if (v.getId()==R.id.close_button)
                        close=true;
                    else {
                        wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                        hideList.add(this);
                    }
                    if(!isMax) {
                        topMini.startScroll(left, top, displayMetrics.widthPixels / 2 - left, displayMetrics.heightPixels / 2 - top, SECOND);
                        heightMini.startScroll(width, height,
                                - width, -height, SECOND);
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
                    winform.getLayoutParams().width = heightMini.getCurrX();
                    winform.getLayoutParams().height = heightMini.getCurrY();
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

        public void unzHide(){
            wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;//讓視窗聚焦
            if(!isMax) {
                topMini.startScroll(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2 ,left - displayMetrics.widthPixels / 2, top - displayMetrics.heightPixels / 2, SECOND);
                heightMini.startScroll(0, 0, width, height, SECOND);
            }else{
                topMini.startScroll(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2 , -(displayMetrics.widthPixels / 2), -(displayMetrics.heightPixels / 2) , SECOND);
                heightMini.startScroll(0,0, displayMetrics.widthPixels,displayMetrics.heightPixels - getStatusBarHeight(), SECOND);
            }
            runUi.post(this);
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

