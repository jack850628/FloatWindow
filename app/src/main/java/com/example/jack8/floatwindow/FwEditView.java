package com.example.jack8.floatwindow;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;

/**
 * 常按會出現剪下複製貼上選單的EditText
 */
public class FwEditView extends EditText {
    public FwEditView(Context context) {
        super(context);
        addPopWindow(context);
    }

    public FwEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addPopWindow(context);
    }

    public FwEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addPopWindow(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FwEditView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        addPopWindow(context);
    }
    private void addPopWindow(final Context context){
        final Clipboard clipboard=new Clipboard(context);
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View editText) {
                View view = LayoutInflater.from(context).inflate(R.layout.fw_editview_popview,null);

                final PopupWindow popupWindow = new PopupWindow(context);
                popupWindow.setContentView(view);
                popupWindow.setWidth(FwEditView.this.getLayoutParams().width);
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                int anchorLoc[] = new int[2];
                editText.getLocationInWindow(anchorLoc);
                popupWindow.showAtLocation(editText, Gravity.LEFT | Gravity.TOP,0, anchorLoc[1] + editText.getHeight());

                OnClickListener onClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()){
                            case R.id.cut:
                                clipboard.copyToClipboard(FwEditView.this.getText().toString());
                                FwEditView.this.setText("");
                                break;
                            case R.id.copy:
                                clipboard.copyToClipboard(FwEditView.this.getText().toString());
                                break;
                            case R.id.paste:
                                FwEditView.this.setText(clipboard.copyFromClipboard());
                                break;
                        }
                        popupWindow.dismiss();
                    }
                };
                view.findViewById(R.id.cut).setOnClickListener(onClickListener);
                view.findViewById(R.id.copy).setOnClickListener(onClickListener);
                view.findViewById(R.id.paste).setOnClickListener(onClickListener);
                return false;
            }
        });
    }
}
