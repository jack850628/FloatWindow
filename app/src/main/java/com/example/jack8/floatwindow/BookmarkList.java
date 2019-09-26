package com.example.jack8.floatwindow;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jack8.floatwindow.Window.WindowStruct;

import java.util.ArrayList;

public class BookmarkList {

    DataBaseForBrowser.BookmarksDao bookmarksDao;
    ArrayList<DataBaseForBrowser.Bookmark> bookmarkList = new ArrayList<DataBaseForBrowser.Bookmark>();
    Context context;
    WebBrowser iw;
    RecyclerView recyclerView;
    BookmarkList.BookmarkListAdapter bookmarkListAdapter;

    class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkList.BookmarkListAdapter.ViewHolder>{

        ArrayList<DataBaseForBrowser.Bookmark> bookmarkList;
        Context context;
        WindowStruct windowStruct;

        @NonNull
        @Override
        public BookmarkList.BookmarkListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new BookmarkList.BookmarkListAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.bookmark_list_item,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final BookmarkList.BookmarkListAdapter.ViewHolder viewHolder, final int i) {
            viewHolder.title.setText(bookmarkList.get(viewHolder.getAdapterPosition()).title);
            viewHolder.url.setText(bookmarkList.get(viewHolder.getAdapterPosition()).url);
            viewHolder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iw.loadUrl(bookmarkList.get(viewHolder.getAdapterPosition()).url);
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
//                                    BookmarkList.this.removeBookmark(viewHolder.getAdapterPosition());
//                            }
//                            popupMenu.dismiss();
//                            return true;
//                        }
//                    });
//                    popupMenu.show();

                    ListView menu_list = new ListView(context);
                    menu_list.setAdapter(new ArrayAdapter<String>(context, R.layout.list_item, R.id.item_text, new String[]{context.getString(R.string.open_link_in_new_window), context.getString(R.string.edit), context.getString(R.string.delete)}));
                    menu_list.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    final PopupWindow popupWindow =new PopupWindow(context);
                    popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                    popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    popupWindow.setContentView(menu_list);
                    popupWindow.setFocusable(true);
                    int anchorLoc[] = new int[2];
                    v.getLocationInWindow(anchorLoc);
                    popupWindow.showAtLocation(v, Gravity.LEFT | Gravity.TOP,0, anchorLoc[1]);
                    menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            switch (position){
                                case 0:{
                                    FloatServer.wm_count++;
                                    new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                            .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                                            .windowPageTitles(new String[]{context.getString(R.string.web_browser), context.getString(R.string.bookmarks), context.getString(R.string.history)})
                                            .windowInitArgs(new Object[][]{new String[]{bookmarkList.get(viewHolder.getAdapterPosition()).url}})
                                            .windowAction(((FloatServer)context).windowAction)
                                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                            .constructionAndDeconstructionWindow(new WebBrowser())
                                            .show();
                                    break;
                                }
                                case 1:{
                                    new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                            .parentWindow(windowStruct)
                                            .windowPageTitles(new String[]{context.getString(R.string.edit_bookmark)})
                                            .windowPages(new int[]{R.layout.add_to_bookmark})
                                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                                            .left(windowStruct.getRealWidth() / 2 + windowStruct.getRealPositionX() - (int)(context.getResources().getDisplayMetrics().density*280) / 2)
                                            .top(windowStruct.getRealHeight() / 2 + windowStruct.getRealPositionY() - (int)(context.getResources().getDisplayMetrics().density*155) / 2)
                                            .width((int)(context.getResources().getDisplayMetrics().density*280))
                                            .height((int)(context.getResources().getDisplayMetrics().density*155))
                                            .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                                @Override
                                                public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
                                                    final EditText title_box = pageView.findViewById(R.id.title);
                                                    final EditText url_box = pageView.findViewById(R.id.home_link);

                                                    title_box.setText(bookmarkList.get(viewHolder.getAdapterPosition()).title);
                                                    url_box.setText(bookmarkList.get(viewHolder.getAdapterPosition()).url);
                                                    pageView.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            final int index = viewHolder.getAdapterPosition();
                                                            bookmarkList.get(index).title = title_box.getText().toString();
                                                            bookmarkList.get(index).url = url_box.getText().toString();
                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        BookmarkList.this.bookmarksDao.upDataBookmark(bookmarkList.get(index).id, title_box.getText().toString(), url_box.getText().toString());
                                                                    }catch (SQLiteConstraintException e){
                                                                        BookmarkList.this.bookmarksDao.deleteBookmark(url_box.getText().toString());
                                                                        BookmarkList.this.bookmarksDao.upDataBookmark(bookmarkList.get(index).id, title_box.getText().toString(), url_box.getText().toString());
                                                                        bookmarkList.clear();
                                                                        bookmarkList.addAll(BookmarkList.this.bookmarksDao.getBookmarks());
                                                                        BookmarkList.this.iw.handler.post(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                BookmarkListAdapter.this.notifyDataSetChanged();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }).start();
                                                            BookmarkListAdapter.this.notifyDataSetChanged();
                                                            windowStruct.close();
                                                        }
                                                    });
                                                    pageView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            removeBookmark(viewHolder.getAdapterPosition());
                                                            windowStruct.close();
                                                        }
                                                    });
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
                                            })
                                            .show();
                                    break;
                                }
                                case 2:
                                    BookmarkList.this.removeBookmark(viewHolder.getAdapterPosition());
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
            return bookmarkList.size();
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

        public BookmarkListAdapter(Context context, WindowStruct windowStruct, ArrayList<DataBaseForBrowser.Bookmark> BookmarkList){
            this.context = context;
            this.windowStruct = windowStruct;
            this.bookmarkList = BookmarkList;
        }

