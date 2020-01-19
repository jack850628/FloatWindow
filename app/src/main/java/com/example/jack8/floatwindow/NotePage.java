package com.example.jack8.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jack8.floatwindow.Window.WindowStruct;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class NotePage implements WindowStruct.constructionAndDeconstructionWindow {
    final static int ADD_NOTE = 0,OPEN_NOTE = 1;
    final static String NOTE = "Note",NOTES = "Notes";
    static LinkedList<String> showingNoteIdList = new LinkedList<>();
    static WindowStruct otherNoteList = null;//其他便條紙清單視窗
    static class OtherNodeListAdapter extends BaseAdapter {//其他便條紙清單所使用的Adapter

        public ArrayList<String[]> noteList = null;

        Context context;

        public OtherNodeListAdapter(Context context,LinkedList<String> showingNoteIdList){
            this.context = context;
            updateNodeList(showingNoteIdList);
        }

        public void update(LinkedList<String> showingNoteIdList){
            updateNodeList(showingNoteIdList);
            this.notifyDataSetChanged();
        }

        public void updateNodeList(LinkedList<String> showingNoteIdList){
            if(noteList == null)
                noteList = new ArrayList();
            else
                noteList.clear();
            noteList.add(new String[]{"ADD_NEW",context.getString(R.string.create_new_note)});
            try {
                JSONObject notes = new JSONObject(context.getSharedPreferences(NOTE,0).getString(NOTES, "{}"));
                Iterator<String> keys = notes.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    if (!showingNoteIdList.contains(key)) {
                        noteList.add(new String[]{key,notes.getString(key)});
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getCount() {
            return noteList.size();
        }

        @Override
        public Object getItem(int position) {
            return noteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.note_list_item, parent, false);
            TextView item_text = convertView.findViewById(R.id.item_text);
            Button removeNode = convertView.findViewById(R.id.remove_node);
            item_text.setText(noteList.get(position)[1]);
            if(position != 0) {
                item_text.setGravity(Gravity.NO_GRAVITY);
                item_text.setPadding(0,0,0,(int)(15*context.getResources().getDisplayMetrics().density));
                removeNode.setVisibility(View.VISIBLE);
                removeNode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences noteSpf = context.getSharedPreferences(NotePage.NOTE,0);
                        try {
                            JSONObject notes = new JSONObject(noteSpf.getString(NOTES,"{}"));
                            notes.remove(noteList.get(position)[0]);
                            SharedPreferences.Editor spfe=noteSpf.edit();
                            spfe.putString(NOTES,notes.toString());
                            spfe.apply();
                            noteList.remove(position);
                            OtherNodeListAdapter.this.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else {
                item_text.setPadding(0,(int)(15*context.getResources().getDisplayMetrics().density),0,(int)(15*context.getResources().getDisplayMetrics().density));
                item_text.setGravity(Gravity.CENTER_HORIZONTAL);
                removeNode.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
    static OtherNodeListAdapter otherNodeListAdapter = null;
    String noteId=null;
    Date dNow = new Date();
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", Locale.getDefault());
    Button nodePageMenuButton;
    MoveWindow moveWindow;
    int nodePageDisplayObj;
    @Override
    public void Construction(final Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
        final EditText note=(EditText) pageView.findViewById(R.id.note);
        final Clipboard clipboard=new Clipboard(context);
        final View toolsBar = pageView.findViewById(R.id.tools_bar);
        final ImageView copy = toolsBar.findViewById(R.id.copy);
        final ImageView paste = toolsBar.findViewById(R.id.paste);
        final ImageView showFrame = toolsBar.findViewById(R.id.show_frame);
        final ImageView close = toolsBar.findViewById(R.id.close);
        final SharedPreferences noteSpf = context.getSharedPreferences(NOTE,0);

        note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    JSONObject notes = new JSONObject(noteSpf.getString(NOTES,"{}"));
                    if(!s.toString().matches("^\\s*$"))
                        notes.put(noteId, s);
                    else
                        notes.remove(noteId);
                    SharedPreferences.Editor spfe=noteSpf.edit();
                    spfe.putString(NOTES,notes.toString());
                    spfe.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        moveWindow = new MoveWindow(context,windowStruct);
        nodePageMenuButton = new Button(context);
        nodePageMenuButton.setLayoutParams(new ViewGroup.LayoutParams(windowStruct.getWindowButtonsWidth(),windowStruct.getWindowButtonsHeight()));
        nodePageMenuButton.setPadding(0,0,0,0);
        nodePageMenuButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.menu));
        nodePageMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupWindow popupWindow =new PopupWindow(context);
                ListView listView = new ListView(context);
                listView.setAdapter(new ArrayAdapter(context, R.layout.list_item, R.id.item_text, new String[]{context.getString(R.string.other_notes),context.getString(R.string.share),context.getString(R.string.hide_frame)}));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:{
                                if(otherNoteList == null) {
                                    ListView nodeList = new ListView(context);
                                    nodeList.setAdapter(otherNodeListAdapter);
                                    nodeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            FloatServer.wm_count++;
                                            new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.HIDE_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                                                    .windowPageTitles(new String[]{context.getString(R.string.note)})
                                                    .windowPages(new int[]{R.layout.note_page})
                                                    .windowInitArgs(
                                                            position == 0
                                                                    ?new Object[][]{{NotePage.ADD_NOTE,""}}
                                                                    :new Object[][]{{NotePage.OPEN_NOTE,otherNodeListAdapter.noteList.get(position)[0]}}
                                                    )
                                                    .windowAction(((FloatServer)context).windowAction)
                                                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                                    .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                                                    .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                                                    .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                                                    .constructionAndDeconstructionWindow(new NotePage())
                                                    .show();
                                        }
                                    });
                                    FloatServer.wm_count++;
                                    otherNoteList = new WindowStruct.Builder(context, (WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                                            .windowPages(new View[]{nodeList})
                                            .windowPageTitles(new String[]{context.getString(R.string.other_notes)})
                                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MINI_BUTTON | WindowStruct.MAX_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(context))
                                            .windowButtonsHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(context)))
                                            .windowButtonsWidth((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(context)))
                                            .windowSizeBarHeight((int) (context.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(context)))
                                            .windowAction(new WindowStruct.WindowAction() {
                                                @Override
                                                public void goHide(WindowStruct windowStruct) {

                                                }

                                                @Override
                                                public void goClose(WindowStruct windowStruct) {
                                                    otherNoteList = null;
                                                    ((FloatServer)context).windowAction.goClose(windowStruct);
                                                }
                                            })
                                            .show();
                                }else
                                    otherNoteList.focusAndShowWindow();
                                break;
                            }
                            case 1:{
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, note.getText().toString());
                                sendIntent.setType("text/plain");
                                Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.select_APP));
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (sendIntent.resolveActivity(context.getPackageManager()) != null)
                                    context.startActivity(chooser);
                                break;
                            }
                            case 2:
                                nodePageDisplayObj = windowStruct.getDisplayObject();
                                windowStruct.setDisplayObject(WindowStruct.ALL_NOT_DISPLAY);
                                showFrame.setVisibility(View.VISIBLE);
                                note.setOnTouchListener(moveWindow);
                                break;
                        }
                        popupWindow.dismiss();
                    }
                });
                listView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);//先量測
                popupWindow.setWidth(listView.getMeasuredWidth());//再取寬度
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(listView);
                popupWindow.setFocusable(true);
                popupWindow.showAsDropDown(v);
            }
        });

        note.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toolsBar.setVisibility(View.VISIBLE);
                return true;
            }
        });
        View.OnClickListener copy_paste = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.copy:
                        clipboard.copyToClipboard(note.getText().toString());
                        Toast.makeText(context,context.getString(R.string.copyed),Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.paste:
                        note.setText(note.getText()+clipboard.copyFromClipboard());
                        break;
                    case R.id.show_frame:
                        windowStruct.setDisplayObject(nodePageDisplayObj);
                        showFrame.setVisibility(View.GONE);
                        note.setOnTouchListener(null);
                }
                toolsBar.setVisibility(View.GONE);
            }
        };
        copy.setOnClickListener(copy_paste);
        paste.setOnClickListener(copy_paste);
        showFrame.setOnClickListener(copy_paste);
        close.setOnClickListener(copy_paste);

        if(otherNodeListAdapter == null)
            otherNodeListAdapter = new OtherNodeListAdapter(context,showingNoteIdList);
        if(args == null || args.length == 0) {
            try {
                JSONObject notes = new JSONObject(noteSpf.getString(NOTES, "{}"));
                Iterator<String> keys = notes.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    if (!showingNoteIdList.contains(key)) {
                        noteId = key;
                        showingNoteIdList.add(noteId);
                        break;
                    }
                }
                if (noteId != null) {
                    note.setText(notes.getString(noteId));
                    otherNodeListAdapter.update(showingNoteIdList);
                }else{
                    noteId = String.valueOf(dNow.getTime());//formatter.format(dNow);
                    showingNoteIdList.add(noteId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            int flag = (int)args[0];
            switch (flag){
                case ADD_NOTE: {
                    noteId = String.valueOf(dNow.getTime());//formatter.format(dNow);
                    showingNoteIdList.add(noteId);
                    note.setText((String) args[1]);
                    break;
                }
                case OPEN_NOTE:{
                    noteId = (String)args[1];
                    showingNoteIdList.add(noteId);
                    try {
                        JSONObject notes = new JSONObject(noteSpf.getString(NOTES, "{}"));
                        note.setText(notes.getString(noteId));
                        otherNodeListAdapter.update(showingNoteIdList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {
        showingNoteIdList.remove(noteId);
        otherNodeListAdapter.update(showingNoteIdList);
    }

    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {
        ViewGroup micro_max_button = pageView.getRootView().findViewById(R.id.micro_max_button_background);
        micro_max_button.addView(nodePageMenuButton,0);
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
        ViewGroup micro_max_button = pageView.getRootView().findViewById(R.id.micro_max_button_background);
        micro_max_button.removeView(nodePageMenuButton);
    }
}
