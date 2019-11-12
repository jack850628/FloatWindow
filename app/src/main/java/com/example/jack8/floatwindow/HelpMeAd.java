package com.example.jack8.floatwindow;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class HelpMeAd extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final RewardedAd rewardedAd = new RewardedAd(this, "ca-app-pub-4604853118314154/2603122586");
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
                .addTestDevice("F4734F4691C588DB93799277888EA573")
                .build();
        rewardedAd.loadAd(adRequest, new RewardedAdLoadCallback(){
            @Override
            public void onRewardedAdLoaded() {
                rewardedAd.show(HelpMeAd.this, new RewardedAdCallback(){
                    @Override
                    public void onRewardedAdClosed() {
                        finish();
                    }

                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                        Intent i = new Intent(HelpMeAd.this, FloatServer.class);
                        i.putExtra(FloatServer.INTENT,FloatServer.SHOW_WATCHED_AD);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                            HelpMeAd.this.startService(i);
                        else
                            HelpMeAd.this.startForegroundService(i);
                    }
                });
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                finish();
                Log.i("獎勵廣告", "載入失敗");
            }
        });
    }
}
