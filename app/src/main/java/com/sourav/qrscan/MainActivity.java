package com.sourav.qrscan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.sourav.qrscan.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AdMobDebug";
    private ActivityMainBinding binding;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Handle theme before setting content view
        SharedPreferences themePrefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob Initialized");
        });

        loadBannerAd();
        loadInterstitialAd();

        binding.btnScan.setOnClickListener(v -> {
            startActivity(new Intent(this, ScannerActivity.class));
        });

        binding.btnGenerate.setOnClickListener(v -> {
            startActivity(new Intent(this, GeneratorActivity.class));
        });

        binding.btnHistory.setOnClickListener(v -> {
            showInterstitialAndNavigate(new Intent(this, HistoryActivity.class));
        });

        binding.btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        
        binding.btnHelp.setOnClickListener(v -> {
            startActivity(new Intent(this, HelpActivity.class));
        });
    }

    private void loadBannerAd() {
        AdView adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        adView.setAdSize(AdSize.BANNER);
        
        binding.adContainer.removeAllViews();
        binding.adContainer.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Banner Ad Loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                Log.e(TAG, "Banner Ad Failed to Load: " + adError.getMessage());
            }
        });
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d(TAG, "Interstitial Ad Loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        Log.e(TAG, "Interstitial Ad Failed to Load: " + loadAdError.getMessage());
                    }
                });
    }

    private void showInterstitialAndNavigate(Intent intent) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad Dismissed");
                    mInterstitialAd = null;
                    loadInterstitialAd(); // Preload next
                    startActivity(intent);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    Log.e(TAG, "Interstitial Ad Failed to Show: " + adError.getMessage());
                    mInterstitialAd = null;
                    startActivity(intent);
                }
            });
            mInterstitialAd.show(this);
        } else {
            startActivity(intent);
        }
    }
}
