package com.example.jack8.floatwindow;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jack8.floatwindow.Window.WindowStruct;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryList {
    private final int ONE_GET_LENGTH = 20;

    private DataBaseForBrowser.HistoryDao historyDao;
    private ArrayList<DataBaseForBrowser.History> historyList = new ArrayList<>();
    private Context context;
    private WebBrowser iw;
    private RecyclerView recyclerView;
    private TextView noDataText;
    private HistoryListAdapter historyListAdapter;

    private boolean haveMoreData = true;

    private void loadData(){
        final Date lastDate = !historyList.isEmpty()
                ? historyList.get(historyList.size() - 1).browserDate
                : Calendar.getInstance().getTime();
        haveMoreData = false;
        JTools.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                final List<DataBaseForBrowser.History> list = historyDao.getHistory(lastDate, ONE_GET_LENGTH);
                JTools.uiThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!list.isEmpty()){
                            final int updateFrom = historyList.size();
                            historyList.addAll(list);
                            haveMoreData = true;
                            historyListAdapter.notifyItemRangeChanged(updateFrom, list.size());
                        }else{
                            historyListAdapter.notifyItemRangeChanged(historyListAdapter.getItemCount() - 1, 1);
                        }
                        if (historyList.isEmpty())
                            noDataText.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder>{

        WindowStruct windowStruct;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new HistoryListAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.history_list_item,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int _) {
            final int index = viewHolder.getBindingAdapterPosition();
            viewHolder.v.setVisibility(View.VISIBLE);
            if(index == getItemCount() - 1) {
                viewHolder.title.setVisibility(View.GONE);
                viewHolder.url.setVisibility(View.GONE);
                viewHolder.date.setVisibility(View.GONE);
                viewHolder.v.setOnClickListener(null);
                viewHolder.v.setOnLongClickListener(null);
                if(haveMoreData) {
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    loadData();
                }else{
                    viewHolder.v.setVisibility(View.GONE);
                }
            } else {
                viewHolder.title.setVisibility(View.VISIBLE);
                viewHolder.url.setVisibility(View.VISIBLE);
                viewHolder.date.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.GONE);

                viewHolder.title.setText(historyList.get(index).title);
                viewHolder.url.setText(historyList.get(index).url);
                viewHolder.date.setText(formatter.format(historyList.get(index).browserDate));
                viewHolder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        iw.loadUrl(historyList.get(index).url);
                        windowStruct.showPage(0);
                    }
                });
                viewHolder.v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
//                    final PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(context,R.style.Theme_AppCompat), v, Gravity.BOTTOM);
//                    Menu menu = popupMenu.getMenu();
//                    menu.add(0, 0, 0, context.getString(R.string.delete));
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem menuItem) {
//                            switch (menuItem.getItemId()){
//                                case 0:
//                                    HistoryList.this.removeHistory(i);
//                            }
//                            popupMenu.dismiss();
//                            return true;
//                        }
//                    });
//                    popupMenu.show();

                        ListView menu_list = new ListView(context);
                        menu_list.setAdapter(new ArrayAdapter<String>(context, R.layout.list_item, R.id.item_text, new String[]{context.getString(R.string.open_link_in_new_window), context.getString(R.string.delete)}));
                        menu_list.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        final PopupWindow popupWindow = new PopupWindow(context);
                        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                        popupWindow.setContentView(menu_list);
                        popupWindow.setFocusable(true);
                        int anchorLoc[] = new int[2];
                        v.getLocationInWindow(anchorLoc);
                        popupWindow.showAtLocation(v, Gravity.START | Gravity.TOP, 0, anchorLoc[1]);
                        menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                switch (position) {
                                    case 0: {
                                        Intent intent = new Intent(context, WebBrowserLauncher.class);
                                        intent.putExtra(Intent.EXTRA_TEXT, historyList.get(index).url);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                        break;
                                    }
                                    case 1:
                                        HistoryList.this.removeHistory(index);
                                        break;
                                }
                                popupWindow.dismiss();
                            }
                        });
                        return true;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return historyList.size() + 1;//多加的1是用來顯示等在資料載入用的progressBar
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title, url, date;
            public ProgressBar progressBar;
            public View v;
            public ViewHolder(View v) {
                super(v);
                this.v = v;
                title = (TextView) v.findViewById(R.id.title);
                url = (TextView) v.findViewById(R.id.home_link);
                date = (TextView) v.findViewById(R.id.date);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            }
        }

        public HistoryListAdapter(WindowStruct windowStruct){
            this.windowStruct = windowStruct;
        }

//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null)
//                convertView = LayoutInflater.from(context).inflate(R.layout.history_list_item, null);
//            ((TextView) convertView.findViewById(R.id.title)).setText(historyList.get(position).title);
//            ((TextView) convertView.findViewById(R.id.url)).setText(historyList.get(position).url);
//            ((TextView) convertView.findViewById(R.id.date)).setText(formatter.format(historyList.get(position).browserDate));
//            return convertView;
//        }
    }

    public HistoryList(Context context, View pageView, WebBrowser iw, WindowStruct windowStruct){
        this.context = context;
        this.iw = iw;
        this.historyDao = DataBaseForBrowser.getInstance(context).historyDao();


        noDataText = (TextView) pageView.findViewById(R.id.no_data_text);
        recyclerView = (RecyclerView) pageView.findViewById(R.id.history_list);
        historyListAdapter = new HistoryListAdapter(windowStruct);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(historyListAdapter);
        recyclerView.setLayoutManager(layoutManager);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if(viewHolder.getBindingAdapterPosition() == recyclerView.getAdapter().getItemCount() - 1)
                    return 0;//沒有任何可滑動的方向
                else
                    return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeHistory(viewHolder.getBindingAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }


    public void onResume() {
        noDataText.setVisibility(View.GONE);
        haveMoreData = true;
        historyListAdapter.notifyDataSetChanged();
    }

    public void onPause(){
        historyList.clear();
    }

    void removeHistory(final int index){
        JTools.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                historyDao.deleteHistory(historyList.get(index));
                JTools.uiThread.post(new Runnable() {
                    @Override
                    public void run() {
                        historyList.remove(index);
                        historyListAdapter.notifyItemRemoved(index);
                        if(historyList.isEmpty())
                            HistoryList.this.noDataText.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    public void Deconstruction(){
        historyDao = null;
//        historyList = null;
//        context = null;
        iw = null;
        recyclerView = null;
        historyListAdapter = null;
    }
}