//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null)
//                convertView = LayoutInflater.from(context).inflate(R.layout.Bookmark_list_item, null);
//            ((TextView) convertView.findViewById(R.id.title)).setText(BookmarkList.get(position).title);
//            ((TextView) convertView.findViewById(R.id.url)).setText(BookmarkList.get(position).url);
//            ((TextView) convertView.findViewById(R.id.date)).setText(formatter.format(BookmarkList.get(position).browserDate));
//            return convertView;
//        }

        void Deconstruction(){
            bookmarkList.clear();
            bookmarkList = null;
            context = null;
            windowStruct = null;
        }
    }

    public BookmarkList(Context context, View pageView, WebBrowser iw, DataBaseForBrowser.BookmarksDao bookmarksDao, WindowStruct windowStruct){
        this.context = context;
        this.iw = iw;
        this.bookmarksDao = bookmarksDao;

        recyclerView = (RecyclerView) pageView.findViewById(R.id.bookmark_list);
        bookmarkListAdapter = new BookmarkList.BookmarkListAdapter(context, windowStruct, bookmarkList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(bookmarkListAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void onResume() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bookmarkList.addAll(bookmarksDao.getBookmarks());
                iw.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        bookmarkListAdapter.notifyDataSetChanged();
                        if(bookmarkList.size() == 0)
                            BookmarkList.this.recyclerView.setVisibility(View.GONE);
                        else
                            BookmarkList.this.recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    void removeBookmark(final int index){
        new Thread(new Runnable() {
            @Override
            public void run() {
                bookmarksDao.deleteBookmark(bookmarkList.get(index));
                iw.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        bookmarkList.remove(index);
                        bookmarkListAdapter.notifyItemRemoved(index);
                        if(bookmarkList.size() == 0)
                            BookmarkList.this.recyclerView.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    public void Deconstruction(){
        bookmarksDao = null;
        bookmarkList = null;
        context = null;
        iw = null;
        recyclerView = null;
        bookmarkListAdapter.Deconstruction();
        bookmarkListAdapter = null;
    }
}
