package com.example.jack8.floatwindow;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jack8.floatwindow.Window.WindowStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebBrowserPermission extends WindowStruct.constructionAndDeconstructionWindow {
    private String domainName;

    private Spinner location, camera, microphone, MIDI_Sysex, protected_media;
    private AdView mAdView;
    private Map<Integer, Spinner> permissionSpinners;

    public WebBrowserPermission(String domainName){
        this.domainName = domainName;
    }

    @Override
    public void Construction(Context context, View pageView, int position, Map<String, Object> args, WindowStruct windowStruct){
        location = (Spinner) pageView.findViewById(R.id.location);
        camera = (Spinner) pageView.findViewById(R.id.camera);
        microphone = (Spinner) pageView.findViewById(R.id.microphone);
        MIDI_Sysex = (Spinner) pageView.findViewById(R.id.MIDI_Sysex);
        protected_media = (Spinner) pageView.findViewById(R.id.protected_media);

        location.setAdapter(new ArrayAdapter(context, R.layout.list_item_no_background, R.id.item_text, context.getResources().getStringArray(R.array.permissions)));
        camera.setAdapter(new ArrayAdapter(context, R.layout.list_item_no_background, R.id.item_text, context.getResources().getStringArray(R.array.permissions)));
        microphone.setAdapter(new ArrayAdapter(context, R.layout.list_item_no_background, R.id.item_text, context.getResources().getStringArray(R.array.permissions)));
        MIDI_Sysex.setAdapter(new ArrayAdapter(context, R.layout.list_item_no_background, R.id.item_text, context.getResources().getStringArray(R.array.permissions)));
        protected_media.setAdapter(new ArrayAdapter(context, R.layout.list_item_no_background, R.id.item_text, context.getResources().getStringArray(R.array.permissions)));

        location.setSelection(1);
        camera.setSelection(1);
        microphone.setSelection(1);
        MIDI_Sysex.setSelection(1);
        protected_media.setSelection(1);

        location.setTag(new DataBaseForBrowser.WebsitePermission(-1, domainName, WebBrowserRequestPermission.WebkitPermissionID.ACCESS_COARSE_LOCATION.getId(), DataBaseForBrowser.WebsitePermission.State.DEFAULT));
        camera.setTag(new DataBaseForBrowser.WebsitePermission(-1, domainName, WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_VIDEO_CAPTURE.getId(), DataBaseForBrowser.WebsitePermission.State.DEFAULT));
        microphone.setTag(new DataBaseForBrowser.WebsitePermission(-1, domainName, WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_AUDIO_CAPTURE.getId(), DataBaseForBrowser.WebsitePermission.State.DEFAULT));
        MIDI_Sysex.setTag(new DataBaseForBrowser.WebsitePermission(-1, domainName, WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_MIDI_SYSEX.getId(), DataBaseForBrowser.WebsitePermission.State.DEFAULT));
        protected_media.setTag(new DataBaseForBrowser.WebsitePermission(-1, domainName, WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_PROTECTED_MEDIA_ID.getId(), DataBaseForBrowser.WebsitePermission.State.DEFAULT));

        permissionSpinners = new HashMap<>();
        permissionSpinners.put(WebBrowserRequestPermission.WebkitPermissionID.ACCESS_COARSE_LOCATION.getId(), location);
        permissionSpinners.put(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_VIDEO_CAPTURE.getId(), camera);
        permissionSpinners.put(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_AUDIO_CAPTURE.getId(), microphone);
        permissionSpinners.put(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_MIDI_SYSEX.getId(), MIDI_Sysex);
        permissionSpinners.put(WebBrowserRequestPermission.WebkitPermissionID.RESOURCE_PROTECTED_MEDIA_ID.getId(), protected_media);

        JTools.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                List<DataBaseForBrowser.WebsitePermission> websitePermissions = DataBaseForBrowser.getInstance(context).websitePermissionDao().getWebsitePermission(domainName);
                JTools.uiThread.post(new Runnable() {
                    @Override
                    public void run() {
                        for(DataBaseForBrowser.WebsitePermission websitePermission: websitePermissions){
                            Spinner spinner = permissionSpinners.get(websitePermission.permission);
                            if(spinner != null) {
                                spinner.setTag(websitePermission);
                                spinner.setSelection(websitePermission.state.getType() + 1);
                            }
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
                final List<DataBaseForBrowser.WebsitePermission> websitePermissions = new ArrayList<>();
                for(Map.Entry<Integer, Spinner> s: permissionSpinners.entrySet()){
                    if(s.getValue().getTag() != null) {
                        DataBaseForBrowser.WebsitePermission websitePermission = (DataBaseForBrowser.WebsitePermission) s.getValue().getTag();
                        websitePermission.state = DataBaseForBrowser.WebsitePermission.State.getState(s.getValue().getSelectedItemPosition() - 1);
                        websitePermissions.add(websitePermission);
                    }
                }
                JTools.threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        DataBaseForBrowser.WebsitePermissionDao dao = DataBaseForBrowser.getInstance(context).websitePermissionDao();
                        for(DataBaseForBrowser.WebsitePermission websitePermission: websitePermissions){
                            if(websitePermission.id != -1)
                                dao.updateWebsitePermission(websitePermission);
                            else {
                                websitePermission.id = 0;//必須從-1改回0，不然id就會變成-1
                                dao.addWebsitePermission(websitePermission);
                            }
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
