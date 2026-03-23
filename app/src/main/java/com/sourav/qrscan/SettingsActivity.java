package com.sourav.qrscan;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.sourav.qrscan.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        binding.switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
        binding.switchVibration.setChecked(prefs.getBoolean("vibration", true));
        binding.switchSound.setChecked(prefs.getBoolean("sound", true));
        binding.switchAutoCopy.setChecked(prefs.getBoolean("autocopy", false));
        binding.switchUrlAction.setChecked(prefs.getBoolean("url_action", false));

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("vibration", isChecked).apply();
        });

        binding.switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sound", isChecked).apply();
        });

        binding.switchAutoCopy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("autocopy", isChecked).apply();
        });

        binding.switchUrlAction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("url_action", isChecked).apply();
        });
    }
}
