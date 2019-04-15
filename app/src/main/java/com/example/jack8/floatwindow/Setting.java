package com.example.jack8.floatwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.jack8.floatwindow.Window.WindowColor;
import com.example.jack8.floatwindow.Window.WindowFrom;
import com.example.jack8.floatwindow.Window.WindowStruct;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.util.ArrayList;

public class Setting extends AppCompatActivity {
    private AdView mAdView;

    WindowColor wColor;
    ViewGroup windowsBackground,titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground;
    ViewGroup windowsBackgroundNotFoucs,titleBarNotFoucs,sizeBarNotFoucs,microMaxButtonBackgroundNotFoucs,closeButtonBackgroundNotFoucs;
    SeekBar secondSet;
    ArrayList<WindowStruct> windowList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        MobileAds.initialize(this, "ca-app-pub-4604853118314154~2222092369");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                .build();
        mAdView.loadAd(adRequest);

        wColor=new WindowColor(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(wColor.getTitleBar()));//設定標題列顏色
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)//判斷作業系統是否大於等於Android 5.0
            getWindow().setStatusBarColor(wColor.getTitleBar());//設定通知列顏色
        setTitle(getString(R.string.float_window_setting));

        secondSet=(SeekBar) findViewById(R.id.secondSet);
        secondSet.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView secondText=(TextView) findViewById(R.id.second);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                secondText.setText(String.format("%.2f",progress/1000f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        secondSet.setProgress(WindowTransitionsDuration.getWindowTransitionsDuration(this));

        ViewGroup content = (ViewGroup)findViewById(R.id.content);
        View FoucsWindow,NotFoucsWindow;
        TextView windowColorSetTitle=new TextView(this);
        windowColorSetTitle.setText(getString(R.string.window_color));
        windowColorSetTitle.setTextSize(20f);
        content.addView(windowColorSetTitle);
        content.addView(FoucsWindow=LayoutInflater.from(this).inflate(R.layout.window,null));
        TextView windowNotFoucsColorNotFoucsSetTitle=new TextView(this);
        windowNotFoucsColorNotFoucsSetTitle.setText(getString(R.string.window_out_of_focus_color));
        windowNotFoucsColorNotFoucsSetTitle.setTextSize(20f);
        content.addView(windowNotFoucsColorNotFoucsSetTitle);
        content.addView(NotFoucsWindow=LayoutInflater.from(this).inflate(R.layout.window,null));

        //-------------------------初始化一般視窗設定畫面----------------------------
        TextView prompt=new TextView(this);
        prompt.setText(getString(R.string.select_window_color));
        prompt.setTextSize(15f);
        ((ViewGroup) FoucsWindow.findViewById(R.id.wincon)).addView(prompt);

        windowsBackground=(ViewGroup) FoucsWindow.findViewById(R.id.menu_list_and_context);
        //windowsBackground.getLayoutParams().width=(int)(getResources().getDisplayMetrics().density*200);
        windowsBackground.getLayoutParams().height=(int)(getResources().getDisplayMetrics().density*240);
        windowsBackground.setOnClickListener(setColor);
        titleBar=(ViewGroup) FoucsWindow.findViewById(R.id.title_bar);
        titleBar.setOnClickListener(setColor);
        sizeBar=(ViewGroup) FoucsWindow.findViewById(R.id.size);
        sizeBar.setOnClickListener(setColor);
        microMaxButtonBackground=(ViewGroup) FoucsWindow.findViewById(R.id.micro_max_button_background);
        FoucsWindow.findViewById(R.id.hide).setOnClickListener(setColor);
        FoucsWindow.findViewById(R.id.mini).setOnClickListener(setColor);
        FoucsWindow.findViewById(R.id.max).setOnClickListener(setColor);
        FoucsWindow.findViewById(R.id.menu).setOnClickListener(setColor);
        FoucsWindow.findViewById(R.id.close_button).setOnClickListener(setColor);
        closeButtonBackground=(ViewGroup) FoucsWindow.findViewById(R.id.close_button_background);
        ((TextView)FoucsWindow.findViewById(R.id.title)).setText(getString(R.string.window_title));

        microMaxButtonBackground.setBackgroundColor(wColor.getMicroMaxButtonBackground());
        titleBar.setBackgroundColor(wColor.getTitleBar());
        sizeBar.setBackgroundColor(wColor.getSizeBar());
        closeButtonBackground.setBackgroundColor(wColor.getCloseButtonBackground());
        //---------------------------------------------------------------------------
        //-------------------------初始化失焦視窗設定畫面----------------------------
        TextView promptNotFoucs=new TextView(this);
        promptNotFoucs.setText(getString(R.string.select_window_out_of_focus_color));
        promptNotFoucs.setTextSize(15f);
        ((ViewGroup) NotFoucsWindow.findViewById(R.id.wincon)).addView(promptNotFoucs);

        windowsBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.menu_list_and_context);
        //windowsBackgroundNotFoucs.getLayoutParams().width=(int)(getResources().getDisplayMetrics().density*200);
        windowsBackgroundNotFoucs.getLayoutParams().height=(int)(getResources().getDisplayMetrics().density*240);
        windowsBackgroundNotFoucs.setOnClickListener(setColor);
        titleBarNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.title_bar);
        titleBarNotFoucs.setOnClickListener(setColorForNotFoucs);
        sizeBarNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.size);
        sizeBarNotFoucs.setOnClickListener(setColorForNotFoucs);
        microMaxButtonBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.micro_max_button_background);
        NotFoucsWindow.findViewById(R.id.hide).setOnClickListener(setColorForNotFoucs);
        NotFoucsWindow.findViewById(R.id.mini).setOnClickListener(setColorForNotFoucs);
        NotFoucsWindow.findViewById(R.id.max).setOnClickListener(setColorForNotFoucs);
        NotFoucsWindow.findViewById(R.id.menu).setOnClickListener(setColorForNotFoucs);
        NotFoucsWindow.findViewById(R.id.close_button).setOnClickListener(setColorForNotFoucs);
        closeButtonBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.close_button_background);
        ((TextView)NotFoucsWindow.findViewById(R.id.title)).setText(getString(R.string.window_title));

        microMaxButtonBackgroundNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        titleBarNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        sizeBarNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        closeButtonBackgroundNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        //---------------------------------------------------------------------------

        ((Button)findViewById(R.id.ok)).setOnClickListener(save);
        ((Button)findViewById(R.id.no)).setOnClickListener(save);
    }
    View.OnClickListener setColor=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorPickerDialog dialog=null;
            switch (v.getId()){
                case R.id.title_bar:
                case R.id.size:
                case R.id.menu:
                    dialog = new ColorPickerDialog(Setting.this, wColor.getTitleBar());
                    dialog.setTitle(getString(R.string.select_color));
                    dialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int color) {
                            wColor.setTitleBar(color);
                            wColor.setSizeBar(color);
                            titleBar.setBackgroundColor(color);
                            sizeBar.setBackgroundColor(color);
                            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(wColor.getTitleBar()));//設定標題列顏色
                            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)//判斷作業系統是否大於等於Android 5.0
                                getWindow().setStatusBarColor(wColor.getTitleBar());//設定通知列顏色
                        }
                    });
                    break;
                case R.id.hide:
                case R.id.max:
                case R.id.mini:
                    dialog = new ColorPickerDialog(Setting.this, wColor.getMicroMaxButtonBackground());
                    dialog.setTitle(getString(R.string.select_color));
                    dialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int color) {
                            wColor.setMicroMaxButtonBackground(color);
                            microMaxButtonBackground.setBackgroundColor(color);
                        }
                    });
                    break;
                case R.id.close_button:
                    dialog = new ColorPickerDialog(Setting.this, wColor.getCloseButtonBackground());
                    dialog.setTitle(getString(R.string.select_color));
                    dialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int color) {
                            wColor.setCloseButtonBackground(color);
                            closeButtonBackground.setBackgroundColor(color);
                        }
                    });
                    break;
                case R.id.menu_list_and_context:
                    dialog = new ColorPickerDialog(Setting.this, wColor.getWindowBackground());
                    dialog.setTitle(getString(R.string.select_color));
                    dialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int color) {
                            wColor.setWindowBackground(color);
                            windowsBackground.setBackgroundColor(color);
                            windowsBackgroundNotFoucs.setBackgroundColor(color);
                        }
                    });
                    break;
            }
            dialog.setAlphaSliderVisible(true);
            dialog.show();
        }
    };
    View.OnClickListener setColorForNotFoucs=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorPickerDialog dialog = new ColorPickerDialog(Setting.this, wColor.getWindowNotFoucs());
            dialog.setTitle(getString(R.string.select_color));
            dialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                @Override
                public void onColorChanged(int color) {
                    wColor.setWindowNotFoucs(color);
                    microMaxButtonBackgroundNotFoucs.setBackgroundColor(color);
                    titleBarNotFoucs.setBackgroundColor(color);
                    sizeBarNotFoucs.setBackgroundColor(color);
                    closeButtonBackgroundNotFoucs.setBackgroundColor(color);
                }
            });
            dialog.setAlphaSliderVisible(true);
            dialog.show();
        }
    };
    View.OnClickListener save=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.ok) {
                WindowTransitionsDuration.setWindowTransitionsDuration(Setting.this,secondSet.getProgress());
                wColor.save();
            }
            finish();
        }
    };
    @Override
    protected void onDestroy(){
        super.onDestroy();
        for(WindowStruct windowStruct:windowList) {
            windowStruct.setTransitionsDuration(0);
            windowStruct.close();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,getString(R.string.about));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 0:
                windowList.add(new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE))
                        .windowPages(new int[]{R.layout.about})
                        .windowPageTitles(new String[]{getString(R.string.about)})
                        .top(60)
                        .left(60)
                        .height((int)(110*this.getResources().getDisplayMetrics().density))
                        .width((int)(195*this.getResources().getDisplayMetrics().density))
                        .displayObject(WindowStruct.MINI_BUTTON)
                        .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(this))
                        .windowAction(new WindowStruct.WindowAction() {
                            @Override
                            public void goHide(WindowStruct windowStruct) {

                            }

                            @Override
                            public void goClose(WindowStruct windowStruct) {
                                windowList.remove(windowStruct);
                            }
                        })
                        .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                            @Override
                            public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {

                            }

                            @Override
                            public void Deconstruction(Context context, View pageView, int position) {

                            }
                        }).show());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}