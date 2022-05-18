package com.example.jack8.floatwindow;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jack8.floatwindow.Window.WindowStruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebBrowserPermission extends WindowStruct.constructionAndDeconstructionWindow {
    private String domainName;
    private List<DataBaseForBrowser.WebsitePermission> websitePermissions;

    private Switch location, camera, microphone, MIDI_Sysex, protected_media;
    private AdView mAdView;

    public WebBrowserPermission(String domainName){
        this.domainName = domainName;
    }

    @Override
    public void Construction(Context context, View pageView, int position, Map<String, Object> args, WindowStruct windowStruct){
        location = (Switch) pageView.findViewById(R.id.location);
        camera = (Switch) pageView.findViewById(R.id.camera);
        microphone = (Switch) pageView.findViewById(R.id.microphone);
        MIDI_Sysex = (Switch) pageView.findViewById(R.id.MIDI_Sysex);
        protected_media = (Switch) pageView.findViewById(R.id.protected_media);

        location.setTag(WebBrowserRequestPermission.WebkitPermissionID.ACCESS_COARSE_LOCATION.getId());
        camera.setTag(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_VIDEO_CAPTURE.getId());
        microphone.setTag(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_AUDIO_CAPTURE.getId());
        MIDI_Sysex.setTag(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_MIDI_SYSEX.getId());
        protected_media.setTag(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_PROTECTED_MEDIA_ID.getId());

        JTools.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                websitePermissions = DataBaseForBrowser.getInstance(context).websitePermissionDao().getWebsitePermission(domainName);
                JTools.uiThread.post(new Runnable() {
                    @Override
                    public void run() {
                        for(DataBaseForBrowser.WebsitePermission websitePermission: websitePermissions){
                            ((Switch)pageView.findViewWithTag(websitePermission.permission)).setChecked(true);
                        }
                    }
                });
            }
        });

        ((TextView)pageView.findViewById(R.id.domain_name)).setText(domainName);
        mAdView = pageView.findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());

        pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<DataBaseForBrowser.WebsitePermission> success = new ArrayList<>();
                final List<DataBaseForBrowser.WebsitePermission> refuse = new ArrayList<>();
                LinearLayout switchList = pageView.findViewById(R.id.switch_list);
                int count = switchList.getChildCount();
                for(int i = 0; i < count; i++){
                    Switch switchBtn = (Switch) switchList.getChildAt(i);
                    if(switchBtn.isChecked()){
                        success.add(new DataBaseForBrowser.WebsitePermission(domainName, (int) switchBtn.getTag()));
                    }else{
                        refuse.add(new DataBaseForBrowser.WebsitePermission(domainName, (int) switchBtn.getTag()));
                    }
                }
                JTools.threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        DataBaseForBrowser.WebsitePermissionDao dao = DataBaseForBrowser.getInstance(context).websitePermissionDao();
                        DataBaseForBrowser.WebsitePermission[] websitePermissions = new DataBaseForBrowser.WebsitePermission[success.size()];
                        for(DataBaseForBrowser.WebsitePermission permission: success){
                            DataBaseForBrowser.WebsitePermission p = dao.getWebsitePermission(permission.domainName, permission.permission);
                            if(p == null)
                                dao.addWebsitePermission(permission);
                        }
                        for(DataBaseForBrowser.WebsitePermission permission: refuse){
                            DataBaseForBrowser.WebsitePermission p = dao.getWebsitePermission(permission.domainName, permission.permission);
                            if(p != null)
                                dao.deleteWebsitePermission(p);
                        }
                    }
                });
                windowStruct.close();
            }
        });
        pageView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowStruct.close();
            }
        });
    }

    @Override
    public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {
        mAdView.destroy();
    }
}
