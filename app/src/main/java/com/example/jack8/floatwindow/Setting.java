package com.example.jack8.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jack8.floatwindow.Window.WindowColor;
import com.jack8.floatwindow.Window.WindowStruct;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

public class Setting extends AppCompatActivity {
    private AdView mAdView;

    WindowColor wColor;
    ViewGroup windowsBackground,titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground,titleBarAndButtons;
    ViewGroup windowsBackgroundNotFoucs,titleBarNotFoucs,sizeBarNotFoucs,microMaxButtonBackgroundNotFoucs,closeButtonBackgroundNotFoucs,titleBarAndButtonsNotFoucs;
    Button menu,close,mini,max,hide;
    Button menuNotFoucs,closeNotFoucs,miniNotFoucs,maxNotFoucs,hideNotFoucs;
    TextView title, titleNotFoucs;
    SeekBar secondSet, buttonsHeight, buttonsWidth, sizeBarHeight;
    CheckBox isAutoRun, isPermanent;
    int adoutWindow = -1;

    Brickout brickout = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        MobileAds.initialize(this, getString(R.string.AD_ID));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
                .addTestDevice("F4734F4691C588DB93799277888EA573")
                .build();
        mAdView.loadAd(adRequest);

        wColor=new WindowColor(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(wColor.getTitleBar()));//設定標題列顏色
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)//判斷作業系統是否大於等於Android 5.0
            getWindow().setStatusBarColor(wColor.getTitleBar());//設定通知列顏色
        setTitle(getString(R.string.float_window_setting));

        View FoucsWindow = findViewById(R.id.focus_winodw), NotFoucsWindow = findViewById(R.id.unfocus_winodw);

        //-------------------------初始化一般視窗設定畫面----------------------------
        TextView prompt=new TextView(this);
        prompt.setText(getString(R.string.select_window_color));
        prompt.setTextSize(15f);
        ((ViewGroup) FoucsWindow.findViewById(R.id.wincon)).addView(prompt);

        windowsBackground=(ViewGroup) FoucsWindow.findViewById(R.id.menu_list_and_context);
        windowsBackground.setOnClickListener(setColor);
        titleBar=(ViewGroup) FoucsWindow.findViewById(R.id.title_bar);
        titleBar.setOnClickListener(setColor);
        sizeBar=(ViewGroup) FoucsWindow.findViewById(R.id.size);
        sizeBar.setOnClickListener(setColor);
        microMaxButtonBackground=(ViewGroup) FoucsWindow.findViewById(R.id.micro_max_button_background);
        hide = FoucsWindow.findViewById(R.id.hide);
        hide.setOnClickListener(setColor);
        mini = FoucsWindow.findViewById(R.id.mini);
        mini.setOnClickListener(setColor);
        max = FoucsWindow.findViewById(R.id.max);
        max.setOnClickListener(setColor);
        menu = FoucsWindow.findViewById(R.id.menu);
        menu.setOnClickListener(setColor);
        close = FoucsWindow.findViewById(R.id.close_button);
        close.setOnClickListener(setColor);
        closeButtonBackground=(ViewGroup) FoucsWindow.findViewById(R.id.close_button_background);
        title = ((TextView)FoucsWindow.findViewById(R.id.title));
        title.setText(getString(R.string.window_title));

        microMaxButtonBackground.setBackgroundColor(wColor.getMicroMaxButtonBackground());
        titleBar.setBackgroundColor(wColor.getTitleBar());
        sizeBar.setBackgroundColor(wColor.getSizeBar());
        closeButtonBackground.setBackgroundColor(wColor.getCloseButtonBackground());
        titleBarAndButtons = FoucsWindow.findViewById(R.id.title_bar_and_buttons);
        //---------------------------------------------------------------------------
        //-------------------------初始化失焦視窗設定畫面----------------------------
        TextView promptNotFoucs=new TextView(this);
        promptNotFoucs.setText(getString(R.string.select_window_out_of_focus_color));
        promptNotFoucs.setTextSize(15f);
        ((ViewGroup) NotFoucsWindow.findViewById(R.id.wincon)).addView(promptNotFoucs);

        windowsBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.menu_list_and_context);
        windowsBackgroundNotFoucs.setOnClickListener(setColor);
        titleBarNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.title_bar);
        titleBarNotFoucs.setOnClickListener(setColorForNotFoucs);
        sizeBarNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.size);
        sizeBarNotFoucs.setOnClickListener(setColorForNotFoucs);
        microMaxButtonBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.micro_max_button_background);
        hideNotFoucs = NotFoucsWindow.findViewById(R.id.hide);
        hideNotFoucs.setOnClickListener(setColorForNotFoucs);
        miniNotFoucs = NotFoucsWindow.findViewById(R.id.mini);
        hideNotFoucs.setOnClickListener(setColorForNotFoucs);
        maxNotFoucs = NotFoucsWindow.findViewById(R.id.max);
        maxNotFoucs.setOnClickListener(setColorForNotFoucs);
        menuNotFoucs = NotFoucsWindow.findViewById(R.id.menu);
        menuNotFoucs.setOnClickListener(setColorForNotFoucs);
        closeNotFoucs = NotFoucsWindow.findViewById(R.id.close_button);
        closeNotFoucs.setOnClickListener(setColorForNotFoucs);
        closeButtonBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.close_button_background);
        titleNotFoucs = ((TextView)NotFoucsWindow.findViewById(R.id.title));
        titleNotFoucs.setText(getString(R.string.window_title));

        microMaxButtonBackgroundNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        titleBarNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        sizeBarNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        closeButtonBackgroundNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        titleBarAndButtonsNotFoucs = NotFoucsWindow.findViewById(R.id.title_bar_and_buttons);

        isAutoRun = findViewById(R.id.is_auto_run);
        isPermanent = findViewById(R.id.is_permanent);
        //---------------------------------------------------------------------------

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
        secondSet.setProgress(WindowParameter.getWindowTransitionsDuration(this));

        buttonsHeight=(SeekBar) findViewById(R.id.buttons_height_set);
        buttonsHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView buttonsHeightText=(TextView) findViewById(R.id.buttons_height);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ViewGroup.LayoutParams layoutParams = menu.getLayoutParams();
                progress += 20;
                buttonsHeightText.setText(String.valueOf(progress));
                layoutParams.height = (int)(getResources().getDisplayMetrics().density*progress);
                menu.setLayoutParams(layoutParams);
                hide.setLayoutParams(layoutParams);
                mini.setLayoutParams(layoutParams);
                max.setLayoutParams(layoutParams);
                close.setLayoutParams(layoutParams);
                menuNotFoucs.setLayoutParams(layoutParams);
                hideNotFoucs.setLayoutParams(layoutParams);
                miniNotFoucs.setLayoutParams(layoutParams);
                maxNotFoucs.setLayoutParams(layoutParams);
                closeNotFoucs.setLayoutParams(layoutParams);
                layoutParams = title.getLayoutParams();
                layoutParams.height = (int)(getResources().getDisplayMetrics().density*progress);
                title.setLayoutParams(layoutParams);
                titleNotFoucs.setLayoutParams(layoutParams);
                titleBarAndButtons.getLayoutParams().height = (int)(getResources().getDisplayMetrics().density*progress);
                titleBarAndButtonsNotFoucs.getLayoutParams().height = (int)(getResources().getDisplayMetrics().density*progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        buttonsHeight.setProgress(WindowParameter.getWindowButtonsHeight(this) - 20);

        buttonsWidth=(SeekBar) findViewById(R.id.buttons_width_set);
        buttonsWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView buttonsWidthText=(TextView) findViewById(R.id.buttons_width);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ViewGroup.LayoutParams layoutParams = menu.getLayoutParams();
                progress += 20;
                buttonsWidthText.setText(String.valueOf(progress));
                layoutParams.width = (int)(getResources().getDisplayMetrics().density*progress);
                menu.setLayoutParams(layoutParams);
                hide.setLayoutParams(layoutParams);
                mini.setLayoutParams(layoutParams);
                max.setLayoutParams(layoutParams);
                close.setLayoutParams(layoutParams);
                menuNotFoucs.setLayoutParams(layoutParams);
                hideNotFoucs.setLayoutParams(layoutParams);
                miniNotFoucs.setLayoutParams(layoutParams);
                maxNotFoucs.setLayoutParams(layoutParams);
                closeNotFoucs.setLayoutParams(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        buttonsWidth.setProgress(WindowParameter.getWindowButtonsWidth(this) - 20);

        sizeBarHeight=(SeekBar) findViewById(R.id.size_bar_height_set);
        sizeBarHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView sizeBarHeightText=(TextView) findViewById(R.id.size_bar_height);
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ViewGroup.LayoutParams layoutParams = sizeBar.getLayoutParams();
                progress += 5;
                sizeBarHeightText.setText(String.valueOf(progress));
                layoutParams.height = (int)(getResources().getDisplayMetrics().density*progress);
                sizeBar.setLayoutParams(layoutParams);
                sizeBarNotFoucs.setLayoutParams(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sizeBarHeight.setProgress(WindowParameter.getWindowSizeBarHeight(this) - 5);

        isAutoRun.setChecked(WindowParameter.isAutoRun(this));
        isPermanent.setChecked(WindowParameter.isPermanent(this));

        ((Button)findViewById(R.id.confirm)).setOnClickListener(save);
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
            if(v.getId()==R.id.confirm) {
                WindowParameter.setWindowTransitionsDuration(Setting.this,secondSet.getProgress());
                WindowParameter.setWindowButtonsHeight(Setting.this,buttonsHeight.getProgress() + 20);
                WindowParameter.setWindowButtonsWidth(Setting.this,buttonsWidth.getProgress() + 20);
                WindowParameter.setWindowwSizeBarHeight(Setting.this,sizeBarHeight.getProgress() + 5);
                WindowParameter.setAutoRun(Setting.this, isAutoRun.isChecked());
                WindowParameter.setPermanent(Setting.this, isPermanent.isChecked());
                wColor.save();
            }
            finish();
        }
    };

    @Override
    protected void onPause(){
        if(brickout != null)
            brickout.onPause();
        if(adoutWindow != -1) {
            WindowStruct.getWindowStruct(adoutWindow).setTransitionsDuration(0);
            WindowStruct.getWindowStruct(adoutWindow).close();
        }
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        if(brickout != null)
            brickout.onDestroy();
        brickout = null;
        mAdView.destroy();
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,getString(R.string.about));
        if(JackLog.isCanWriteLog())
            menu.add(0,1,0,"Log View");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 0:
                if(brickout == null)
                    brickout = new Brickout(Setting.this);
                if(adoutWindow == -1) {
                    adoutWindow = new WindowStruct.Builder(this, (WindowManager) getSystemService(Context.WINDOW_SERVICE))
                            .windowPages(new int[]{R.layout.about})
                            .windowPageTitles(new String[]{getString(R.string.about)})
                            .left(getResources().getDisplayMetrics().widthPixels / 2 - (int) (getResources().getDisplayMetrics().density * 97))
                            .top(getResources().getDisplayMetrics().heightPixels / 2 - (int) (getResources().getDisplayMetrics().density * 55))
                            .height((int) ((80 + WindowParameter.getWindowButtonsHeight(this)) * getResources().getDisplayMetrics().density))
                            .width((int) (195 * getResources().getDisplayMetrics().density))
                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MINI_BUTTON | WindowStruct.CLOSE_BUTTON)
                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                            .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                            .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                            .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                            .windowAction(new WindowStruct.WindowAction() {
                                @Override
                                public void goHide(WindowStruct windowStruct) {

                                }

                                @Override
                                public void goClose(WindowStruct windowStruct) {
                                    adoutWindow = -1;
                                }
                            })
                            .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                int count = -1;
                                TextView app_name, version;
                                ImageView icon;

                                @Override
                                public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {
                                    app_name = pageView.findViewById(R.id.app_name);
                                    version = pageView.findViewById(R.id.version);
                                    icon = pageView.findViewById(R.id.icon);
                                    icon.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (count == -1) {
                                                app_name.setText(getResources().getString(R.string.brickout));
                                                version.setText(getResources().getString(R.string.play));
                                            }
                                            if (++count % 2 == 0)
                                                icon.setImageResource(R.drawable.arkanoid_icon);
                                            else
                                                icon.setImageResource(R.drawable.arkanoid_icon2);
                                        }
                                    });
                                    version.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (count == -1)
                                                return;
                                            brickout.onPlay(count % 2 == 1);
                                        }
                                    });
                                    version.setText(version.getText() + BuildConfig.VERSION_NAME);
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
                            }).show().getNumber();
                }else
                    WindowStruct.getWindowStruct(adoutWindow).focusAndShowWindow();
                return true;
            case 1:
                startActivity(new Intent(Setting.this, LogView.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}