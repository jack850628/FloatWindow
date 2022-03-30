package com.example.jack8.floatwindow;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.jack8.floatwindow.AShCalculator.AShCalculator;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.jack8.floatwindow.Window.WindowStruct;

import java.util.Arrays;
import java.util.Map;

/**
 * 初始化視窗內容
 */
public class Calculator extends AutoRecordConstructionAndDeconstructionWindow {
    private AdRequest adRequest = new AdRequest.Builder().build();

    public Calculator(){
        super(CalculatorLauncher.class);
    }

    /**
     * 初始化視窗子頁面內容
     * @param context 視窗所在的Activity或Service的Context
     * @param pageView 子頁面的View
     * @param position 表示是第幾個子頁面
     * @param args 初始化視窗用的參數
     * @param windowStruct  子頁面所在的視窗本體
     */
    public void Construction(Context context, View pageView, int position, Map<String, Object> args, WindowStruct windowStruct){
        switch (position){
            case 0:
                calculator(context,pageView,windowStruct);
                break;
            case 1:
                initWindow2(context,pageView,windowStruct);
                break;
            case 2:
                initWindow3(context,pageView,windowStruct);
                break;
        }
    }

    AShCalculator aShCalculato = new AShCalculator();
    void calculator(Context context, final View pageView, final WindowStruct windowStruct){
        final EditText editText = pageView.findViewById(R.id.editText);
        ViewPager viewPager = pageView.findViewById(R.id.viewPager);
        final View[] page = new View[2];
        final ImageView[] pageIndicatorLight = new ImageView[]{
                pageView.findViewById(R.id.pageindicatorLight1),
                pageView.findViewById(R.id.pageindicatorLight2)
        };
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getTag().toString()){
                    case "=":{
                        String value;
                        try {
                            value = aShCalculato.exec(editText.getText().toString());
                        } catch (Exception e) {
                            value = e.getMessage();
                        }
                        editText.setText(value);
                        editText.setSelection(0, value.length());
                        break;
                    }
                    case "del":{
                        CharSequence value = editText.getText();
                        if(value.length() != 0) {
                            if(editText.getSelectionStart() < editText.getSelectionEnd())
                                ((Editable) value).delete(editText.getSelectionStart(), editText.getSelectionEnd());
                            else if(editText.getSelectionStart() != 0)
                                ((Editable) value).delete(editText.getSelectionStart() - 1, editText.getSelectionStart());
                        }
                        if(value.length() == 0) {
                            ((Editable) value).append('0');
                            editText.setSelection(editText.getText().length());
                        }
                        break;
                    }
                    default: {
                        if(!editText.getText().toString().equals("0") || v.getTag().toString().equals("."))
                            editText.getText().insert(editText.getSelectionStart(), v.getTag().toString());
                        else {
                            editText.setText(v.getTag().toString());
                            editText.setSelection(editText.getText().length());
                        }
                        break;
                    }
                }
            }
        };
        pageView.findViewById(R.id.del).setOnClickListener(onClickListener);
        page[0] = LayoutInflater.from(context).inflate(R.layout.calculator_sub_page1, viewPager, false);
        loadCalculatorSubPage(page[0], onClickListener);
        page[1] = LayoutInflater.from(context).inflate(R.layout.calculator_sub_page2, viewPager, false);
        loadCalculatorSubPage(page[1], onClickListener);

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return page.length;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(page[position]);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(page[position]);
                return page[position];
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i = 0;i < pageIndicatorLight.length;i++)
                    if(i == position)
                        pageIndicatorLight[i].setImageResource(R.drawable.page_indicator_focused);
                    else
                        pageIndicatorLight[i].setImageResource(R.drawable.page_indicator_unfocused);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void loadCalculatorSubPage(View page1, View.OnClickListener onClickListener){
        TableLayout tableLayout = page1.findViewById(R.id.table);
        for(int i = 0;i <tableLayout.getChildCount();i++){
            TableRow tableRow = (TableRow)tableLayout.getChildAt(i);
            for(int j = 0;j < tableRow.getChildCount();j++){
                tableRow.getChildAt(j).setOnClickListener(onClickListener);
            }
        }
    }

    private AdView adView1;
    void initWindow2(Context context, View pageView, final WindowStruct windowStruct){
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
        adView1 = pageView.findViewById(R.id.adView);
        adView1.loadAd(adRequest);
        adView1.pause();
    }

    private AdView adView2;
    void initWindow3(Context context, View pageView, final WindowStruct windowStruct){
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
        super.onResume(context, pageView, position, windowStruct);
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
