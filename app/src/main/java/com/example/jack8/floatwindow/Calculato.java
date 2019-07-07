package com.example.jack8.floatwindow;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.jack8.floatwindow.Window.WindowStruct;

/**
 * 初始化視窗內容
 */
public class Calculato implements WindowStruct.constructionAndDeconstructionWindow {

    /**
     * 初始化視窗子頁面內容
     * @param context 視窗所在的Activity或Service的Context
     * @param pageView 子頁面的View
     * @param position 表示是第幾個子頁面
     * @param  args 初始化視窗用的參數
     * @param windowStruct  子頁面所在的視窗本體
     */
    public void Construction(Context context, View pageView, int position,Object[] args, WindowStruct windowStruct){
        switch (position){
            case 0:
                initWindow2(context,pageView,windowStruct);
                break;
            case 1:
                initWindow3(context,pageView,windowStruct);
                break;
        }
    }

    private AdView adView1;
    public void initWindow2(Context context, View pageView, final WindowStruct windowStruct){
        final EditText et=(EditText)pageView.findViewById(R.id.Temperature);
        View.OnClickListener oc=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et.getText().toString().matches("^\\s*$"))
                    return;
                switch (v.getId()) {
                    case R.id.toC:
                        et.setText(String.valueOf((Float.parseFloat(et.getText().toString()) - 32) * 5f / 9f));
                        break;
                    case R.id.toF:
                        et.setText(String.valueOf(Float.parseFloat(et.getText().toString())*(9f/5f)+32));
                        break;
                }
            }
        };
        ((Button)pageView.findViewById(R.id.toC)).setOnClickListener(oc);
        ((Button)pageView.findViewById(R.id.toF)).setOnClickListener(oc);
        MobileAds.initialize(context, context.getString(R.string.AD_ID));
        adView1 = pageView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
                .addTestDevice("F4734F4691C588DB93799277888EA573")
                .build();
        adView1.loadAd(adRequest);
        adView1.pause();
    }

    private AdView adView2;
    public void initWindow3(Context context, View pageView, final WindowStruct windowStruct){
        final EditText H=(EditText)pageView.findViewById(R.id.H),W=(EditText)pageView.findViewById(R.id.W);
        final TextView BMI=(TextView)pageView.findViewById(R.id.BMI);
        ((Button)pageView.findViewById(R.id.CH)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(H.getText().toString().matches("^\\s*$")||W.getText().toString().matches("^\\s*$"))
                    return;
                float h=Float.parseFloat(H.getText().toString())/100f;
                BMI.setText(String.valueOf(Float.parseFloat(W.getText().toString())/(h*h)));
            }
        });
        adView2 = pageView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
                .addTestDevice("F4734F4691C588DB93799277888EA573")
                .build();
        adView2.loadAd(adRequest);
        adView2.pause();
    }

    public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct){
        if(position == 0)
            adView1.destroy();
        else if(position == 1)
            adView2.destroy();
    }

    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {
        if(position == 0)
            adView1.resume();
        else if(position == 1)
            adView2.resume();
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
        if(position == 0)
            adView1.pause();
        else if(position == 1)
            adView2.pause();
    }
}
