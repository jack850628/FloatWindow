package com.example.jack8.floatwindow.Window;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jack8.floatwindow.DataBaseForBrowser;
import com.example.jack8.floatwindow.R;
import com.example.jack8.floatwindow.WindowTransitionsDuration;
import com.example.jack8.floatwindow.initWindow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HistoryList implements WindowStruct.constructionAndDeconstructionWindow {
    static Handler handler = new Handler(Looper.getMainLooper());

    DataBaseForBrowser.HistoryDao historyDao;
    ArrayList<DataBaseForBrowser.History> historyList;
    Context context;
    initWindow iw;

    static class HistoryListAdapter extends BaseAdapter{

        ArrayList<DataBaseForBrowser.History> historyList;
        Context context;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        public HistoryListAdapter(Context context, ArrayList<DataBaseForBrowser.History> historyList){
            this.context = context;
            this.historyList = historyList;
        }

        @Override
        public int getCount() {
            return historyList.size();
        }

        @Override
        public Object getItem(int position) {
            return historyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.history_list_item, null);
            ((TextView) convertView.findViewById(R.id.title)).setText(historyList.get(position).title);
            ((TextView) convertView.findViewById(R.id.url)).setText(historyList.get(position).url);
            ((TextView) convertView.findViewById(R.id.date)).setText(formatter.format(historyList.get(position).browserDate));
            return convertView;
        }
    }

    public static void show(final Context context, initWindow iw, final WindowStruct parentWindow, final DataBaseForBrowser.HistoryDao historyDao){
        final ArrayList<DataBaseForBrowser.History> historyList = new ArrayList<>();
        final HistoryListAdapter historyListAdapter = new HistoryListAdapter(context, historyList);

        ListView historyView = new ListView(context);
        historyView.setAdapter(historyListAdapter);
        new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .parentWindowNumber(parentWindow)
                .windowPageTitles(new String[]{context.getString(R.string.history)})
                .windowPages(new View[]{historyView})
                .transitionsDuration(WindowTransitionsDuration.getWindowTransitionsDuration(context))
                .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.SIZE_BAR)
                .constructionAndDeconstructionWindow(new HistoryList(context, iw, historyList, historyDao))
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                historyList.addAll(historyDao.getAllHistory());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        historyListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private HistoryList(Context context, initWindow iw, ArrayList<DataBaseForBrowser.History> historyList, DataBaseForBrowser.HistoryDao historyDao){
        this.context = context;
        this.iw = iw;
        this.historyList = historyList;
        this.historyDao = historyDao;
    }

    @Override
    public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {
        ListView listView = (ListView) pageView;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                iw.loadUrl(historyList.get(position).url);
            }
        });
    }

    @Override
    public void Deconstruction(Context context, View pageView, int position) {

    }

    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

    }
}
