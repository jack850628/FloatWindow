package com.example.jack8.floatwindow;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.jack8.floatwindow.Window.WindowStruct;

public class Help implements WindowStruct.constructionAndDeconstructionWindow  {
    @Override
    public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {
        switch (position){
            case 0:{
//                if(context.getResources().getConfiguration().locale.getCountry().equals("TW") ||
//                        context.getResources().getConfiguration().locale.getCountry().equals("HK") ||
//                        context.getResources().getConfiguration().locale.getCountry().equals("CN")){
//                    ((ImageView)pageView.findViewById(R.id.what_is_new_image1)).setImageResource(R.drawable.auto_run2);
//                    ((ImageView)pageView.findViewById(R.id.what_is_new_image2)).setImageResource(R.drawable.notify_image2);
//                }
                break;
            }
            case 1:{
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                    ((ImageView)pageView.findViewById(R.id.demonstration_image3)).setImageResource(R.drawable.add_shortcut_image2);
                }
                if(context.getResources().getConfiguration().locale.getCountry().equals("TW") ||
                        context.getResources().getConfiguration().locale.getCountry().equals("HK") ||
                        context.getResources().getConfiguration().locale.getCountry().equals("CN")){
                    ((ImageView)pageView.findViewById(R.id.demonstration_image1)).setImageResource(R.drawable.demonstration2);
                    ((ImageView)pageView.findViewById(R.id.demonstration_image2)).setImageResource(R.drawable.notify_image2);
                }
                break;
            }
        }
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
}
