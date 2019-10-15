package com.example.jack8.floatwindow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jack8.floatwindow.Window.WindowStruct;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryList {

    DataBaseForBrowser.HistoryDao historyDao;
    ArrayList<DataBaseForBrowser.History> historyList = new ArrayList<>();
    Context context;
    WebBrowser iw;
    RecyclerView recyclerView;
    HistoryListAdapter historyListAdapter;

    class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder>{

        ArrayList<DataBaseForBrowser.History> historyList;
        Context context;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        WindowStruct windowStruct;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new HistoryListAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.history_list_item,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
            viewHolder.title.setText(historyList.get(viewHolder.getAdapterPosition()).title);
            viewHolder.url.setText(historyList.get(viewHolder.getAdapterPosition()).url);
            viewHolder.date.setText(formatter.format(historyList.get(viewHolder.getAdapterPosition()).browserDate));
            viewHolder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iw.loadUrl(historyList.get(viewHolder.getAdapterPosition()).url);
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
//                                    HistoryList.this.removeHistory(viewHolder.getAdapterPosition());
//                            }
//                            popupMenu.dismiss();
//                            return true;
//                        }
//                    });
//                    popupMenu.show();

                    ListView menu_list = new ListView(context);
                    menu_list.setAdapter(new ArrayAdapter<String>(context, R.layout.list_item, R.id.item_text, new String[]{context.getString(R.string.open_link_in_new_window), context.getString(R.string.delete)}));
                    menu_list.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    final PopupWindow popupWindow =new PopupWindow(context);
                    popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                    popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    popupWindow.setContentView(menu_list);
                    popupWindow.setFocusable(true);
                    int anchorLoc[] = new int[2];
                    v.getLocationInWindow(anchorLoc);
                    popupWindow.showAtLocation(v,Gravity.LEFT | Gravity.TOP,0, anchorLoc[1]);
                    menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            switch (position){
                                case 0:{
                                    FloatServer.wm_count++;
                                    new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                            .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                                            .windowPageTitles(new String[]{context.getString(R.string.web_browser), context.getString(R.string.bookmarks), context.getString(R.string.history)})
                                            .windowInitArgs(new Object[][]{new String[]{historyList.get(viewHolder.getAdapterPosition()).url}})
                                            .windowAction(((FloatServer)context).windowAction)
                                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                            .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                                            .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                                            .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                                            .constructionAndDeconstructionWindow(new WebBrowser())
                                            .show();
                                    break;
                                }
                                case 1:
                                    HistoryList.this.removeHistory(viewHolder.getAdapterPosition());
                                    break;
                            }
                            popupWindow.dismiss();
                        }
                    });
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title, url, date;
            public View v;
            public ViewHolder(View v) {
                super(v);
                this.v = v;
                title = (TextView) v.findViewById(R.id.title);
                url = (TextView) v.findViewById(R.id.home_link);
                date = (TextView) v.findViewById(R.id.date);
            }
        }

        public HistoryListAdapter(Context context, ArrayList<DataBaseForBrowser.History> historyList, WindowStruct windowStruct){
            this.context = context;
            this.historyList = historyList;
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

        void Deconstruction(){
            historyList.clear();
            historyList = null;
            context = null;
            formatter = null;
            windowStruct = null;
        }
    }

    public HistoryList(Context context, View pageView, WebBrowser iw, DataBaseForBrowser.HistoryDao historyDao, WindowStruct windowStruct){
        this.context = context;
        this.iw = iw;
        this.historyList = historyList;
        this.historyDao = historyDao;


        recyclerView = (RecyclerView) pageView.findViewById(R.id.history_list);
        historyListAdapter = new HistoryListAdapter(context, historyList, windowStruct);
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
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                removeHistory(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }


    public void onResume() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                historyList.addAll(historyDao.getAllHistory());
                iw.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        historyListAdapter.notifyDataSetChanged();
                        if(historyList.size() == 0)
                            HistoryList.this.recyclerView.setVisibility(View.GONE);
                        else
                            HistoryList.this.recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    void removeHistory(final int index){
        new Thread(new Runnable() {
            @Override
            public void run() {
                historyDao.deleteHistory(historyList.get(index));
                iw.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        historyList.remove(index);
                        historyListAdapter.notifyItemRemoved(index);
                        if(historyList.size() == 0)
                            HistoryList.this.recyclerView.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    public void Deconstruction(){
        historyDao = null;
        historyList = null;
        context = null;
        iw = null;
        recyclerView = null;
        historyListAdapter.Deconstruction();
        historyListAdapter = null;
    }
}
