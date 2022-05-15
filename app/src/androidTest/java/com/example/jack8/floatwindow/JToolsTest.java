package com.example.jack8.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;

import androidx.test.annotation.UiThreadTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.jack8.floatwindow.Window.WindowStruct;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class JToolsTest extends TestCase {

    private Context context;
    private WindowManager windowManager;

    public JToolsTest(){
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Test
    @UiThreadTest
    public void testCreateAppUri() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "安安");
        String uri = JTools.createAppUri(
                new String[]{"poi"},
                new WindowStruct.Builder(context, windowManager).show(),
                context,
                map
        );
        Log.d("jtest_createAppUri", uri);
        Intent intent = JTools.uriToIntent(Uri.parse(uri));
        Log.d("jtest_path", Objects.requireNonNull(intent.getStringExtra(JTools.IntentParameter.PATH)));

        String firstDirectoryName = JTools.popPathFirstDirectoryNameFromIntent(intent);
        Log.d("jtest_firstDirectoryName", firstDirectoryName);
        Log.d("jtest_path", Objects.requireNonNull(intent.getStringExtra(JTools.IntentParameter.PATH)));

        firstDirectoryName = JTools.popPathFirstDirectoryNameFromIntent(intent);
        Log.d("jtest_firstDirectoryName", firstDirectoryName);
        Log.d("jtest_path", Objects.requireNonNull(intent.getStringExtra(JTools.IntentParameter.PATH)));
    }
}