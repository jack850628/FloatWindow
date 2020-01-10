package com.example.jack8.floatwindow;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LogView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view);
        setTitle("Log View");
        final TextView logView = findViewById(R.id.logView);
        JackLog.readLog(this, new JackLog.JackLogCallBack() {
            @Override
            public void callBack(String log) {
                logView.setText(log);
            }
        });
        findViewById(R.id.clearLog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JackLog.writeLog(LogView.this, "", Context.MODE_PRIVATE);
                logView.setText("");
            }
        });
    }
}
