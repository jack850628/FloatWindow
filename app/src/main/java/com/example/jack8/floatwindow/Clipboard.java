package com.example.jack8.floatwindow;

import android.content.Context;
import android.util.Log;

/**
 * 剪貼簿
 */
public class Clipboard {
    Context context;
    public Clipboard(Context context){
        this.context=context;
    }
    public void copyToClipboard(String str){
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(str);
            Log.e("version","1 version");
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("text label",str);
            clipboard.setPrimaryClip(clip);
            Log.e("version","2 version");
        }
    }

    public String copyFromClipboard(){
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            StringBuffer sb = new StringBuffer();
            if(clipboard.getPrimaryClip() == null)
                return "";
            for(int i = 0; i < clipboard.getPrimaryClip().getItemCount(); i++){
                sb.append(clipboard.getPrimaryClip().getItemAt(i).getText());
            }
            return sb.toString();
        } else {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            return clipboard.getText().toString();
        }

    }
}
