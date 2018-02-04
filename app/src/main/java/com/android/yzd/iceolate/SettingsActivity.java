package com.android.yzd.iceolate;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

import com.kyleduo.switchbutton.SwitchButton;

public class SettingsActivity extends AppCompatActivity {

    public static final String IS_SHESHI = "IS_SHESHI";
    public static final String Alert = "Alert";
    public static final String Sound = "Sound";
    public static final String Shake = "Shake";
    public static final String ShakeSound = "ShakeSound";

    public static final String TEMPERATURE_SETTING = "TEMPERATURE_SETTING";

    public static final int pian = 35;
    public static final int max = 43;
    public static final int defaulttem = 39;

    private boolean isSheShi = true;
    private NumberProgressBar mNumber_progress_bar;
    private int tempetureSetting;
    private boolean isAlert;
    private boolean isSound;
    private boolean isShake;
    private boolean isShakeSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        isSheShi = (boolean) SharedPreferencesUtils.getParam(this, IS_SHESHI, true);
        isAlert = (boolean) SharedPreferencesUtils.getParam(this, Alert, false);
        isSound = (boolean) SharedPreferencesUtils.getParam(this, Sound, false);
        isShake = (boolean) SharedPreferencesUtils.getParam(this, Shake, false);
        isShakeSound = (boolean) SharedPreferencesUtils.getParam(this, ShakeSound, false);
        tempetureSetting = (int) SharedPreferencesUtils.getParam(this, TEMPERATURE_SETTING, defaulttem);


        SwitchButton sheShiButton = (SwitchButton) findViewById(R.id.sb_use_delay);
        sheShiButton.setChecked(isSheShi);
        sheShiButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSheShi = false;
                } else {
                    isSheShi = true;
                }
                SharedPreferencesUtils.setParam(SettingsActivity.this, IS_SHESHI, isSheShi);
            }
        });
        mNumber_progress_bar = (NumberProgressBar) findViewById(R.id.number_progress_bar);
        mNumber_progress_bar.setMax(max - pian);
        mNumber_progress_bar.setProgress(tempetureSetting - pian);

        findViewById(R.id.imageView4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempetureSetting += 1;
                mNumber_progress_bar.setProgress(tempetureSetting - pian);
                SharedPreferencesUtils.setParam(SettingsActivity.this, TEMPERATURE_SETTING, tempetureSetting);
            }
        });

        findViewById(R.id.jian).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempetureSetting -= 1;
                mNumber_progress_bar.setProgress(tempetureSetting - pian);
                SharedPreferencesUtils.setParam(SettingsActivity.this, TEMPERATURE_SETTING, tempetureSetting);
            }
        });


        SwitchButton alertSwitch = (SwitchButton) findViewById(R.id.alertSwitch);
        alertSwitch.setChecked(isAlert);
        alertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesUtils.setParam(SettingsActivity.this, Alert, isChecked);
            }
        });

        final SwitchButton zhenDongSwitch = (SwitchButton) findViewById(R.id.zhenDongSwitch);
        zhenDongSwitch.setChecked(isShake);
        zhenDongSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesUtils.setParam(SettingsActivity.this, Shake, isChecked);
            }
        });

        final SwitchButton soundSwitch = (SwitchButton) findViewById(R.id.soundSwitch);
        soundSwitch.setChecked(isSound);
        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesUtils.setParam(SettingsActivity.this, Sound, isChecked);
            }
        });

        SwitchButton soundShakeSwitch = (SwitchButton) findViewById(R.id.soundShakeSwitch);
        soundShakeSwitch.setChecked(isShakeSound);
        soundShakeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    soundSwitch.setChecked(false);
                    zhenDongSwitch.setChecked(false);
                }
                SharedPreferencesUtils.setParam(SettingsActivity.this, ShakeSound, isChecked);
            }
        });

    }
}
