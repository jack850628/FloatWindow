package com.example.jack8.floatwindow;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Setup extends AppCompatActivity {
    WindowColor wColor;
    ViewGroup titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground;
    ViewGroup titleBarNotFoucs,sizeBarNotFoucs,microMaxButtonBackgroundNotFoucs,closeButtonBackgroundNotFoucs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup);
        wColor=new WindowColor(this);
        setTitle("浮動視窗設定");
        ViewGroup content = (ViewGroup) ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        View FoucsWindow,NotFoucsWindow;
        TextView windowColorSetTitle=new TextView(this);
        windowColorSetTitle.setText("視窗顏色:");
        windowColorSetTitle.setTextSize(20f);
        content.addView(windowColorSetTitle);
        content.addView(FoucsWindow=LayoutInflater.from(this).inflate(R.layout.window,null));
        TextView windowNotFoucsColorNotFoucsSetTitle=new TextView(this);
        windowNotFoucsColorNotFoucsSetTitle.setText("視窗失焦顏色:");
        windowNotFoucsColorNotFoucsSetTitle.setTextSize(20f);
        content.addView(windowNotFoucsColorNotFoucsSetTitle);
        content.addView(NotFoucsWindow=LayoutInflater.from(this).inflate(R.layout.window,null));
        ((WindowFrom)NotFoucsWindow).isStart=false;

        TextView prompt=new TextView(this);
        prompt.setText("點選視窗外框設定顏色");
        prompt.setTextSize(15f);
        ((ViewGroup) FoucsWindow.findViewById(R.id.wincon)).addView(prompt);

        titleBar=(ViewGroup) FoucsWindow.findViewById(R.id.title_bar);
        titleBar.setOnClickListener(setColor);
        sizeBar=(ViewGroup) FoucsWindow.findViewById(R.id.size);
        sizeBar.setOnClickListener(setColor);
        microMaxButtonBackground=(ViewGroup) FoucsWindow.findViewById(R.id.micro_max_button_background);
        FoucsWindow.findViewById(R.id.mini).setOnClickListener(setColor);
        FoucsWindow.findViewById(R.id.max).setOnClickListener(setColor);
        FoucsWindow.findViewById(R.id.menu).setOnClickListener(setColor);
        FoucsWindow.findViewById(R.id.close_button).setOnClickListener(setColor);
        closeButtonBackground=(ViewGroup) FoucsWindow.findViewById(R.id.close_button_background);

        TextView promptNotFoucs=new TextView(this);
        promptNotFoucs.setText("點選視窗外框設定失焦顏色");
        promptNotFoucs.setTextSize(15f);
        ((ViewGroup) NotFoucsWindow.findViewById(R.id.wincon)).addView(promptNotFoucs);

        titleBarNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.title_bar);
        titleBarNotFoucs.setOnClickListener(setColorForNotFoucs);
        sizeBarNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.size);
        sizeBarNotFoucs.setOnClickListener(setColorForNotFoucs);
        microMaxButtonBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.micro_max_button_background);
        NotFoucsWindow.findViewById(R.id.mini).setOnClickListener(setColorForNotFoucs);
        NotFoucsWindow.findViewById(R.id.max).setOnClickListener(setColorForNotFoucs);
        NotFoucsWindow.findViewById(R.id.menu).setOnClickListener(setColorForNotFoucs);
        NotFoucsWindow.findViewById(R.id.close_button).setOnClickListener(setColorForNotFoucs);
        closeButtonBackgroundNotFoucs=(ViewGroup) NotFoucsWindow.findViewById(R.id.close_button_background);

        microMaxButtonBackgroundNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        titleBarNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        sizeBarNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
        closeButtonBackgroundNotFoucs.setBackgroundColor(wColor.getWindowNotFoucs());
    }
    View.OnClickListener setColor=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorPickerDialog dialog=null;
            switch (v.getId()){
                case R.id.title_bar:
                case R.id.size:
                case R.id.menu:
                    dialog = new ColorPickerDialog(Setup.this, wColor.getTitleBar(), "選擇顏色",
                        new ColorPickerDialog.OnColorChangedListener() {
                            public void colorChanged(int color){
                                wColor.setTitleBar(color);
                                wColor.setSizeBar(color);
                                titleBar.setBackgroundColor(color);
                                sizeBar.setBackgroundColor(color);
                            }
                        });
                    break;
                case R.id.max:
                case R.id.mini:
                    dialog = new ColorPickerDialog(Setup.this, wColor.getMicroMaxButtonBackground(), "選擇顏色",
                            new ColorPickerDialog.OnColorChangedListener() {
                                public void colorChanged(int color){
                                    wColor.setMicroMaxButtonBackground(color);
                                    microMaxButtonBackground.setBackgroundColor(color);
                                }
                            });
                    break;
                case R.id.close_button:
                    dialog = new ColorPickerDialog(Setup.this, wColor.getCloseButtonBackground(), "選擇顏色",
                            new ColorPickerDialog.OnColorChangedListener() {
                                public void colorChanged(int color){
                                    wColor.setCloseButtonBackground(color);
                                    closeButtonBackground.setBackgroundColor(color);
                                }
                            });
                    break;
            }
            dialog.show();
        }
    };
    View.OnClickListener setColorForNotFoucs=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new ColorPickerDialog(Setup.this, wColor.getWindowNotFoucs(), "選擇顏色",
                    new ColorPickerDialog.OnColorChangedListener() {
                        public void colorChanged(int color) {
                            wColor.setWindowNotFoucs(color);
                            microMaxButtonBackgroundNotFoucs.setBackgroundColor(color);
                            titleBarNotFoucs.setBackgroundColor(color);
                            sizeBarNotFoucs.setBackgroundColor(color);
                            closeButtonBackgroundNotFoucs.setBackgroundColor(color);
                        }
                    }).show();
        }
    };
}
