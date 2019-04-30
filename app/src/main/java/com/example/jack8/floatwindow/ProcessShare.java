package com.example.jack8.floatwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.jack8.floatwindow.Window.WindowStruct;

/**
 * 處理帶有外部字串啟動時的頁面選擇
 */
public class ProcessShare extends initWindow {
    WindowManager wm;
    WindowStruct.WindowAction wa;
    public ProcessShare(WindowManager wm, WindowStruct.WindowAction wa){
        this.wm=wm;
        this.wa=wa;
    }
    public void Construction(final Context context, View pageView, int position, final Object[] args, final WindowStruct windowStruct){
        switch (pageView.getId()){
            case 0: {//當id為0就是選擇頁面的畫面
                ((ListView)pageView).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int[] layouts = (int[])args[0];
                        View winform= LayoutInflater.from(context).inflate(R.layout.window,null);
                        View window_content_view = LayoutInflater.from(context).inflate(layouts[position],(ViewGroup) winform,false);
                        window_content_view.setId(layouts[position]);//將id設定跟Resource id相同
                        String[] titles = (String[])args[1];
                        Object[] _args = position == 0
                                ? new Object[]{args[2]}
                                : new Object[]{initWindow.ADD_NOTE,args[2]};
                        FloatServer.wm_count++;
                        new WindowStruct.Builder(context, wm)
                                .windowPages(new View[]{window_content_view})
                                .windowPageTitles(new String[]{titles[position]})
                                .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
                                .windowInitArgs(new Object[][]{_args})
                                .windowAction(wa)
                                .constructionAndDeconstructionWindow(ProcessShare.this)
                                .show();
                        windowStruct.close();
                    }
                });
                break;
            }
            case R.layout.webpage:
                initWindow1(context,pageView,position,args,windowStruct);
                break;
            case R.layout.note_page:
                initWindow_Note_Page(context,pageView,position,args,windowStruct);
                break;
        }
    }
    public void Deconstruction(Context context, View pageView, int position){
        switch (pageView.getId()){
            case R.layout.webpage:
                super.Deconstruction(context,pageView,0);
                break;
            case R.layout.note_page:
                super.Deconstruction(context,pageView,1);
                break;
        }
    }
    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {
        switch (pageView.getId()){
            case R.layout.note_page:
                super.onResume(context,pageView,1,windowStruct);
                break;
        }
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
        switch (pageView.getId()){
            case R.layout.note_page:
                super.onPause(context,pageView,1,windowStruct);
                break;
        }
    }
}
